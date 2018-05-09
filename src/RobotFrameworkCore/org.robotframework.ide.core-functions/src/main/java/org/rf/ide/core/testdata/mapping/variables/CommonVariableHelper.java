/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.variables;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class CommonVariableHelper {

    private static final Pattern NAME = Pattern.compile("[{](.+?)[}]");

    private static final Pattern ASSIGN = Pattern.compile("(\\s)*((\\s*)[=])+(\\s)*$");

    private final ParsingStateHelper stateHelper;

    public CommonVariableHelper() {
        this.stateHelper = new ParsingStateHelper();
    }

    public void extractVariableAssignmentPart(final RobotLine line, final Stack<ParsingState> state) {
        if (stateHelper.getCurrentStatus(state) != ParsingState.KEYWORD_SETTING_ARGUMENTS_ARGUMENT_VALUE) {
            final List<IRobotLineElement> lineElements = line.getLineElements();
            final boolean wasNotVariableElement = false;
            for (int elementIndex = 0; elementIndex < lineElements.size(); elementIndex++) {
                final IRobotLineElement element = lineElements.get(elementIndex);
                if (element instanceof RobotToken) {
                    final RobotToken token = (RobotToken) element;
                    if (isVariable(token) && !wasNotVariableElement) {
                        String variableText = token.getRaw();
                        if (variableText != null
                                && !variableText.contains(RobotTokenType.ASSIGNMENT.getRepresentation().get(0))) {
                            variableText = token.getText();
                        }
                        final RobotToken assignment = extractAssignmentPart(token.getFilePosition(), variableText);
                        if (assignment.isNotEmpty()) {
                            final String variable = variableText.substring(0,
                                    assignment.getStartColumn() - token.getStartColumn());
                            token.setText(variable);
                            token.setRaw(variable);
                            line.addLineElementAt(elementIndex + 1, assignment);
                            elementIndex++;
                        }
                    }
                }
            }
        }
    }

    public boolean isVariable(final RobotToken token) {
        boolean isVar = false;
        final List<IRobotTokenType> types = token.getTypes();
        for (final IRobotTokenType type : types) {
            if (type == RobotTokenType.START_HASH_COMMENT || type == RobotTokenType.COMMENT_CONTINUE) {
                isVar = false;
                break;
            } else if (type == RobotTokenType.VARIABLES_SCALAR_DECLARATION
                    || type == RobotTokenType.VARIABLES_SCALAR_AS_LIST_DECLARATION
                    || type == RobotTokenType.VARIABLES_LIST_DECLARATION
                    || type == RobotTokenType.VARIABLES_DICTIONARY_DECLARATION) {
                final String text = token.getText().toString();
                if (text != null && !text.isEmpty()) {
                    final VariableType varType = VariableType.getTypeByTokenType(type);
                    if (varType != null) {
                        isVar = (text.startsWith(varType.getIdentificator()));
                    } else {
                        isVar = false;
                    }
                } else {
                    isVar = false;
                }
                break;
            } else if (type == RobotTokenType.VARIABLE_USAGE) {
                isVar = false;
                final String text = token.getText().toString();
                if (text != null) {
                    final String trimmed = text.trim();

                    final VariableType varType = VariableType.getTypeByChar(trimmed.charAt(0));
                    isVar = (varType != VariableType.INVALID);
                }
                break;
            }
        }
        return isVar;
    }

    private RobotToken extractAssignmentPart(final FilePosition startPosition, final String text) {
        final RobotToken assignToken = new RobotToken();

        final Matcher matcher = NAME.matcher(text);
        if (matcher.find()) {
            final int assignPart = matcher.end();
            final String assignmentText = text.substring(assignPart);
            final Matcher assignMatcher = ASSIGN.matcher(assignmentText);
            if (assignMatcher.find()) {
                assignToken.setLineNumber(startPosition.getLine());
                assignToken.setStartColumn(startPosition.getColumn() + assignPart);
                assignToken.setStartOffset(startPosition.getOffset() + assignPart);
                assignToken.setText(assignmentText);
                assignToken.setRaw(assignmentText);
                assignToken.setType(RobotTokenType.ASSIGNMENT);
            }
        }

        return assignToken;
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

        return (name != null) ? name : "";
    }

    @VisibleForTesting
    protected String mergeNotEscapedVariableWhitespaces(final String text) {
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

    @VisibleForTesting
    protected int countNumberOfChars(final String text, final char expected) {
        int count = 0;
        final char[] cArray = text.toCharArray();
        for (final char c : cArray) {
            if (c == expected) {
                count++;
            }
        }

        return count;
    }

    public boolean isIncludedInVariableTable(final RobotLine line, final Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            result = (processingState.get(processingState.size() - 1) == ParsingState.VARIABLE_TABLE_INSIDE);
        } else {
            result = false;
        }

        return result;
    }
}
