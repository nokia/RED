/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * @author Michal Anglart
 */
public class EmbeddedKeywordNamesSupport {

    public static boolean hasEmbeddedArguments(final String definitionName) {
        return !findEmbeddedArgumentsRanges(definitionName).isEmpty();
    }

    /**
     * @return an Optional describing first range in definition which matches with given occurrence,
     *         or an empty Optional if no match or only embedded variable matches
     */
    public static Optional<Range<Integer>> containsIgnoreCase(final String definitionName,
            final String occurrenceName) {
        final int occurrenceIndex = definitionName.toLowerCase().indexOf(occurrenceName.toLowerCase());
        if (occurrenceIndex >= 0) {
            return Optional.of(Range.closedOpen(occurrenceIndex, occurrenceIndex + occurrenceName.length()));
        } else if (definitionName.indexOf('$') == -1) {
            return Optional.empty();
        }

        final RangeSet<Integer> varRanges = findEmbeddedArgumentsRanges(definitionName);

        int lowerIndex = 0;
        while (lowerIndex < definitionName.length()) {
            int upperIndex = definitionName.length();
            while (lowerIndex <= upperIndex) {
                if (lowerIndex < upperIndex && varRanges.encloses(Range.closedOpen(lowerIndex, upperIndex - 1))) {
                    // we do not want to match variable only
                    return Optional.empty();
                }

                final String shortenedDefinition = definitionName.substring(lowerIndex, upperIndex);
                if (matchesIgnoreCase(shortenedDefinition, occurrenceName)) {
                    return Optional.of(Range.closedOpen(lowerIndex, upperIndex));
                }

                upperIndex--;
                if (varRanges.contains(upperIndex)) {
                    final Range<Integer> range = varRanges.rangeContaining(upperIndex);
                    upperIndex = range.lowerEndpoint();
                }
            }
            lowerIndex++;
        }

        return Optional.empty();
    }

    public static boolean matchesIgnoreCase(final String definitionName, final String occurrenceName) {
        if (definitionName.equalsIgnoreCase(occurrenceName)) {
            return true;
        } else if (definitionName.indexOf('$') == -1) {
            return false;
        }

        final String regex = "(?iu)^" + substituteVariablesWithRegex(definitionName) + "$";
        try {
            return Pattern.matches(regex, occurrenceName);
        } catch (final PatternSyntaxException e) {
            return false;
        }
    }

    private static String substituteVariablesWithRegex(final String definitionName) {
        final StringBuilder wholeRegex = new StringBuilder();

        final RangeSet<Integer> varRanges = findEmbeddedArgumentsRanges(definitionName);

        StringBuilder exactWordPatternRegex = new StringBuilder();
        int i = 0;
        while (i < definitionName.length()) {
            if (varRanges.contains(i)) {
                if (exactWordPatternRegex.length() > 0) {
                    wholeRegex.append(Pattern.quote(exactWordPatternRegex.toString()));
                    exactWordPatternRegex = new StringBuilder();
                }

                final Range<Integer> varRange = varRanges.rangeContaining(i);
                final String internalRegex = getEmbeddedArgumentRegex(definitionName, varRange);
                wholeRegex.append(internalRegex);
                i = varRange.upperEndpoint() + 1;
            } else {
                exactWordPatternRegex.append(definitionName.charAt(i));
                i++;
            }
        }
        if (exactWordPatternRegex.length() > 0) {
            wholeRegex.append(Pattern.quote(exactWordPatternRegex.toString()));
        }
        return wholeRegex.toString();
    }

    private static String getEmbeddedArgumentRegex(final String definitionName, final Range<Integer> varRange) {
        final String varContent = definitionName.substring(varRange.lowerEndpoint() + 2, varRange.upperEndpoint());
        final String unescapedRegex = varContent.indexOf(':') != -1 ? varContent.substring(varContent.indexOf(':') + 1)
                : ".+";

        boolean isEscaped = false;
        final StringBuilder escapedRegex = new StringBuilder();
        for (int i = 0; i < unescapedRegex.length(); i++) {
            final char currentChar = unescapedRegex.charAt(i);
            if (!isEscaped && currentChar == '\\') {
                isEscaped = true;
                continue;
            }

            if (isEscaped && currentChar != '\\' && currentChar != '}') {
                escapedRegex.append('\\');
            }
            escapedRegex.append(currentChar);
            isEscaped = false;
        }
        return escapedRegex.toString();
    }

    @VisibleForTesting
    static RangeSet<Integer> findEmbeddedArgumentsRanges(final String definitionName) {
        final RangeSet<Integer> varRanges = TreeRangeSet.create();

        if (definitionName.isEmpty()) {
            return varRanges;
        }
        int varStart = -1;
        int currentIndex = 0;
        KeywordDfaState currentState = KeywordDfaState.START_STATE;

        while (currentIndex < definitionName.length()) {
            final char character = definitionName.charAt(currentIndex);

            if (currentState == KeywordDfaState.START_STATE) {
                currentState = character == '$' ? KeywordDfaState.VAR_DOLLAR_DETECTED : KeywordDfaState.START_STATE;

            } else if (currentState == KeywordDfaState.VAR_DOLLAR_DETECTED) {
                currentState = character == '{' ? KeywordDfaState.VAR_START_DETECTED : KeywordDfaState.START_STATE;

            } else if (currentState == KeywordDfaState.VAR_START_DETECTED) {
                varStart = currentIndex - 2;
                if (character == '}') {
                    currentState = KeywordDfaState.START_STATE;
                } else if (character == '\\') {
                    currentState = KeywordDfaState.IN_VAR_ESCAPING;
                } else {
                    currentState = KeywordDfaState.IN_VAR_NO_ESCAPE_CURRENTLY;
                }

            } else if (currentState == KeywordDfaState.IN_VAR_ESCAPING) {
                currentState = KeywordDfaState.IN_VAR_NO_ESCAPE_CURRENTLY;

            } else if (currentState == KeywordDfaState.IN_VAR_NO_ESCAPE_CURRENTLY) {
                if (character == '}') {
                    varRanges.add(Range.closed(varStart, currentIndex));
                    currentState = KeywordDfaState.START_STATE;
                } else if (character == '\\') {
                    currentState = KeywordDfaState.IN_VAR_ESCAPING;
                } else {
                    currentState = KeywordDfaState.IN_VAR_NO_ESCAPE_CURRENTLY;
                }

            } else {
                throw new IllegalStateException("Unrecognized state");
            }
            currentIndex++;
        }
        return varRanges;
    }

    public static String removeRegex(final String variable) {
        return variable.indexOf(':') != -1 ? variable.substring(0, variable.indexOf(':')) + "}" : variable;
    }

    private enum KeywordDfaState {
        START_STATE,
        VAR_DOLLAR_DETECTED,
        VAR_START_DETECTED,
        IN_VAR_ESCAPING,
        IN_VAR_NO_ESCAPE_CURRENTLY;
    }
}
