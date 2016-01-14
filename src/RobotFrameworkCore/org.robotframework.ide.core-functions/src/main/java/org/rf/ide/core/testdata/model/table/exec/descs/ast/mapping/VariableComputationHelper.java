/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration.GeneralVariableType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class VariableComputationHelper {

    public final static Pattern COMPUTATION_OPERATION_PATTERN = Pattern
            .compile("([+]|[-]|[*]|[/]|[:]|[>]|[<]|[=]|[&]|[\\^]|[\\!]|[|])+");

    public final static Pattern BRACKETS = Pattern.compile("(\\[|\\(|\\)|\\])");

    public Optional<TextPosition> extractVariableName(final VariableDeclaration variableDec) {
        Optional<TextPosition> text = Optional.absent();

        if (variableDec.getVariableType() == GeneralVariableType.COMPUTATION) {
            TextPosition variableName = variableDec.getVariableName();
            String c = variableName.getText();

            int lastReadCharacter = 0;
            List<String> splitted = Arrays.asList(COMPUTATION_OPERATION_PATTERN.split(c));
            for (String s : splitted) {
                try {
                    Double.parseDouble(s);
                } catch (final NumberFormatException nfe) {
                    if (lastReadCharacter > 0) {
                        if (!isBracketDecorated(s)) {
                            text = Optional.absent();
                            break;
                        }
                    } else {
                        int firstNotWhiteSpace = getFirstNotWhitespaceCharacter(s, 0);
                        int textLength = s.trim().length();
                        text = Optional.of(new TextPosition(variableName.getFullText(),
                                variableName.getStart() + firstNotWhiteSpace,
                                variableName.getStart() + firstNotWhiteSpace + textLength - 1));
                    }
                }

                lastReadCharacter = lastReadCharacter + s.length();
            }
        }

        return text;
    }

    @VisibleForTesting
    protected int getFirstNotWhitespaceCharacter(final String text, int beginChar) {
        int index = -1;
        char[] chars = text.toCharArray();
        for (int i = beginChar; i < chars.length; i++) {
            char c = chars[i];
            if (c != ' ' && c != '\t') {
                index = i;
                break;
            }
        }

        return index;
    }

    @VisibleForTesting
    protected boolean isBracketDecorated(final String s) {
        List<String> bracket = Arrays.asList(BRACKETS.split(s));
        boolean result = !bracket.isEmpty();
        for (final String value : bracket) {
            try {
                if (!value.isEmpty()) {
                    Double.parseDouble(value);
                }
            } catch (final NumberFormatException nfe) {
                result = false;
                break;
            }
        }

        return result;
    }
}
