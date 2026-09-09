package com.edusearch.exception;

/** Thrown when YouTube or Tavily responds with a quota/rate-limit/auth error. */
public class ApiQuotaExceededException extends RuntimeException {

    private final String sourceApi;

    public ApiQuotaExceededException(String sourceApi, String message) {
        super(message);
        this.sourceApi = sourceApi;
    }

    public String getSourceApi() {
        return sourceApi;
    }
}
