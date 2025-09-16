package com.example.cerpshashkin.exception;

public class ExternalApiException extends RuntimeException {

    private static final String FAILED_OPERATION_TEMPLATE = "Failed to %s from %s";

    public ExternalApiException(final String operation, final String provider, final Throwable cause) {
        super(String.format(FAILED_OPERATION_TEMPLATE, operation, provider), cause);
    }
}
