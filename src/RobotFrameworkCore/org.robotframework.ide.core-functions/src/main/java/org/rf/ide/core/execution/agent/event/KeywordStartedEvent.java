/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class KeywordStartedEvent {

    private final String name;

    private final String keywordType;

    private final String libraryName;

    public static KeywordStartedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("start_keyword");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String keywordType = (String) attributes.get("type");
        final String libraryName = (String) attributes.get("libname");

        return new KeywordStartedEvent(name, keywordType, libraryName);
    }

    public KeywordStartedEvent(final String name, final String keywordType, final String libraryName) {
        this.name = name;
        this.keywordType = keywordType;
        this.libraryName = libraryName;
    }

    public String getName() {
        return name;
    }

    public String getKeywordType() {
        return keywordType;
    }

    public String getLibraryName() {
        return libraryName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == KeywordStartedEvent.class) {
            final KeywordStartedEvent that = (KeywordStartedEvent) obj;
            return this.name.equals(that.name) && this.keywordType.equals(that.keywordType)
                    && this.libraryName.equals(that.libraryName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, keywordType, libraryName);
    }
}