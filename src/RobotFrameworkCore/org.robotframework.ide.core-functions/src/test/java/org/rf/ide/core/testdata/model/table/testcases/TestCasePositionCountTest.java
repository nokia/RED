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
    public void given_EmptyTestCase_when_shouldReturnEndOf_2ndLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS.get("Empty");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(2);
        assertThat(endPosition.getColumn()).isEqualTo(5);
        assertThat(endPosition.getOffset()).isEqualTo(25);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningTestCase_when_shouldReturnEndOf_4thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginning");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(4);
        assertThat(endPosition.getColumn()).isEqualTo(26);
        assertThat(endPosition.getOffset()).isEqualTo(55);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningAndEndTestCase_when_shouldReturnEndOf_6thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginningAndEnd");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(6);
        assertThat(endPosition.getColumn()).isEqualTo(34);
        assertThat(endPosition.getOffset()).isEqualTo(93);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningAndEndAndSpaceAfterTestCase_when_shouldReturnEndOf_8thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginningAndEndAndSpaceAfter");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(8);
        assertThat(endPosition.getColumn()).isEqualTo(48);
        assertThat(endPosition.getOffset()).isEqualTo(145);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningAndEndAndSpaceAfterAndTabsTestCase_when_shouldReturnEndOf_10thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginningAndEndAndSpaceAfterAndTabs");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(10);
        assertThat(endPosition.getColumn()).isEqualTo(57);
        assertThat(endPosition.getOffset()).isEqualTo(206);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesTestCase_when_shouldReturnEndOf_13thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpaces");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(13);
        assertThat(endPosition.getColumn()).isEqualTo(9);
        assertThat(endPosition.getOffset()).isEqualTo(251);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesAndSpaceAsLastTestCase_when_shouldReturnEndOf_16thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpacesAndSpaceAsLast");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(16);
        assertThat(endPosition.getColumn()).isEqualTo(10);
        assertThat(endPosition.getOffset()).isEqualTo(311);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTabTestCase_when_shouldReturnEndOf_19thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTab");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(19);
        assertThat(endPosition.getColumn()).isEqualTo(12);
        assertThat(endPosition.getOffset()).isEqualTo(379);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTabTestCase_when_shouldReturnEndOf_22ndLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTab_Pipe");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(22);
        assertThat(endPosition.getColumn()).isEqualTo(18);
        assertThat(endPosition.getOffset()).isEqualTo(458);
    }

    @Test
    public void given_Three_ExecsRowOnly_WithSpacesAndSpaceAsLastAndTab_PipeTestCase_when_shouldReturnEndOf_29thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("Three_ExecsRowOnly_WithSpacesAndSpaceAsLastAndTab_Pipe");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(29);
        assertThat(endPosition.getColumn()).isEqualTo(10);
        assertThat(endPosition.getOffset()).isEqualTo(594);
    }
}
