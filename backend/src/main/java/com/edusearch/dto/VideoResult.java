package com.edusearch.dto;

/**
 * A single ranked YouTube video result.
 * score = weighted combination of log-scaled view count + recency decay (see RankingUtil).
 */
public class VideoResult {

    private String videoId;
    private String title;
    private String channel;
    private String thumbnailUrl;
    private long viewCount;
    private String publishedAt; // ISO-8601 string, e.g. 2024-03-11T10:15:30Z
    private String videoUrl;
    private double score;

    public VideoResult() {
    }

    public VideoResult(String videoId, String title, String channel, String thumbnailUrl,
                        long viewCount, String publishedAt, String videoUrl, double score) {
        this.videoId = videoId;
        this.title = title;
        this.channel = channel;
        this.thumbnailUrl = thumbnailUrl;
        this.viewCount = viewCount;
        this.publishedAt = publishedAt;
        this.videoUrl = videoUrl;
        this.score = score;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
