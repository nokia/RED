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
            .compile("([+]|[-]|[*]|[/]|[:]|[>][=]?|[<][=]?)");

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
                        if (!isBracketToken(s)) {
                            text = Optional.absent();
                            break;
                        }
                    } else {
                        text = Optional.of(new TextPosition(variableName.getFullText(), variableName.getStart(),
                                variableName.getStart() + lastReadCharacter + s.length() - 1));
                    }
                }

                lastReadCharacter = lastReadCharacter + s.length();
            }
        }

        return text;
    }

    @VisibleForTesting
    protected boolean isBracketToken(final String s) {
        return "[".equals(s) || "]".equals(s) || "(".equals(s) || ")".equals(s);
    }
}
