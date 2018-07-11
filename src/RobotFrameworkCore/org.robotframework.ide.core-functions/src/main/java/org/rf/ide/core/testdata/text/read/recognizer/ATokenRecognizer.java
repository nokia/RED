/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.RobotVersion;

import com.google.common.annotations.VisibleForTesting;

public abstract class ATokenRecognizer {

    private final Pattern pattern;

    private Matcher matcher;

    private int lineNumber = -1;

    private int columnNumber = -1;

    private final RobotTokenType type;

    private String text;

    protected ATokenRecognizer(final Pattern pattern, final RobotTokenType type) {
        this.pattern = pattern;
        this.type = type;
    }

    public boolean isApplicableFor(@SuppressWarnings("unused") final RobotVersion robotVersion) {
        return true;
    }

    public abstract ATokenRecognizer newInstance();

    @VisibleForTesting
    public boolean hasNext(final StringBuilder newText, final int currentLineNumber, final int currentColumnNumber) {
        return hasNext(newText.toString(), currentLineNumber, currentColumnNumber);
    }

    public boolean hasNext(final String newText, final int currentLineNumber, final int currentColumnNumber) {
        if (matcher == null || lineNumber != currentLineNumber || !text.equals(newText)
                || columnNumber != currentColumnNumber) {
            this.matcher = pattern.matcher(newText);
            this.text = newText;
            this.lineNumber = currentLineNumber;
            this.columnNumber = currentColumnNumber;
        }
        return matcher.find();
    }

    public RobotToken next() {
        final RobotToken t = new RobotToken();
        t.setLineNumber(lineNumber);
        final int start = matcher.start();
        t.setStartColumn(start);
        final int end = matcher.end();

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
