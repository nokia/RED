/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

public interface ServerResponse {

    String toMessage() throws ResponseException;

    public static class ResponseException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ResponseException(final String message) {
            super(message);
        }

        public ResponseException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}