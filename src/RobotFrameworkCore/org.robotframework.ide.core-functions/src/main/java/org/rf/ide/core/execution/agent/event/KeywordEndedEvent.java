/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.rf.ide.core.execution.agent.Status;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

public final class KeywordEndedEvent {

    public static KeywordEndedEvent fromPre(final Map<String, Object> eventMap) {
        return fromEventArguments((List<?>) eventMap.get("pre_end_keyword"));
    }

    public static KeywordEndedEvent from(final Map<String, Object> eventMap) {
        return fromEventArguments((List<?>) eventMap.get("end_keyword"));
    }

    private static KeywordEndedEvent fromEventArguments(final List<?> arguments) {
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String libOrResourceName = (String) attributes.get("libname");
        final String keywordName = (String) attributes.get("kwname");
        final String keywordType = (String) attributes.get("type");
        final String status = (String) attributes.get("status");

        if (libOrResourceName == null || keywordName == null || keywordType == null || status == null) {
            throw new IllegalArgumentException(
                    "Keyword ended event should have name of keyword and library, keyword type and status");
        }
        return new KeywordEndedEvent(libOrResourceName, keywordName, keywordType, Status.valueOf(status));
    }

    private final String libOrResourceName;

    private final String name;

    private final String keywordType;

    private final Status status;

    public KeywordEndedEvent(final String libOrResourceName, final String name, final String keywordType,
            final Status status) {
        this.libOrResourceName = libOrResourceName;
        this.name = name;
        this.keywordType = keywordType;
        this.status = status;
    }

    public String getLibraryName() {
        return libOrResourceName;
    }

    public String getName() {
        return name;
    }

    public QualifiedKeywordName getQualifiedName() {
        return QualifiedKeywordName.create(name, libOrResourceName);
    }

    public String getKeywordType() {
        return keywordType;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == KeywordEndedEvent.class) {
            final KeywordEndedEvent that = (KeywordEndedEvent) obj;
            return this.libOrResourceName.equals(that.libOrResourceName) && this.name.equals(that.name)
                    && this.keywordType.equals(that.keywordType) && this.status == that.status;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(libOrResourceName, name, keywordType, status);
    }
}