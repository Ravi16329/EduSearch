package com.edusearch.controller;

import com.edusearch.cache.SearchCacheService;
import com.edusearch.dto.ArticleResult;
import com.edusearch.dto.SearchResponse;
import com.edusearch.dto.VideoResult;
import com.edusearch.service.WebArticleService;
import com.edusearch.service.YouTubeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class SearchController {

    private static final java.util.Set<String> SUPPORTED_LANGS = java.util.Set.of("en", "hi", "te");

    private static final String VIDEO_RANKING_NOTE =
            "Videos are ranked by a blend of view count (log-scaled) and how recent the upload is.";
    private static final String ARTICLE_RANKING_NOTE =
            "These are top articles by relevance, not \"most viewed\" - regular webpages don't expose view counts.";

    private final YouTubeService youTubeService;
    private final WebArticleService webArticleService;
    private final SearchCacheService cacheService;

    public SearchController(YouTubeService youTubeService,
                             WebArticleService webArticleService,
                             SearchCacheService cacheService) {
        this.youTubeService = youTubeService;
        this.webArticleService = webArticleService;
        this.cacheService = cacheService;
    }

    @GetMapping("/api/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam String topic,
            @RequestParam(required = false, defaultValue = "custom") String subject,
            @RequestParam(required = false, defaultValue = "en") String lang) {

        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("`topic` must not be empty");
        }
        String relevanceLanguage = normalizeLang(lang);

        String cacheKey = SearchCacheService.buildKey(subject, topic, relevanceLanguage);
        SearchResponse cached = cacheService.get(cacheKey);
        if (cached != null) {
            cached.setFromCache(true);
            return ResponseEntity.ok(cached);
        }

        // Query YouTube and Tavily in parallel.
        CompletableFuture<List<VideoResult>> videosFuture = CompletableFuture.supplyAsync(
                () -> youTubeService.search(topic, subject, relevanceLanguage));
        CompletableFuture<List<ArticleResult>> articlesFuture = CompletableFuture.supplyAsync(
                () -> webArticleService.search(topic, subject, relevanceLanguage));

        // Join both; if either throws, the underlying exception propagates and is
        // handled by GlobalExceptionHandler (CompletionException unwraps via getCause
        // through Spring's exception resolution for our custom RuntimeExceptions).
        List<VideoResult> videos;
        List<ArticleResult> articles;
        try {
            CompletableFuture.allOf(videosFuture, articlesFuture).join();
            videos = videosFuture.get();
            articles = articlesFuture.get();
        } catch (java.util.concurrent.CompletionException | java.util.concurrent.ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Search interrupted", e);
        }

        SearchResponse response = new SearchResponse(
                topic, subject, relevanceLanguage, videos, articles,
                VIDEO_RANKING_NOTE, ARTICLE_RANKING_NOTE, false);

        cacheService.put(cacheKey, response);
        return ResponseEntity.ok(response);
    }

    private String normalizeLang(String lang) {
        if (lang == null) return "en";
        String l = lang.trim().toLowerCase();
        return SUPPORTED_LANGS.contains(l) ? l : "en";
    }
}
