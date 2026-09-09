package com.edusearch.cache;

import com.edusearch.dto.SearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal in-memory cache to avoid burning YouTube / Tavily quota on
 * repeat searches for the same (subject, topic, lang) combo.
 *
 * Intentionally NOT Spring Cache/@Cacheable here so the TTL and the
 * "fromCache" flag on the response are both fully explicit and easy to
 * reason about; swap for Caffeine/Redis later without changing callers.
 */
@Service
public class SearchCacheService {

    private final long ttlMillis;
    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    public SearchCacheService(@Value("${edusearch.cache.ttl-seconds:1800}") long ttlSeconds) {
        this.ttlMillis = ttlSeconds * 1000L;
    }

    public static String buildKey(String subject, String topic, String lang) {
        return normalize(subject) + "|" + normalize(topic) + "|" + normalize(lang);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    public SearchResponse get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() > entry.expiresAtMillis) {
            store.remove(key);
            return null;
        }
        return entry.response;
    }

    public void put(String key, SearchResponse response) {
        store.put(key, new CacheEntry(response, System.currentTimeMillis() + ttlMillis));
    }

    /** Periodic sweep so the map doesn't grow unbounded with dead entries. */
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void evictExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> e.getValue().expiresAtMillis < now);
    }

    private static class CacheEntry {
        final SearchResponse response;
        final long expiresAtMillis;

        CacheEntry(SearchResponse response, long expiresAtMillis) {
            this.response = response;
            this.expiresAtMillis = expiresAtMillis;
        }
    }
}
