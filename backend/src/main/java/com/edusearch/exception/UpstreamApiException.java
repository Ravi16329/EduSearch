package com.edusearch.exception;

/** Thrown for any non-quota upstream failure (network error, malformed response, 5xx, etc). */
public class UpstreamApiException extends RuntimeException {

    private final String sourceApi;

    public UpstreamApiException(String sourceApi, String message, Throwable cause) {
        super(message, cause);
        this.sourceApi = sourceApi;
    }

    public String getSourceApi() {
        return sourceApi;
    }
}
