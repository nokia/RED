/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.variables.mapping;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class CommonVariableHelper {

    private static final Pattern NAME = Pattern.compile("[{](.+?)[}]");
    private static final Pattern ASSIGN = Pattern
            .compile("((\\s)*[=]+)+(\\s)*$");


    public void extractVariableAssignmentPart(RobotLine line) {
        List<IRobotLineElement> lineElements = line.getLineElements();
        boolean wasNotVariableElement = false;
        for (int elementIndex = 0; elementIndex < lineElements.size(); elementIndex++) {
            IRobotLineElement element = lineElements.get(elementIndex);
            if (element instanceof RobotToken) {
                RobotToken token = (RobotToken) element;
                if (isVariable(token) && !wasNotVariableElement) {
                    String variableText = token.getRaw().toString();
                    RobotToken assignment = extractAssignmentPart(
                            token.getFilePosition(), variableText);
                    if (!assignment.getFilePosition().isNotSet()) {
                        final String variable = variableText.substring(
                                0,
                                assignment.getStartColumn()
                                        - token.getStartColumn());
                        token.setRaw(new StringBuilder(variable));
                        token.setText(new StringBuilder(variable));
                        line.addLineElementAt(elementIndex + 1, assignment);
                        elementIndex++;
                    }
                } else {
                    wasNotVariableElement = true;
                }
            }
        }
    }


    public boolean isVariable(final RobotToken token) {
        boolean isVar = false;
        List<IRobotTokenType> types = token.getTypes();
        for (IRobotTokenType type : types) {
            if (type == RobotTokenType.START_HASH_COMMENT
                    || type == RobotTokenType.COMMENT_CONTINUE) {
                isVar = false;
                break;
            } else if (type == RobotTokenType.VARIABLES_SCALAR_DECLARATION
                    || type == RobotTokenType.VARIABLES_SCALAR_AS_LIST_DECLARATION
                    || type == RobotTokenType.VARIABLES_LIST_DECLARATION
                    || type == RobotTokenType.VARIABLES_DICTIONARY_DECLARATION) {
                String text = token.getText().toString();
                if (text != null && !text.isEmpty()) {
                    VariableType varType = VariableType
                            .getTypeByTokenType(type);
                    if (varType != null) {
                        isVar = (text.startsWith(varType.getIdentificator()));
                    } else {
                        isVar = false;
                    }
                } else {
                    isVar = false;
                }
                break;
            }
        }
        return isVar;
    }


    private RobotToken extractAssignmentPart(final FilePosition startPossition,
            final String text) {
        RobotToken assignToken = new RobotToken();

        Matcher matcher = NAME.matcher(text);
        if (matcher.find()) {
            int assignPart = matcher.end();
            String assignmentText = text.substring(assignPart);
            Matcher assignMatcher = ASSIGN.matcher(assignmentText);
            if (assignMatcher.find()) {
                assignToken.setLineNumber(startPossition.getLine());
                assignToken.setStartColumn(startPossition.getColumn()
                        + assignPart);
                assignToken.setStartOffset(startPossition.getOffset()
                        + assignPart);
                assignToken.setRaw(new StringBuilder(assignmentText));
                assignToken.setText(new StringBuilder(assignmentText));
                assignToken.setType(RobotTokenType.ASSIGNMENT);
            }
        }

        return assignToken;
    }


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
