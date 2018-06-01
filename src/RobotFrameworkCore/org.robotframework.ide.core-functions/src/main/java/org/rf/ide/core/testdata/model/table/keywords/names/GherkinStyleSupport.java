/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Michal Anglart
 */
public class GherkinStyleSupport {

    public static final Set<String> PREFIXES = Collections
            .unmodifiableSet(newHashSet("given", "when", "and", "but", "then"));

    public static <T> Optional<T> firstNameTransformationResult(final String originalName,
            final Function<String, Optional<T>> transformation) {
        String current = originalName;
        String gherkinFree = GherkinStyleSupport.removeGherkinPrefix(originalName);

        Optional<T> transformed = transformation.apply(current);
        while (!transformed.isPresent() && !gherkinFree.equals(current)) {
            current = gherkinFree;
            gherkinFree = GherkinStyleSupport.removeGherkinPrefix(current);

            transformed = transformation.apply(current);
        }
        return transformed;
    }

    public static void forEachPossibleGherkinName(final String originalName, final Consumer<String> operation) {
        String current = originalName;
        String gherkinFree = GherkinStyleSupport.removeGherkinPrefix(originalName);

        operation.accept(current);
        while (!gherkinFree.equals(current)) {
            current = gherkinFree;
            gherkinFree = GherkinStyleSupport.removeGherkinPrefix(current);

            operation.accept(current);
        }
    }

    public static String getTextAfterGherkinPrefixesIfExists(final String originalName) {
        String current = originalName;
        String gherkinFree = GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName);

        while (!gherkinFree.equals(current)) {
            current = gherkinFree;
            gherkinFree = GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(current);
        }
        return gherkinFree;
    }

    public static String getTextAfterGherkinPrefixIfExists(final String originalName) {
        final String textAfterPrefix = GherkinStyleSupport.removeGherkinPrefix(originalName);
        if (textAfterPrefix.isEmpty()) {
            return originalName; // if no any text after gherkin prefix, return gherkin prefix
        }
        return textAfterPrefix;
    }

    private static String removeGherkinPrefix(final String name) {
        for (final String prefix : PREFIXES) {
            if (name.toLowerCase().startsWith(prefix)) {
                final String suffix = name.substring(prefix.length());
                final String trimmedSuffix = suffix.trim();
                if (suffix.equals(trimmedSuffix)) {
                    continue;
                }
                return trimmedSuffix;
            }
        }
        return name;
    }
}
