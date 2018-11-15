/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import static java.util.stream.Collectors.joining;

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

    public RobotTokenType getProducedType() {
        return type;
    }

    public Pattern getPattern() {
        return pattern;
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
        final int start = matcher.start();
        final int end = matcher.end();

        return RobotToken.create(text.substring(start, end), lineNumber, start, type);
    }

    public static String createUpperLowerCaseWordWithOptionalSpaceInside(final String text) {
        return createUpperLowerCaseWordWithPatternBetweenLetters(text, "[\\s]?");
    }

    public static String createUpperLowerCaseWordWithSpacesInside(final String text) {
        return createUpperLowerCaseWordWithPatternBetweenLetters(text, "[\\s]*");
    }

    public static String createUpperLowerCaseWord(final String text) {
        return createUpperLowerCaseWordWithPatternBetweenLetters(text, "");
    }

    private static String createUpperLowerCaseWordWithPatternBetweenLetters(final String text,
            final String patternBetweenChars) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.chars().mapToObj(c -> createUpperLowerVariant((char) c)).collect(joining(patternBetweenChars));
    }

    private static String createUpperLowerVariant(final char c) {
        return Character.isLetter(c)
                ? "[" + Character.toUpperCase(c) + Character.toLowerCase(c) + "]"
                : Character.toString(c);
    }
}
