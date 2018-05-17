/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;

public abstract class ATokenRecognizer {

    private final Pattern pattern;

    private Matcher m;

    private int lineNumber = -1;

    private int columnNumber = -1;

    private final RobotTokenType type;

    private String text;

    protected ATokenRecognizer(final Pattern p, final RobotTokenType type) {
        this.pattern = p;
        this.type = type;
    }

    public abstract ATokenRecognizer newInstance();

    @VisibleForTesting
    public boolean hasNext(final StringBuilder newText, final int currentLineNumber, final int currentColumnNumber) {
        return hasNext(newText.toString(), currentLineNumber, currentColumnNumber);
    }

    public boolean hasNext(final String newText, final int currentLineNumber, final int currentColumnNumber) {
        if (m == null || lineNumber != currentLineNumber || !text.equals(newText)
                || columnNumber != currentColumnNumber) {
            m = pattern.matcher(newText);
            this.text = newText;
            this.lineNumber = currentLineNumber;
            this.columnNumber = currentColumnNumber;
        }

        return m.find();
    }

    public RobotToken next() {
        final RobotToken t = new RobotToken();
        t.setLineNumber(lineNumber);
        final int start = m.start();
        t.setStartColumn(start);
        final int end = m.end();

        t.setText(text.substring(start, end));
        t.setType(getProducedType());
        return t;
    }

    public RobotTokenType getProducedType() {
        return type;
    }

    public static String createUpperLowerCaseWordWithOptionalSpaceInside(final String text) {
        return createUpperLowerCaseWordWithPatternBetweenLetters(text, "[\\s]?");
    }

    public static String createUpperLowerCaseWordWithSpacesInside(final String text) {
        return createUpperLowerCaseWordWithPatternBetweenLetters(text, "[\\s]*");
    }

    public static String createUpperLowerCaseWord(final String text) {
        return createUpperLowerCaseWordWithPatternBetweenLetters(text, null);
    }

    private static String createUpperLowerCaseWordWithPatternBetweenLetters(final String text,
            final String patternBetweenChars) {
        final StringBuilder str = new StringBuilder();
        if (text != null && text.length() > 0) {

            final char[] ca = text.toCharArray();
            final int size = ca.length;
            for (int i = 0; i < size; i++) {

                str.append('[');
                final char c = ca[i];
                if (Character.isLetter(c)) {
                    str.append(Character.toUpperCase(c)).append('|').append(Character.toLowerCase(c));
                } else {
                    str.append(c);
                }
                str.append(']');

                if (patternBetweenChars != null && i + 1 < size) {
                    str.append(patternBetweenChars);
                }
            }
        }
        return str.toString();
    }

    public Pattern getPattern() {
        return this.pattern;
    }
}
