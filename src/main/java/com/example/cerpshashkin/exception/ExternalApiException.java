package com.example.cerpshashkin.exception;

public class ExternalApiException extends RuntimeException {

    public ExternalApiException() {
        super();
    }

    public ExternalApiException(final String message) {
        super(message);
    }

    public ExternalApiException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ExternalApiException(final Throwable cause) {
        super(cause);
    }

    public ExternalApiException(final String message, final Throwable cause,
                                final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
