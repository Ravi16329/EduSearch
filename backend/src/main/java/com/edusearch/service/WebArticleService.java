package com.edusearch.service;

import com.edusearch.dto.ArticleResult;
import com.edusearch.exception.ApiQuotaExceededException;
import com.edusearch.exception.UpstreamApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls the Tavily Search API (https://tavily.com) for general web/article
 * results (GFG, W3Schools, blogs, etc). Results keep Tavily's native
 * relevance order untouched - there is intentionally no view-count scoring
 * here, because ordinary webpages don't expose a view-count concept the
 * way YouTube does.
 *
 * Tavily has no first-class "restrict results to this language" parameter
 * (unlike Google CSE's `lr`), so the language is folded into the query text
 * itself as a soft hint (e.g. "... explained in Hindi"). This is a best
 * effort, not a hard filter.
 */
@Service
public class WebArticleService {

    private static final Logger log = LoggerFactory.getLogger(WebArticleService.class);
    private static final String SOURCE = "tavily";

    private static final Map<String, String> LANG_HINT = Map.of(
            "en", "",
            "hi", " explained in Hindi",
            "te", " explained in Telugu"
    );

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;
    private final int maxResults;

    public WebArticleService(WebClient webClient,
                              @Value("${tavily.api.key}") String apiKey,
                              @Value("${tavily.api.base-url}") String baseUrl,
                              @Value("${edusearch.search.max-articles:10}") int maxResults) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.maxResults = maxResults;
    }

    /**
     * @param topic    search topic, e.g. "Laplace Transform"
     * @param subject  subject hint appended to the query, e.g. "Math"
     * @param langCode ISO code: "en" | "hi" | "te" - folded into the query as a soft hint
     */
    public List<ArticleResult> search(String topic, String subject, String langCode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new UpstreamApiException(SOURCE,
                    "Tavily is not configured (set TAVILY_API_KEY).", null);
        }

        String query = buildQuery(topic, subject, langCode);
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("query", query);
            body.put("search_depth", "basic");
            body.put("max_results", Math.min(Math.max(maxResults, 1), 20));
            body.put("include_answer", false);

            JsonNode root = webClient.post()
                    .uri(baseUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<ArticleResult> results = new ArrayList<>();
            if (root != null && root.has("results")) {
                int rank = 1;
                for (JsonNode item : root.get("results")) {
                    String title = item.path("title").asText("");
                    String snippet = item.path("content").asText("");
                    String url = item.path("url").asText("");
                    String source = extractDomain(url);
                    results.add(new ArticleResult(title, source, trimSnippet(snippet), url, rank++));
                }
            }
            return results;
        } catch (WebClientResponseException e) {
            throw translateError(e);
        } catch (Exception e) {
            log.error("Tavily search call failed", e);
            throw new UpstreamApiException(SOURCE, "Web article search failed", e);
        }
    }

    private String buildQuery(String topic, String subject, String langCode) {
        String base = (subject == null || subject.isBlank() || subject.equalsIgnoreCase("custom"))
                ? topic
                : subject + " " + topic;
        return base + LANG_HINT.getOrDefault(langCode, "");
    }

    private String trimSnippet(String snippet) {
        if (snippet == null) return "";
        return snippet.length() > 280 ? snippet.substring(0, 277) + "..." : snippet;
    }

    private String extractDomain(String url) {
        try {
            String host = URI.create(url).getHost();
            return host == null ? url : host.replaceFirst("^www\\.", "");
        } catch (Exception e) {
            return url;
        }
    }

    private RuntimeException translateError(WebClientResponseException e) {
        int status = e.getStatusCode().value();
        if (status == 401 || status == 403 || status == 429) {
            log.warn("Tavily quota/auth response: {}", e.getResponseBodyAsString());
            return new ApiQuotaExceededException(SOURCE, "Tavily API quota or auth error");
        }
        log.error("Tavily API error {}: {}", status, e.getResponseBodyAsString());
        return new UpstreamApiException(SOURCE, "Tavily API returned status " + status, e);
    }
}
