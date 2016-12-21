/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
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
     * @return number of characters in definition which matches with given prefix
     */
    public static int startsWithIgnoreCase(final String definitionName, final String occurenceNamePrefix) {
        return startsWith(definitionName.toLowerCase(), occurenceNamePrefix.toLowerCase());
    }

    private static int startsWith(final String definitionName, final String occurenceNamePrefix) {
        if (definitionName.startsWith(occurenceNamePrefix)) {
            return occurenceNamePrefix.length();
        } else if (definitionName.indexOf('$') == -1) {
            return -1;
        }

        final RangeSet<Integer> varRanges = findEmbeddedArgumentsRanges(definitionName);

        int i = definitionName.length();
        while (i >= 0) {
            final String shortenedDefinition = definitionName.substring(0, i);
            final boolean matches = matchesIgnoreCase(shortenedDefinition, occurenceNamePrefix);
            if (matches) {
                return i;
            }

            i--;
            if (varRanges.contains(i)) {
                final Range<Integer> range = varRanges.rangeContaining(i);
                i = range.lowerEndpoint();
            }
        }
        return -1;
    }

    public static boolean matchesIgnoreCase(final String definitionName, final String occurrenceName) {
        if (definitionName.equalsIgnoreCase(occurrenceName)) {
            return true;
        } else if (definitionName.indexOf('$') == -1) {
            return false;
        }

        final String regex = "^" + substituteVariablesWithRegex(definitionName, true) + "$";
        try {
            return Pattern.matches(regex, occurrenceName.toLowerCase());
        } catch (final PatternSyntaxException e) {
            return false;
        }
    }

    private static String substituteVariablesWithRegex(final String definitionName, final boolean ignoreCase) {
        final StringBuilder wholeRegex = new StringBuilder();

        final RangeSet<Integer> varRanges = findEmbeddedArgumentsRanges(definitionName);

        StringBuilder exactWordPatternRegex = new StringBuilder();
        int i = 0;
        while (i < definitionName.length()) {
            if (varRanges.contains(i)) {
                if (exactWordPatternRegex.length() > 0) {
                    final String exactWordPattern = exactWordPatternRegex.toString();
                    wholeRegex.append(Pattern.quote(ignoreCase ? exactWordPattern.toLowerCase() : exactWordPattern));
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
            final String exactWordPattern = exactWordPatternRegex.toString();
            wholeRegex.append(Pattern.quote(ignoreCase ? exactWordPattern.toLowerCase() : exactWordPattern));
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
        int currentState = 0;

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

    public static Function<String, String> removeRegexFunction() {
        return new Function<String, String>() {

            @Override
            public String apply(final String variable) {
                return removeRegex(variable);
            }
        };
    }

    public static String removeRegex(final String variable) {
        return variable.indexOf(':') != -1 ? variable.substring(0, variable.indexOf(':')) + "}" : variable;
    }

    private interface KeywordDfaState {

        static final int START_STATE = 0;
        static final int VAR_DOLLAR_DETECTED = 1;
        static final int VAR_START_DETECTED = 2;
        static final int IN_VAR_ESCAPING = 3;
        static final int IN_VAR_NO_ESCAPE_CURRENTLY = 4;
    }
}
