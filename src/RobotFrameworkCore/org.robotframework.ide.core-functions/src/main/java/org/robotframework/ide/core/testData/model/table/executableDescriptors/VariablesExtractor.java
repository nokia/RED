/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.LinkedListMultimap;


public class VariablesExtractor {

    private static final String ESCAPED_AND_NORMAL_VARIABLES_PATTERN = "(\\\\)*([$]|[@]|[&]|[%])"
            + "(\\s)*[{]((?!\\[)(?!([$]|[@]|[&]|[%]))(?![}]).)*";
    private static final Pattern ANY_VARIABLE_PATTERN = Pattern
            .compile(ESCAPED_AND_NORMAL_VARIABLES_PATTERN);


    @VisibleForTesting
    protected LinkedListMultimap<Character, Integer> findCharsPositions(
            final String text, List<Character> charsToFind) {
        LinkedListMultimap<Character, Integer> charToPosition = LinkedListMultimap
                .create();
        if (text != null && !charsToFind.isEmpty()) {
            char[] textChars = text.toCharArray();
            int textLength = textChars.length;
            for (int charIndex = 0; charIndex < textLength; charIndex++) {
                char c = textChars[charIndex];
                if (charsToFind.contains(c)) {
                    charToPosition.put(c, charIndex);
                }
            }
        }

        return charToPosition;
    }


    @VisibleForTesting
    protected List<TextPosition> extractPossibleVariablePositions(
            final String robotArgument) {
        List<TextPosition> positions = new LinkedList<>();
        Matcher matcher = ANY_VARIABLE_PATTERN.matcher(robotArgument);
        while(matcher.find()) {
            positions.add(new TextPosition(matcher.start(), matcher.end()));
        }

        return positions;
    }

    @VisibleForTesting
    protected class TextPosition {

        private final int start;
        private final int end;


        public TextPosition(final int start, final int end) {
            this.start = start;
            this.end = end;
        }


        public int getStart() {
            return start;
        }


        public int getEnd() {
            return end;
        }
    }
}
