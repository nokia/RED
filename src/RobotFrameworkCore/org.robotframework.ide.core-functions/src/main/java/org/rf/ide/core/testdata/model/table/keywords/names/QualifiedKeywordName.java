/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;

public final class QualifiedKeywordName {

    private final String name;

    private final String source;

    private QualifiedKeywordName(final String name, final String source) {
        this.name = name;
        this.source = source;
    }

    public static QualifiedKeywordName create(final String name, final String sourceName) {
        return new QualifiedKeywordName(name, sourceName);
    }

    public static QualifiedKeywordName from(final String wholeName) {
        final List<String> splitted = Splitter.on('.').splitToList(wholeName);
        final String name = splitted.get(splitted.size() - 1).trim();
        final String source = Joiner.on('.').join(splitted.subList(0, splitted.size() - 1)).trim();
        return new QualifiedKeywordName(name, source);
    }


    public String getKeywordName() {
        return name;
    }

    public String getKeywordSource() {
        return source;
    }

    public boolean matchesIgnoringCase(final QualifiedKeywordName that) {
        if (source.isEmpty()) {
            return this.name.equalsIgnoreCase(that.name);
        }
        return this.name.equalsIgnoreCase(that.name) && this.source.equalsIgnoreCase(that.source);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == QualifiedKeywordName.class) {
            final QualifiedKeywordName that = (QualifiedKeywordName) obj;
            return Objects.equal(this.name, that.name) && Objects.equal(this.source, that.source);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, source);
    }
}