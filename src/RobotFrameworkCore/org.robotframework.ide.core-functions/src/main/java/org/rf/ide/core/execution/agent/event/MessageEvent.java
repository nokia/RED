/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;

import org.rf.ide.core.execution.agent.LogLevel;

import com.google.common.base.Objects;

public final class MessageEvent {

    public static MessageEvent fromMessage(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("message");
        return from((Map<?, ?>) arguments.get(0));
    }

    public static MessageEvent fromLogMessage(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("log_message");
        return from((Map<?, ?>) arguments.get(0));
    }

    private static MessageEvent from(final Map<?, ?> message) {
        final String msg = (String) message.get("message");
        final String timestamp = (String) message.get("timestamp");
        final String level = (String) message.get("level");

        if (msg == null || timestamp == null || level == null) {
            throw new IllegalArgumentException("Message event has to have the content, timestamp and level");
        }
        return new MessageEvent(msg, LogLevel.valueOf(level.toUpperCase()), timestamp);
    }


    private final String message;

    private final LogLevel level;

    private final String timestamp;

    public MessageEvent(final String message, final LogLevel level, final String timestamp) {
        this.message = message;
        this.level = level;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == MessageEvent.class) {
            final MessageEvent that = (MessageEvent) obj;
            return this.message.equals(that.message) && this.level == that.level
                    && this.timestamp.equals(that.timestamp);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(message, level, timestamp);
    }
}
