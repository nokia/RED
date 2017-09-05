/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.TestCaseTable;

/**
 * @author wypych
 */
public class TestCasePositionCountTest {

    private static final String FILE_LOCATION = "parser/bugs/";

    private static RobotFile testFileModel;

    private final static Map<String, IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> CACHED_UNITS = new HashMap<>(
            0);

    @BeforeClass
    public static void setUp() throws Exception {
        // prepare
        final Path inputFile = Paths
                .get(RobotParser.class.getResource(FILE_LOCATION + "TestCase_endOfUnitCheck.robot").toURI());
        testFileModel = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());
        final TestCaseTable testCaseTable = testFileModel.getTestCaseTable();
        final List<TestCase> testCases = testCaseTable.getTestCases();
        for (final TestCase tc : testCases) {
            final String tcName = tc.getName().getText();
            if (CACHED_UNITS.containsKey(tcName)) {
                throw new IllegalStateException("Executable Unit with name " + tcName + " already exists in position "
                        + CACHED_UNITS.get(tcName).getName().getFilePosition());
            }

            CACHED_UNITS.put(tcName, tc);
        }
    }

    @Test
    public void given_EmptyTestCase_when_shouldReturnEndOf_3rdLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS.get("Empty");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(3);
        assertThat(endPosition.getColumn()).isEqualTo(0);
        assertThat(endPosition.getOffset()).isEqualTo(27);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningTestCase_when_shouldReturnEndOf_5thLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginning");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(5);
        assertThat(endPosition.getColumn()).isEqualTo(0);
        assertThat(endPosition.getOffset()).isEqualTo(57);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningAndEndTestCase_when_shouldReturnEndOf_7thLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginningAndEnd");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(7);
        assertThat(endPosition.getColumn()).isEqualTo(0);
        assertThat(endPosition.getOffset()).isEqualTo(95);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningAndEndAndSpaceAfterTestCase_when_shouldReturnEndOf_9thLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginningAndEndAndSpaceAfter");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify - this is empty line position
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(9);
        assertThat(endPosition.getColumn()).isEqualTo(0);
        assertThat(endPosition.getOffset()).isEqualTo(147);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningAndEndAndSpaceAfterAndTabsTestCase_when_shouldReturnEndOf_11thLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginningAndEndAndSpaceAfterAndTabs");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(11);
        assertThat(endPosition.getColumn()).isEqualTo(0);
        assertThat(endPosition.getOffset()).isEqualTo(208);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesTestCase_when_shouldReturnEndOf_14thLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpaces");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(14);
        assertThat(endPosition.getColumn()).isEqualTo(0);
        assertThat(endPosition.getOffset()).isEqualTo(253);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesAndSpaceAsLastTestCase_when_shouldReturnEndOf_17thLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpacesAndSpaceAsLast");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(17);
        assertThat(endPosition.getColumn()).isEqualTo(0);
        assertThat(endPosition.getOffset()).isEqualTo(313);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTabTestCase_when_shouldReturnEndOf_20thLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTab");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(20);
        assertThat(endPosition.getColumn()).isEqualTo(0);
        assertThat(endPosition.getOffset()).isEqualTo(381);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTabTestCase_when_shouldReturnEndOf_22ndLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTab_Pipe");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(23);
        assertThat(endPosition.getColumn()).isEqualTo(0);
        assertThat(endPosition.getOffset()).isEqualTo(459);
    }

    @Test
    public void given_One_ExecsRowOnly_WithSpacesAndSpaceAsLastAndTab_PipeTestCase_when_shouldReturnEndOf_29thLine() {
        // prepare
        final IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("Three_ExecsRowOnly_WithSpacesAndSpaceAsLastAndTab_Pipe");

        // execute
        final FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(29);
        assertThat(endPosition.getColumn()).isEqualTo(10);
        assertThat(endPosition.getOffset()).isEqualTo(594);
    }
}
