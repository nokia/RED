/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;

public class VariableComputationHelperTest {

    @Test
    public void variableNamesAreProperlyExtracted() {
        assertThat(VariableComputationHelper.extractVariableName(varDeclaration("${x - 1}")).get().getText())
                .isEqualTo("x");
        assertThat(VariableComputationHelper.extractVariableName(varDeclaration("${x * 2}")).get().getText())
                .isEqualTo("x");
        assertThat(VariableComputationHelper.extractVariableName(varDeclaration("${x + \"abc\"}")).get().getText())
                .isEqualTo("x");
        assertThat(VariableComputationHelper.extractVariableName(varDeclaration("${x + 'abc'}")).get().getText())
                .isEqualTo("x");
    }

    private static VariableDeclaration varDeclaration(final String variable) {
        final TextPosition variableStart = new TextPosition(variable, 1, 1);
        final TextPosition variableEnd = new TextPosition(variable, variable.length() - 1, variable.length() - 1);
        final VariableDeclaration variableDeclaration = new VariableDeclaration(variableStart, variableEnd);
        variableDeclaration.setTypeIdentificator(new TextPosition(variable.substring(0, 1), 0, 0));
        return variableDeclaration;
    }
}
