/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import java.util.Optional;
import java.util.Stack;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.VariableDeclaration.VariableDeclarationType;

class VariableComputationHelper {

    private static final String NUMBER_PATTERN_TXT = "([+]|[-])*\\d+([.]\\d+)?\\s*";

    private static final String COUNT_OPERATIONS = "([+]|[-]|[*]|[/]|[:]|[>]|[<]|[=]|[&]|[%]|\\^|\\!|[|])";

    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_PATTERN_TXT);

    private static final Pattern COUNT_OPERATION_PATTERN = Pattern.compile(COUNT_OPERATIONS);

    private static final Pattern ILLEGAL_BRACKET_SYNTAX = Pattern
            .compile("\\s*((?!" + COUNT_OPERATIONS + "| (\\[|\\()).)\\s*(\\[|\\()");

    private static final Pattern NUMBER_OPERATION = Pattern
            .compile("^" + NUMBER_PATTERN + "(" + COUNT_OPERATIONS + NUMBER_PATTERN + ")*");

    private static final String QUOTE_TEXT_1 = "[\"](([\\\\][\"])|((?![\"]).))*[\"]";

    private static final String QUOTE_TEXT_2 = "['](([\\\\]['])|((?![']).))*[']";

    private static final Pattern TEXT_OPERATION = Pattern
            .compile("^((\\s*(" + COUNT_OPERATIONS + ")+\\s*(" + NUMBER_PATTERN + "|" + QUOTE_TEXT_1 + "|"
                    + QUOTE_TEXT_2 + ")+\\s*)+)*");

    static Optional<TextPosition> extractVariableName(final VariableDeclaration variableDec) {
        if (variableDec.getVariableType() == VariableDeclarationType.COMPUTATION) {
            final String variableName = variableDec.getVariableName().getText().trim();
            if (!variableName.isEmpty()) {
                final String variableFullName = variableDec.getVariableName().getFullText();
                if (!variableName.startsWith("(") && !variableName.startsWith("\"") && !variableName.startsWith("[")) {
                    if (!ILLEGAL_BRACKET_SYNTAX.matcher(variableName).find()) {
                        if (startsFromNumber(variableName)) {
                            if (isNumberOperation(variableName)) {
                                return Optional.of(new TextPosition("" + 3, 0, 1));
                            }
                        } else if (startsWithVariableName(variableName)) {
                            final String variable = extractVariableName(variableName);
                            if (variable != null) {
                                final int startIndex = variableFullName.indexOf(variable);

                                if (startIndex >= 0) {
                                    if (isTextOperation(variableName, variable)) {
                                        return Optional.of(new TextPosition(variableFullName, startIndex,
                                                startIndex + variable.length() - 1));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    final int startIndex = variableFullName.indexOf(variableName.charAt(0));
                    return Optional.of(new TextPosition(variableFullName, startIndex, startIndex));
                }
            }
        }

        return Optional.empty();
    }

    private static boolean isTextOperation(final String expression, final String variable) {
        final String restOperationText = expression.substring(variable.length()).trim();
        final String withoutBrackets = validateAndRemoveBrackets(restOperationText);
        if (withoutBrackets != null) {
            return TEXT_OPERATION.matcher(withoutBrackets).matches();
        }

        return false;
    }

    private static String extractVariableName(final String variableName) {
        final String[] split = COUNT_OPERATION_PATTERN.split(variableName);
        if (split.length >= 2) {
            return split[0].trim();
        }

        return null;
    }

    private static boolean startsFromNumber(final String expression) {
        return NUMBER_PATTERN.matcher(expression).matches();
    }

    private static boolean startsWithVariableName(final String expression) {
        return !startsFromNumber(expression);
    }

    private static boolean isNumberOperation(final String expression) {
        final String bracketRemoved = validateAndRemoveBrackets(expression);
        if (bracketRemoved != null) {
            return NUMBER_OPERATION.matcher(bracketRemoved).matches();
        }

        return false;
    }

    private static String validateAndRemoveBrackets(final String text) {
        final char[] chars = text.toCharArray();
        final Stack<Character> bracketStack = new Stack<>();
        final StringBuilder removed = new StringBuilder();
        for (final char c : chars) {
            if (c == '(') {
                bracketStack.push(c);
            } else if (c == ')') {
                if (bracketStack.isEmpty()) {
                    return null;
                }

                final Character pop = bracketStack.pop();
                if (pop != '(') {
                    return null;
                }
            } else if (c == '[') {
                bracketStack.push(c);
            } else if (c == ']') {
                if (bracketStack.isEmpty()) {
                    return null;
                }

                final Character pop = bracketStack.pop();
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
