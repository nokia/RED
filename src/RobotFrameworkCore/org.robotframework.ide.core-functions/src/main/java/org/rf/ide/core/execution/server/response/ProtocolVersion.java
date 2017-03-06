/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.ImmutableMap;

public final class ProtocolVersion implements ServerResponse {

    private final boolean isCorrect;

    public ProtocolVersion(final boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    @Override
    public String toMessage() {
        try {
            final Map<String, Boolean> arguments = ImmutableMap.of("is_correct", Boolean.valueOf(isCorrect));
            final Map<String, Object> value = ImmutableMap.of("protocol_version", arguments);

            return new ObjectMapper().writeValueAsString(value);
        } catch (final IOException e) {
            throw new ResponseException("Unable to serialize protocol version response arguments to json", e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == ProtocolVersion.class) {
            final ProtocolVersion that = (ProtocolVersion) obj;
            return this.isCorrect == that.isCorrect;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(isCorrect);
    }
}
