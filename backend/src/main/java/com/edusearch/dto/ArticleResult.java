package com.edusearch.dto;

/**
 * A single web/article result from Google Custom Search.
 * Deliberately has NO score/viewCount field: ordinary webpages don't expose
 * view counts, so these are relevance-ranked in Google's native order only.
 */
public class ArticleResult {

    private String title;
    private String source;   // domain, e.g. "geeksforgeeks.org"
    private String snippet;
    private String url;
    private int rank;        // 1-based position in Google's relevance order

    public ArticleResult() {
    }

    public ArticleResult(String title, String source, String snippet, String url, int rank) {
        this.title = title;
        this.source = source;
        this.snippet = snippet;
        this.url = url;
        this.rank = rank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
