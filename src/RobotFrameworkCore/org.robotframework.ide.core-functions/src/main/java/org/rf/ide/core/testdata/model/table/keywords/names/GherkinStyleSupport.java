/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class GherkinStyleSupport {

    private static final Set<String> PREFIXES = newHashSet("given", "when", "and", "but", "then");

    public static <T> Optional<T> firstNameTransformationResult(final String originalName,
            final NameTransformation<T> transformation) {
        String current = originalName;
        String gherkinFree = GherkinStyleSupport.removeGherkinPrefix(originalName);

        Optional<T> transformed = transformation.transform(current);
        while (!transformed.isPresent() && !gherkinFree.equals(current)) {
            current = gherkinFree;
            gherkinFree = GherkinStyleSupport.removeGherkinPrefix(current);

            transformed = transformation.transform(current);
        }
        return transformed;
    }

    public static void forEachPossibleGherkinName(final String originalName, final NameOperation operation) {
        String current = originalName;
        String gherkinFree = GherkinStyleSupport.removeGherkinPrefix(originalName);

        operation.perform(current);
        while (!gherkinFree.equals(current)) {
            current = gherkinFree;
            gherkinFree = GherkinStyleSupport.removeGherkinPrefix(current);

            operation.perform(current);
        }
    }
    
    public static String getTextAfterGherkinPrefixIfExists(final String text) {
        for (final String prefix : PREFIXES) {
            if (text.toLowerCase().startsWith(prefix)) {
                final String textAfterPrefix = text.substring(prefix.length());
                if (!textAfterPrefix.isEmpty()) { 
                    return textAfterPrefix.trim();
                } else {
                    return text; // if no any text after gherkin prefix, return gherkin prefix
                }
            }
        }
        return text; 
    }

    private static String removeGherkinPrefix(final String name) {
        for (final String prefix : PREFIXES) {
            if (name.toLowerCase().startsWith(prefix)) {
                return name.substring(prefix.length()).trim();
            }
        }
        return name;
    }

    public interface NameOperation {
        void perform(String gherkinNameVariant);
    }

    
    public interface NameTransformation<T> {

        Optional<T> transform(String gherkinNameVariant);
    }
}
