package com.edusearch.dto;

import java.util.List;

/**
 * Top-level payload returned by GET /api/search.
 *
 * rankingNote is surfaced verbatim in the UI so the frontend never has to
 * hardcode the disclaimer text about videos vs. articles ranking differently.
 */
public class SearchResponse {

    private String topic;
    private String subject;
    private String lang;
    private List<VideoResult> videos;     // sorted best-first; videos.get(0) is the "top pick"
    private List<ArticleResult> articles; // Google's native relevance order
    private String videoRankingNote;
    private String articleRankingNote;
    private boolean fromCache;

    public SearchResponse() {
    }

    public SearchResponse(String topic, String subject, String lang, List<VideoResult> videos,
                           List<ArticleResult> articles, String videoRankingNote,
                           String articleRankingNote, boolean fromCache) {
        this.topic = topic;
        this.subject = subject;
        this.lang = lang;
        this.videos = videos;
        this.articles = articles;
        this.videoRankingNote = videoRankingNote;
        this.articleRankingNote = articleRankingNote;
        this.fromCache = fromCache;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public List<VideoResult> getVideos() {
        return videos;
    }

    public void setVideos(List<VideoResult> videos) {
        this.videos = videos;
    }

    public List<ArticleResult> getArticles() {
        return articles;
    }

    public void setArticles(List<ArticleResult> articles) {
        this.articles = articles;
    }

    public String getVideoRankingNote() {
        return videoRankingNote;
    }

    public void setVideoRankingNote(String videoRankingNote) {
        this.videoRankingNote = videoRankingNote;
    }

    public String getArticleRankingNote() {
        return articleRankingNote;
    }

    public void setArticleRankingNote(String articleRankingNote) {
        this.articleRankingNote = articleRankingNote;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }
}
