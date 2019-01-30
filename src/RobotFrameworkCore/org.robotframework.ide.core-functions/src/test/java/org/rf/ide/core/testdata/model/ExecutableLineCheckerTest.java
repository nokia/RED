/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.RobotParser;

public class ExecutableLineCheckerTest {

    RobotFile modelFile;

    @Test
    public void test() throws URISyntaxException {

        final RobotParser parser = RobotModelTestProvider.getParser();
        modelFile = RobotModelTestProvider.getModelFile("test_ExeChecker_1.robot", parser);

        checkExecutableLines(2, 3, 4, 5, 13, 14, 16, 17, 18, 22, 23, 24, 26, 29, 32, 36, 38, 41, 43);

        checkNotExecutableLines(1, 6, 7, 8, 9, 10, 11, 12, 15, 19, 20, 21, 25, 27, 28, 30, 31, 33, 34, 35, 37, 39, 40,
                42, 44);

    }

    private void checkExecutableLines(final int... lines) {
        for (final int line : lines) {
            assertThat(ExecutableLineChecker.isExecutableLine(modelFile, line)).isTrue();
        }
    }

    private void checkNotExecutableLines(final int... lines) {
        for (final int line : lines) {
            assertThat(ExecutableLineChecker.isExecutableLine(modelFile, line)).isFalse();
        }
    }
}
