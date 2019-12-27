/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class VariableComputationHelperExtractionParameterizedTest {

    private final static Pattern SPLIT_CSV_PATTERN = Pattern.compile("(?<!\\\\),");

    private final static Pattern NUMBER = Pattern.compile("[0-9]+");

    public static List<Object[]> provideTestData() throws Exception {
        final List<String> lines = Files
                .readAllLines(Paths.get(VariableComputationHelperExtractionParameterizedTest.class
                        .getResource("VAR_WITH_MATH_OPERATIONS.cvs").toURI()), Charset.forName("UTF-8"));

        final List<Object[]> data = new ArrayList<>();
        int lineNumber = 0;
        for (final String line : lines) {
            lineNumber++;
            if ("".equals(line.trim()) || line.trim().startsWith("#")) {
                continue;
            }
            final String[] split = SPLIT_CSV_PATTERN.split(line);
            if (split.length != 5) {
                throw new IllegalStateException(
                        "Expected 5 columns for each line. Line " + lineNumber + " has " + split.length);
            }

            final String testName = split[0];
            final String text = split[1];
            final String variableNameStartText = split[2];
            int variableNameStart = -1;
            if (NUMBER.matcher(variableNameStartText).matches()) {
                variableNameStart = Integer.parseInt(variableNameStartText);
            } else {
                throw new IllegalStateException(
                        "Variable name start - column 3, should be number from 0 to Integer.MAX got: "
                                + variableNameStartText + ". Line " + lineNumber);
            }

            final String variableName = split[3];
            final String shouldExtractText = split[4].trim().toLowerCase();
            boolean shouldExtract = false;
            if ("false".equals(shouldExtractText) || "true".equals(shouldExtractText)) {
                shouldExtract = Boolean.parseBoolean(shouldExtractText);
            } else {
                throw new IllegalStateException("Flag should extract - column 5, should be boolean got: "
                        + shouldExtractText + ". Line " + lineNumber);
            }

            final Object[] p = new Object[5];
            p[0] = testName;
            p[1] = text;
            p[2] = variableNameStart;
            p[3] = variableName;
            p[4] = shouldExtract;

            data.add(p);
        }

        return data;
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
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
