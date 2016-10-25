/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import java.util.Stack;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration.GeneralVariableType;

import com.google.common.base.Optional;

public class VariableComputationHelper {

    private final static String NUMBER_PATTERN_TXT = "([+]|[-])*\\d+([.]\\d+)?\\s*";

    private final static String COUNT_OPERATIONS = "([+]|[-]|[*]|[/]|[:]|[>]|[<]|[=]|[&]|[%]|\\^|\\!|[|])";

    private final static Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_PATTERN_TXT);

    private final static Pattern COUNT_OPERATION_PATTERN = Pattern.compile(COUNT_OPERATIONS);

    private final static Pattern ILLEGAL_BRACKET_SYNTAX = Pattern
            .compile("\\s*((?!" + COUNT_OPERATIONS + "| (\\[|\\()).)\\s*(\\[|\\()");

    private final static Pattern NUMBER_OPERATION = Pattern
            .compile("^" + NUMBER_PATTERN + "(" + COUNT_OPERATIONS + NUMBER_PATTERN + ")*");

    private final static String QUOTA_TEXT = "[\"](([\\\\][\"])|((?![\"]).))*[\"]";

    private final static Pattern TEXT_OPERATION = Pattern
            .compile("^((\\s*(" + COUNT_OPERATIONS + ")+\\s*(" + NUMBER_PATTERN + "|" + QUOTA_TEXT + ")+\\s*)+)*");

    public Optional<TextPosition> extractVariableName(final VariableDeclaration variableDec) {
        Optional<TextPosition> text = Optional.absent();

        if (variableDec.getVariableType() == GeneralVariableType.COMPUTATION) {
            final TextPosition textPositionVariableName = variableDec.getVariableName();
            final String variableName = textPositionVariableName.getText().trim();
            if (!variableName.isEmpty()) {
                if (!variableName.startsWith("(") && !variableName.startsWith("\"") && !variableName.startsWith("[")) {
                    if (!ILLEGAL_BRACKET_SYNTAX.matcher(variableName).find()) {
                        if (startsFromNumber(variableName)) {
                            if (isNumberComputation(variableName)) {
                                return Optional.of(new TextPosition("" + 3, 0, 1));
                            }
                        } else if (startsWithVariableName(variableName)) {
                            final String variable = getVariableName(variableName);
                            if (variable != null) {
                                final int startIndex = textPositionVariableName.getFullText().indexOf(variable);

                                if (startIndex >= 0) {
                                    if (isPropertTextOperation(variableName, variable)) {
                                        return Optional.of(new TextPosition(textPositionVariableName.getFullText(),
                                                startIndex, startIndex + variable.length() - 1));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    final int startIndex = textPositionVariableName.getFullText().indexOf(variableName.charAt(0));
                    return Optional
                            .of(new TextPosition(textPositionVariableName.getFullText(), startIndex, startIndex));
                }
            }
        }

        return text;
    }

    private static boolean isPropertTextOperation(final String expression, final String variable) {
        final String restOperationText = expression.substring(variable.length()).trim();
        final String withoutBrackets = validateAndRemoveBrackets(restOperationText);
        if (withoutBrackets != null) {
            return TEXT_OPERATION.matcher(withoutBrackets).matches();
        }

        return false;
    }

    public static void main(String[] args) {
        System.out.println(TEXT_OPERATION.matcher("+ \"msg\" * 2").matches());
    }

    private String getVariableName(final String variableName) {
        String[] split = COUNT_OPERATION_PATTERN.split(variableName);
        if (split.length >= 2) {
            return split[0].trim();
        }

        return null;
    }

    private boolean startsFromNumber(final String expression) {
        return NUMBER_PATTERN.matcher(expression).matches();
    }

    private boolean startsWithVariableName(final String expression) {
        return !startsFromNumber(expression);
    }

    private boolean isNumberComputation(final String expression) {
        final String bracketRemoved = validateAndRemoveBrackets(expression);
        if (bracketRemoved != null) {
            return NUMBER_OPERATION.matcher(bracketRemoved).matches();
        }

        return false;
    }

    private static String validateAndRemoveBrackets(final String text) {
        final char[] chars = text.toCharArray();
        final Stack<Character> bracketStack = new Stack<Character>();
        final StringBuilder removed = new StringBuilder();
        for (char c : chars) {
            if (c == '(') {
                bracketStack.push(c);
            } else if (c == ')') {
                if (bracketStack.isEmpty()) {
                    return null;
                }

                Character pop = bracketStack.pop();
                if (pop != '(') {
                    return null;
                }
            } else if (c == '[') {
                bracketStack.push(c);
            } else if (c == ']') {
                if (bracketStack.isEmpty()) {
                    return null;
                }

                Character pop = bracketStack.pop();
                if (pop != '[') {
                    return null;
                }
            } else {
                removed.append(c);
            }
        }

        if (bracketStack.isEmpty()) {
            return removed.toString();
        } else {
            return null;
        }
    }
}
