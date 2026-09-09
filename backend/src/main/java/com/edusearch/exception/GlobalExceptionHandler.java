package com.edusearch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiQuotaExceededException.class)
    public ResponseEntity<ErrorResponse> handleQuota(ApiQuotaExceededException ex) {
        ErrorResponse body = new ErrorResponse(
                "API_QUOTA_EXCEEDED",
                "We've hit the daily search limit for " + ex.getSourceApi()
                        + ". Please try again later.",
                ex.getSourceApi());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    @ExceptionHandler(UpstreamApiException.class)
    public ResponseEntity<ErrorResponse> handleUpstream(UpstreamApiException ex) {
        ErrorResponse body = new ErrorResponse(
                "UPSTREAM_ERROR",
                "We couldn't reach " + ex.getSourceApi() + " right now. Please try again shortly.",
                ex.getSourceApi());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        ErrorResponse body = new ErrorResponse(
                "BAD_REQUEST",
                "Missing required parameter: " + ex.getParameterName(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse body = new ErrorResponse("BAD_REQUEST", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse body = new ErrorResponse(
                "INTERNAL_ERROR",
                "Something went wrong while searching. Please try again.",
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
