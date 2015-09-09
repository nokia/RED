/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class ATokenRecognizer {

    private final Pattern pattern;
    private Matcher m;
    private int lineNumber = -1;
    private final RobotTokenType type;
    private StringBuilder text;


    protected ATokenRecognizer(final Pattern p, final RobotTokenType type) {
        this.pattern = p;
        this.type = type;
    }


    public boolean hasNext(StringBuilder text, int lineNumber) {
        this.text = text;
        this.lineNumber = lineNumber;
        if (m == null || isTextDifferent(text)) {
            m = pattern.matcher(text);
        }
        return m.find();
    }


    private boolean isTextDifferent(final StringBuilder text) {
        boolean result = false;
        if (this.text == null) {
            result = true;
        } else {
            result = this.text.toString().equals(text.toString());
        }

        return result;
    }


    public RobotToken next() {
        RobotToken t = new RobotToken();
        t.setLineNumber(lineNumber);
        int start = m.start();
        t.setStartColumn(start);
        int end = m.end();

        t.setText(new StringBuilder().append(text.substring(start, end)));
        t.setRaw(new StringBuilder().append(t.getText()));
        t.setType(getProducedType());
        return t;
    }


    public RobotTokenType getProducedType() {
        return type;
    }


    public static String createUpperLowerCaseWord(final String text) {
        StringBuilder str = new StringBuilder();
        if (text != null && text.length() > 0) {

            char[] ca = text.toCharArray();
            int size = ca.length;
            for (int i = 0; i < size; i++) {
                str.append('[');
                char c = ca[i];
                if (Character.isLetter(c)) {
                    str.append(Character.toUpperCase(c)).append('|')
                            .append(Character.toLowerCase(c));
                } else {
                    str.append(c);
                }

                str.append(']');
            }

        }
        return str.toString();
    }


    public Pattern getPattern() {
        return this.pattern;
    }
}
