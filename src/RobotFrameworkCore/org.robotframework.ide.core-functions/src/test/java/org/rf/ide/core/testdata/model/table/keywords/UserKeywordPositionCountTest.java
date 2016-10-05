/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords;

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
import org.rf.ide.core.testdata.model.table.KeywordTable;

/**
 * @author wypych
 */
public class UserKeywordPositionCountTest {

    private static final String FILE_LOCATION = "parser/bugs/";

    private static RobotFile testFileModel;

    private final static Map<String, IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> CACHED_UNITS = new HashMap<>(
            0);

    @BeforeClass
    public static void setUp() throws Exception {
        // prepare
        final Path inputFile = Paths
                .get(RobotParser.class.getResource(FILE_LOCATION + "UserKeyword_endOfUnitCheck.robot").toURI());
        testFileModel = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());
        final KeywordTable keywordTable = testFileModel.getKeywordTable();
        final List<UserKeyword> userKeywords = keywordTable.getKeywords();
        for (final UserKeyword uk : userKeywords) {
            final String ukName = uk.getName().getText();
            if (CACHED_UNITS.containsKey(ukName)) {
                throw new IllegalStateException("Executable Unit with name " + ukName + " already exists in position "
                        + CACHED_UNITS.get(ukName).getName().getFilePosition());
            }

            CACHED_UNITS.put(ukName, uk);
        }
    }

    @Test
    public void given_EmptyUserKeyword_when_shouldReturnEndOf_2ndLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS.get("Empty");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(2);
        assertThat(endPosition.getColumn()).isEqualTo(5);
        assertThat(endPosition.getOffset()).isEqualTo(23);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningUserKeyword_when_shouldReturnEndOf_4thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginning");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(4);
        assertThat(endPosition.getColumn()).isEqualTo(26);
        assertThat(endPosition.getOffset()).isEqualTo(53);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningAndEndUserKeyword_when_shouldReturnEndOf_6thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginningAndEnd");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(6);
        assertThat(endPosition.getColumn()).isEqualTo(34);
        assertThat(endPosition.getOffset()).isEqualTo(91);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningAndEndAndSpaceAfterUserKeyword_when_shouldReturnEndOf_8thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginningAndEndAndSpaceAfter");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(8);
        assertThat(endPosition.getColumn()).isEqualTo(48);
        assertThat(endPosition.getOffset()).isEqualTo(143);
    }

    @Test
    public void given_EmptyWithPipeAtBeginningAndEndAndSpaceAfterAndTabsUserKeyword_when_shouldReturnEndOf_10thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("EmptyWithPipeAtBeginningAndEndAndSpaceAfterAndTabs");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(10);
        assertThat(endPosition.getColumn()).isEqualTo(57);
        assertThat(endPosition.getOffset()).isEqualTo(204);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesUserKeyword_when_shouldReturnEndOf_13thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpaces");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(13);
        assertThat(endPosition.getColumn()).isEqualTo(9);
        assertThat(endPosition.getOffset()).isEqualTo(249);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesAndSpaceAsLastUserKeyword_when_shouldReturnEndOf_16thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpacesAndSpaceAsLast");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(16);
        assertThat(endPosition.getColumn()).isEqualTo(10);
        assertThat(endPosition.getOffset()).isEqualTo(309);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTabUserKeyword_when_shouldReturnEndOf_19thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTab");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(19);
        assertThat(endPosition.getColumn()).isEqualTo(12);
        assertThat(endPosition.getOffset()).isEqualTo(377);
    }

    @Test
    public void given_One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTabUserKeyword_when_shouldReturnEndOf_22ndLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTab_Pipe");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(22);
        assertThat(endPosition.getColumn()).isEqualTo(17);
        assertThat(endPosition.getOffset()).isEqualTo(455);
    }

    @Test
    public void given_Three_ExecsRowOnly_WithSpacesAndSpaceAsLastAndTab_PipeUserKeyword_when_shouldReturnEndOf_29thLine() {
        // prepare
        IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>> exec = CACHED_UNITS
                .get("Three_ExecsRowOnly_WithSpacesAndSpaceAsLastAndTab_Pipe");

        // execute
        FilePosition endPosition = exec.getHolder().getEndPosition();

        // verify
        assertThat(endPosition.isNotSet()).isFalse();
        assertThat(endPosition.getLine()).isEqualTo(29);
        assertThat(endPosition.getColumn()).isEqualTo(10);
        assertThat(endPosition.getOffset()).isEqualTo(592);
    }
}
