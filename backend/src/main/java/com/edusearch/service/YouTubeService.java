package com.edusearch.service;

import com.edusearch.dto.VideoResult;
import com.edusearch.exception.ApiQuotaExceededException;
import com.edusearch.exception.UpstreamApiException;
import com.edusearch.util.RankingUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calls YouTube Data API v3:
 *   1. search.list  - find candidate videos for the topic, filtered by relevanceLanguage
 *   2. videos.list  - fetch statistics (viewCount) + snippet (publishedAt) for those candidates
 * then ranks them with RankingUtil (log-scale views + recency decay).
 */
@Service
public class YouTubeService {

    private static final Logger log = LoggerFactory.getLogger(YouTubeService.class);
    private static final String SOURCE = "youtube";

    private final WebClient webClient;
    private final RankingUtil rankingUtil;
    private final String apiKey;
    private final String baseUrl;
    private final int maxResults;

    public YouTubeService(WebClient webClient,
                           RankingUtil rankingUtil,
                           @Value("${youtube.api.key}") String apiKey,
                           @Value("${youtube.api.base-url}") String baseUrl,
                           @Value("${edusearch.search.max-videos:15}") int maxResults) {
        this.webClient = webClient;
        this.rankingUtil = rankingUtil;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.maxResults = maxResults;
    }

    /**
     * @param topic            search topic, e.g. "Laplace Transform"
     * @param subject          subject hint appended to the query for better relevance, e.g. "Math"
     * @param relevanceLanguage ISO 639-1 code: "en" | "hi" | "te"
     */
    public List<VideoResult> search(String topic, String subject, String relevanceLanguage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new UpstreamApiException(SOURCE,
                    "YouTube API key is not configured (set YOUTUBE_API_KEY).", null);
        }

        String query = buildQuery(topic, subject);
        List<String> videoIds = searchVideoIds(query, relevanceLanguage);
        if (videoIds.isEmpty()) {
            return List.of();
        }
        List<VideoResult> results = fetchStatistics(videoIds);

        for (VideoResult v : results) {
            v.setScore(rankingUtil.score(v.getViewCount(), v.getPublishedAt()));
        }
        results.sort(Comparator.comparingDouble(VideoResult::getScore).reversed());
        return results;
    }

    private String buildQuery(String topic, String subject) {
        if (subject == null || subject.isBlank() || subject.equalsIgnoreCase("custom")) {
            return topic;
        }
        return subject + " " + topic;
    }

    private List<String> searchVideoIds(String query, String relevanceLanguage) {
        try {
            JsonNode root = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("www.googleapis.com")
                            .path("/youtube/v3/search")
                            .queryParam("part", "snippet")
                            .queryParam("type", "video")
                            .queryParam("videoEmbeddable", "true")
                            .queryParam("safeSearch", "strict")
                            .queryParam("order", "relevance")
                            .queryParam("maxResults", maxResults)
                            .queryParam("q", query)
                            .queryParam("relevanceLanguage", relevanceLanguage)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<String> ids = new ArrayList<>();
            if (root != null && root.has("items")) {
                for (JsonNode item : root.get("items")) {
                    JsonNode idNode = item.path("id").path("videoId");
                    if (!idNode.isMissingNode()) {
                        ids.add(idNode.asText());
                    }
                }
            }
            return ids;
        } catch (WebClientResponseException e) {
            throw translateError(e);
        } catch (Exception e) {
            log.error("YouTube search.list call failed", e);
            throw new UpstreamApiException(SOURCE, "YouTube search failed", e);
        }
    }

    private List<VideoResult> fetchStatistics(List<String> videoIds) {
        try {
            JsonNode root = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("www.googleapis.com")
                            .path("/youtube/v3/videos")
                            .queryParam("part", "snippet,statistics")
                            .queryParam("id", String.join(",", videoIds))
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<VideoResult> results = new ArrayList<>();
            if (root != null && root.has("items")) {
                for (JsonNode item : root.get("items")) {
                    String videoId = item.path("id").asText();
                    JsonNode snippet = item.path("snippet");
                    JsonNode statistics = item.path("statistics");

                    String title = snippet.path("title").asText("");
                    String channel = snippet.path("channelTitle").asText("");
                    String publishedAt = snippet.path("publishedAt").asText("");
                    String thumbnailUrl = snippet.path("thumbnails").path("high").path("url")
                            .asText(snippet.path("thumbnails").path("default").path("url").asText(""));
                    long viewCount = statistics.path("viewCount").asLong(0L);

                    results.add(new VideoResult(
                            videoId, title, channel, thumbnailUrl, viewCount, publishedAt,
                            "https://www.youtube.com/watch?v=" + videoId, 0.0));
                }
            }
            return results;
        } catch (WebClientResponseException e) {
            throw translateError(e);
        } catch (Exception e) {
            log.error("YouTube videos.list call failed", e);
            throw new UpstreamApiException(SOURCE, "YouTube statistics lookup failed", e);
        }
    }

    private RuntimeException translateError(WebClientResponseException e) {
        int status = e.getStatusCode().value();
        if (status == 403 || status == 429) {
            log.warn("YouTube quota/rate-limit response: {}", e.getResponseBodyAsString());
            return new ApiQuotaExceededException(SOURCE, "YouTube API quota exceeded");
        }
        log.error("YouTube API error {}: {}", status, e.getResponseBodyAsString());
        return new UpstreamApiException(SOURCE, "YouTube API returned status " + status, e);
    }
}
