/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.variables;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Strings;

public class CommonVariableHelper {

    private static final Pattern NAME = Pattern.compile("[{](.+?)[}]");

    private static final Pattern ASSIGN = Pattern.compile("(\\s)*((\\s*)[=])+(\\s)*$");

    public boolean isVariable(final RobotToken token) {
        final List<IRobotTokenType> types = token.getTypes();
        for (final IRobotTokenType type : types) {
            if (type == RobotTokenType.START_HASH_COMMENT || type == RobotTokenType.COMMENT_CONTINUE) {
                return false;
            } else if (type == RobotTokenType.VARIABLES_SCALAR_DECLARATION
                    || type == RobotTokenType.VARIABLES_SCALAR_AS_LIST_DECLARATION
                    || type == RobotTokenType.VARIABLES_LIST_DECLARATION
                    || type == RobotTokenType.VARIABLES_DICTIONARY_DECLARATION) {
                final String text = Strings.nullToEmpty(token.getText());
                if (!text.isEmpty()) {
                    final VariableType varType = VariableType.getTypeByTokenType(type);
                    return varType != null && text.startsWith(varType.getIdentificator());
                } else {
                    return false;
                }
            } else if (type == RobotTokenType.VARIABLE_USAGE) {
                final String text = Strings.nullToEmpty(token.getText()).trim();
                if (!text.isEmpty()) {
                    final VariableType varType = VariableType.getTypeByChar(text.charAt(0));
                    return varType != null && varType != VariableType.INVALID;
                }
            }
        }
        return false;
    }

    public void extractVariableAssignmentPart(final RobotLine line) {
        final List<IRobotLineElement> lineElements = line.getLineElements();
        for (int elementIndex = 0; elementIndex < lineElements.size(); elementIndex++) {
            final IRobotLineElement element = lineElements.get(elementIndex);
            if (element instanceof RobotToken) {
                final RobotToken token = (RobotToken) element;
                if (isVariable(token)) {
                    final String variableText = token.getText();
                    final RobotToken assignment = extractAssignmentPart(token.getFilePosition(), variableText);
                    if (assignment.isNotEmpty()) {
                        final String variable = variableText.substring(0,
                                assignment.getStartColumn() - token.getStartColumn());
                        token.setText(variable);
                        line.addLineElementAt(elementIndex + 1, assignment);
                        elementIndex++;
                    }
                }
            }
        }
    }

    private RobotToken extractAssignmentPart(final FilePosition startPosition, final String text) {
        final Matcher nameMatcher = NAME.matcher(text);
        if (nameMatcher.find()) {
            final int assignPart = nameMatcher.end();
            final String assignmentText = text.substring(assignPart);
            final Matcher assignMatcher = ASSIGN.matcher(assignmentText);
            if (assignMatcher.matches()) {
                final FilePosition filePosition = new FilePosition(startPosition.getLine(),
                        startPosition.getColumn() + assignPart, startPosition.getOffset() + assignPart);
                return RobotToken.create(assignmentText, filePosition, RobotTokenType.ASSIGNMENT);
            }
        }

        return new RobotToken();
    }

    public String extractVariableName(final String text) {
        String name = null;
        final Matcher matcher = NAME.matcher(text);
        if (matcher.find()) {
            name = matcher.group(1);
        }

        if (name != null) {
            name = mergeNotEscapedVariableWhitespaces(name);
        }

        return name != null ? name : "";
    }

    private String mergeNotEscapedVariableWhitespaces(final String text) {
        final StringBuilder replaced = new StringBuilder();

        boolean wasWhitespace = false;
        final char[] cText = text.toCharArray();
        for (final char c : cText) {
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

    public boolean matchesBracketsConditionsForCorrectVariable(final String text) {
        if (countNumberOfChars(text, '{') == 1 && countNumberOfChars(text, '}') == 1) {
            final String trimmed = text.trim().replaceAll("[}]\\s*[=]+", "}");
            if (trimmed.endsWith("}")) {
                return true;
            }
        }

        return false;
    }

    private int countNumberOfChars(final String text, final char expected) {
        int count = 0;
        final char[] cArray = text.toCharArray();
        for (final char c : cArray) {
            if (c == expected) {
                count++;
            }
        }

        return count;
    }
}
