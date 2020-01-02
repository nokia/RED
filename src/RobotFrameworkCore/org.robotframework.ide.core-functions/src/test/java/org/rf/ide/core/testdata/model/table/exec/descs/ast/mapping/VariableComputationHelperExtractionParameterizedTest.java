/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class VariableComputationHelperExtractionParameterizedTest {

    @ParameterizedTest
    @CsvFileSource(resources = "VAR_WITH_MATH_OPERATIONS.cvs")
    public void test(final String testName, final String text, final int variableNameStart, final String variableName,
            final boolean shouldExtract) {
        final Optional<TextPosition> result = performOperationsFor(text);
        if (shouldExtract) {
            assertThat(result.isPresent()).isTrue();
            final TextPosition varName = result.get();
            assertThat(varName.getFullText()).isEqualTo(text);
            assertThat(varName.getStart()).isEqualTo(variableNameStart);
            assertThat(varName.getText()).isEqualTo(variableName);
            assertThat(varName.getLength()).isEqualTo(variableName.length());
            assertThat(varName.getEnd()).isEqualTo(variableNameStart + variableName.length() - 1);
        } else {
            assertThat(result.isPresent()).isFalse();
        }
    }

    private Optional<TextPosition> performOperationsFor(final String text) {
        final RobotToken token = new RobotToken();
        token.setStartOffset(0);
        token.setLineNumber(1);
        token.setStartColumn(0);
        token.setText(text);
        final MappingResult extract = new VariableExtractor().extract(token);
        return VariableComputationHelper.extractVariableName(extract.getCorrectVariables().get(0));
    }
}
