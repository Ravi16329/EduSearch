package com.edusearch.exception;

import java.time.Instant;

public class ErrorResponse {
    private String error;      // machine-readable code, e.g. "API_QUOTA_EXCEEDED"
    private String message;    // human-readable message safe to show in the UI
    private String source;     // "youtube" | "tavily" | null
    private Instant timestamp;

    public ErrorResponse(String error, String message, String source) {
        this.error = error;
        this.message = message;
        this.source = source;
        this.timestamp = Instant.now();
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
