package com.edusearch.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Shared ranking math used only by the video pipeline.
 *
 * There is deliberately no equivalent "web article score" here: ordinary
 * webpages don't expose view counts, so article results are left in
 * Google's native relevance order untouched (see WebArticleService).
 *
 * score = viewWeight * normalizedLogViews + recencyWeight * recencyDecay
 *
 *  - normalizedLogViews: log10(viewCount + 1), then squashed to roughly [0,1]
 *    against a soft ceiling so a handful of mega-viral outliers don't blow
 *    out the scale for everything else.
 *  - recencyDecay: exponential decay with a configurable half-life, so a
 *    2-year-old video with more views can still lose to a fresher one that's
 *    "good enough", without recency dominating outright.
 */
@Component
public class RankingUtil {

    // log10(viewCount) ceiling used to normalize into [0,1]; ~50M views -> ~1.0
    private static final double LOG_VIEW_CEILING = 7.7;

    private final double viewWeight;
    private final double recencyWeight;
    private final double halfLifeDays;

    public RankingUtil(
            @Value("${edusearch.ranking.view-weight:0.7}") double viewWeight,
            @Value("${edusearch.ranking.recency-weight:0.3}") double recencyWeight,
            @Value("${edusearch.ranking.recency-half-life-days:180}") double halfLifeDays) {
        this.viewWeight = viewWeight;
        this.recencyWeight = recencyWeight;
        this.halfLifeDays = halfLifeDays;
    }

    /**
     * @param viewCount   raw view count from YouTube statistics
     * @param publishedAt ISO-8601 publish timestamp from YouTube snippet
     * @return a score, roughly in [0,1], higher is better
     */
    public double score(long viewCount, String publishedAt) {
        double viewComponent = normalizedLogViews(viewCount);
        double recencyComponent = recencyDecay(publishedAt);
        return viewWeight * viewComponent + recencyWeight * recencyComponent;
    }

    double normalizedLogViews(long viewCount) {
        double logViews = Math.log10(Math.max(viewCount, 0) + 1);
        double normalized = logViews / LOG_VIEW_CEILING;
        return Math.min(Math.max(normalized, 0.0), 1.0);
    }

    double recencyDecay(String publishedAtIso) {
        if (publishedAtIso == null || publishedAtIso.isBlank()) {
            return 0.0;
        }
        try {
            Instant published = Instant.parse(publishedAtIso);
            long ageDays = ChronoUnit.DAYS.between(published, Instant.now());
            if (ageDays < 0) {
                ageDays = 0;
            }
            // Exponential decay: decay = 0.5 ^ (ageDays / halfLifeDays)
            return Math.pow(0.5, ageDays / halfLifeDays);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
