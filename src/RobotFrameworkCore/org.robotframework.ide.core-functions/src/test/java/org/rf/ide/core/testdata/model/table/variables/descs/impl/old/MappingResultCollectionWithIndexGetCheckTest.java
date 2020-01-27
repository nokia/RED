/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.MappingResult;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.VariableExtractor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class MappingResultCollectionWithIndexGetCheckTest {

    @SuppressWarnings("unused")
    @ParameterizedTest
    @CsvFileSource(resources = "VAR_INDEX_CHECK.cvs")
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
