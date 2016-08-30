/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.update;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public class UpdateTestCaseTest {

    public static final String PRETTY_NEW_DIR_LOCATION_NEW_UNITS = "testCases//update//";

    @Test
    public void test_givenTestCaseTableWithHashCommentLines_whenChangeKeywordNameInLastKeyword_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "InputTwoTestCases_oneHasSpaceAtTheBeginningOfName.robot";
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "OutputTwoTestCases_oneHasSpaceAtTheBeginningOfName.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(0);
        RobotExecutableRow<TestCase> prevExec = testCase.getExecutionContext()
                .get(testCase.getExecutionContext().size() - 2);
        prevExec.getArguments().get(0).setText("data2");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

}
