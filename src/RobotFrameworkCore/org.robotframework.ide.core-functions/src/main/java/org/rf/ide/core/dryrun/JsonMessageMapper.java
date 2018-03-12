/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.dryrun;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.execution.agent.event.MessageEvent;

class JsonMessageMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final TypeReference<Map<String, ?>> MESSAGE_TYPE = new TypeReference<Map<String, ?>>() {
    };

    static <T> Optional<T> readValue(final MessageEvent event, final String key,
            final TypeReference<Map<String, T>> type) throws IOException {
        final Map<String, ?> messageEntry = MAPPER.readValue(event.getMessage(), MESSAGE_TYPE);
        if (messageEntry.containsKey(key)) {
            final Map<String, T> typedEntry = MAPPER.readValue(event.getMessage(), type);
            return Optional.of(typedEntry.get(key));
        }
        return Optional.empty();
    }

    @SuppressWarnings("serial")
    static class JsonMessageMapperException extends RuntimeException {

        public JsonMessageMapperException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

}
