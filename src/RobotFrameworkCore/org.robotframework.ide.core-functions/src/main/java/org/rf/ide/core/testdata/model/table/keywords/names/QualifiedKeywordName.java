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
    
    private final String embeddedName;

    private QualifiedKeywordName(final String name, final String source) {
        this(name, null, source);
    }
    
    private QualifiedKeywordName(final String name, final String embeddedName, final String source) {
        this.name = name;
        this.source = source;
        this.embeddedName = embeddedName;
    }

    public static QualifiedKeywordName create(final String name, final String sourceName) {
        return new QualifiedKeywordName(unifyDefinition(name), sourceName);
    }

    public static QualifiedKeywordName fromOccurrence(final String givenWholeName) {
        final List<String> splitted = Splitter.on('.').splitToList(givenWholeName);
        final String name = splitted.get(splitted.size() - 1).trim();
        final String source = Joiner.on('.').join(splitted.subList(0, splitted.size() - 1)).trim();
        return new QualifiedKeywordName(unifyDefinition(name), name.toLowerCase(), source);
    }
    
    public static QualifiedKeywordName fromOccurrenceWithDots(final String givenWholeName) { 
        // ignore keyword source
        return new QualifiedKeywordName(unifyDefinition(givenWholeName), givenWholeName.toLowerCase(), "");
    }

    public static String unifyDefinition(final String keywordDefinition) {
        if (keywordDefinition != null) {
            return EmbeddedKeywordNamesSupport.hasEmbeddedArguments(keywordDefinition) ? keywordDefinition.toLowerCase()
                    : keywordDefinition.toLowerCase().replaceAll("_", "").replaceAll(" ", "");
        }
        return "";
    }
    
    public static boolean isOccurrenceEqualToDefinition(final String keywordOccurrence, final String keywordDefinition) {
        if(EmbeddedKeywordNamesSupport.hasEmbeddedArguments(keywordDefinition)) { // ignore embedded keyword names 
            return true;
        }
        final List<String> splittedOccurrence = Splitter.on('.').splitToList(keywordOccurrence);
        final String nameInOccurrence = splittedOccurrence.get(splittedOccurrence.size() - 1).trim();
        return nameInOccurrence.equalsIgnoreCase(keywordDefinition);
    }

    public String getKeywordName() {
        return name;
    }
    
    public String getEmbeddedKeywordName() {
        return embeddedName;
    }

    public String getKeywordSource() {
        return source;
    }

    public QualifiedKeywordName toLowerCase() {
        return new QualifiedKeywordName(name.toLowerCase(), embeddedName.toLowerCase(), source.toLowerCase());
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