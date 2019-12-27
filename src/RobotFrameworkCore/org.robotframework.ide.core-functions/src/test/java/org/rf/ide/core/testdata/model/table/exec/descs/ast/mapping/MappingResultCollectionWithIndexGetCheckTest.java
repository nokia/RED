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
import java.util.regex.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class MappingResultCollectionWithIndexGetCheckTest {

    private final static Pattern SPLIT_CSV_PATTERN = Pattern.compile("(?<!\\\\),");

    public static List<Object[]> provideTestData() throws Exception {
        final List<String> lines = Files.readAllLines(Paths.get(
                VariableComputationHelperExtractionParameterizedTest.class.getResource("VAR_INDEX_CHECK.cvs").toURI()),
                Charset.forName("UTF-8"));

        final List<Object[]> data = new ArrayList<>();
        int lineNumber = 0;
        for (final String line : lines) {
            lineNumber++;
            if ("".equals(line.trim()) || line.trim().startsWith("#")) {
                continue;
            }
            final String[] split = SPLIT_CSV_PATTERN.split(line);
            if (split.length != 3) {
                throw new IllegalStateException(
                        "Expected 3 columns for each line. Line " + lineNumber + " has " + split.length);
            }

            final String testName = split[0];
            final String text = split[1];
            final String shouldMarkAsVariableIndexText = split[2].trim().toLowerCase();
            boolean shouldMarkAsVariableIndex = false;
            if ("false".equals(shouldMarkAsVariableIndexText) || "true".equals(shouldMarkAsVariableIndexText)) {
                shouldMarkAsVariableIndex = Boolean.parseBoolean(shouldMarkAsVariableIndexText);
            } else {
                throw new IllegalStateException("Flag should extract - column 3, should be boolean got: "
                        + shouldMarkAsVariableIndexText + ". Line " + lineNumber);
            }

            final Object[] p = new Object[3];
            p[0] = testName;
            p[1] = text;
            p[2] = shouldMarkAsVariableIndex;

            data.add(p);
        }

        return data;
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void test(final String testName, final String text, final boolean shouldMarkAsVariableIndex) {
        final MappingResult mappingResult = performOperationsFor(text);
        if (shouldMarkAsVariableIndex) {
            assertThat(mappingResult.isCollectionVariableElementGet()).isTrue();
        } else {
            assertThat(mappingResult.isCollectionVariableElementGet()).isFalse();
        }
    }

    private MappingResult performOperationsFor(final String text) {
        final RobotToken token = new RobotToken();
        token.setStartOffset(0);
        token.setLineNumber(1);
        token.setStartColumn(0);
        token.setText(text);
        return new VariableExtractor().extract(token);
    }
}
