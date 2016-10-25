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

    private final static Pattern ILLEGAL_BRACKET_SYNTAX = Pattern.compile("(\\)|\\])\\s*(\\(|\\[)");

    private final static String NUMBER_PATTERN = "([+]|[-])*[0-9][0-9]*\\s*";

    private final static String COUNT_OPERATIONS = "(\\s)*([+]|[-]|[*]|[/]|[:]|[>]|[<]|[=]|[&]|[%]|\\^|\\!|[|])(\\s)*";

    private final static Pattern NUMBER_OPERATION = Pattern
            .compile("^" + NUMBER_PATTERN + "(" + COUNT_OPERATIONS + NUMBER_PATTERN + ")*");

    public Optional<TextPosition> extractVariableName(final VariableDeclaration variableDec) {
        Optional<TextPosition> text = Optional.absent();

        if (variableDec.getVariableType() == GeneralVariableType.COMPUTATION) {
            final TextPosition textPostionVariableName = variableDec.getVariableName();
            final String variableName = textPostionVariableName.getText().trim();
            if (!variableName.isEmpty()) {
                if (!variableName.startsWith("(") && !variableName.startsWith("\"") && !variableName.startsWith("[")) {
                    if (!ILLEGAL_BRACKET_SYNTAX.matcher(variableName).matches()) {
                        if (startsFromNumber(variableName)) {
                            if (isNumberComputation(variableName)) {
                                return Optional.of(new TextPosition("" + 3, 0, 1));
                            }
                        } else {

                        }
                    }
                }
            }
        }

        return text;
    }

    private boolean startsFromNumber(final String variableName) {
        return variableName.matches("^[+|-]*[0-9]+");
    }

    private boolean isNumberComputation(final String variableName) {
        final String bracketRemoved = validateAndRemoveBrackets(variableName);
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
