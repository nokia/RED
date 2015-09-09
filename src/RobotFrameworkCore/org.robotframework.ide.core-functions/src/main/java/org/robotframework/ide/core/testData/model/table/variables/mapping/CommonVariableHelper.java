/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.variables.mapping;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;

import com.google.common.annotations.VisibleForTesting;


public class CommonVariableHelper {

    private static final Pattern NAME = Pattern.compile("[{](.+?)[}]");


    public String extractVariableName(String text) {
        String name = null;
        Matcher matcher = NAME.matcher(text);
        if (matcher.find()) {
            name = matcher.group(1);
        }

        if (name != null) {
            name = mergeNotEscapedVariableWhitespaces(name);
        }

        return (name != null) ? name : "";
    }


    @VisibleForTesting
    protected String mergeNotEscapedVariableWhitespaces(String text) {
        StringBuilder replaced = new StringBuilder();

        boolean wasWhitespace = false;
        char cText[] = text.toCharArray();
        for (char c : cText) {
            char toUse = c;
            if (c == '\t') {
                toUse = ' ';
            }

            if (toUse == ' ') {
                if (!wasWhitespace) {
                    replaced.append(toUse);
                    wasWhitespace = true;
                }
            } else {
                replaced.append(toUse);
                wasWhitespace = false;
            }
        }

        return replaced.toString();
    }


    public boolean isCorrectVariable(String text) {
        return (countNumberOfChars(text, '{') == 1 && countNumberOfChars(text,
                '}') == 1);
    }


    @VisibleForTesting
    protected int countNumberOfChars(String text, char expected) {
        int count = 0;
        char[] cArray = text.toCharArray();
        for (char c : cArray) {
            if (c == expected) {
                count++;
            }
        }

        return count;
    }


    public boolean isIncludedInVariableTable(final RobotLine line,
            final Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            result = (processingState.get(processingState.size() - 1) == ParsingState.VARIABLE_TABLE_INSIDE);
        } else {
            result = false;
        }

        return result;
    }
}
