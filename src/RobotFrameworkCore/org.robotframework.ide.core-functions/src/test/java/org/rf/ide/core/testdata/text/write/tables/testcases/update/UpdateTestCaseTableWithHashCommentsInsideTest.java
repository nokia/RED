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
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public class UpdateTestCaseTableWithHashCommentsInsideTest {

    public static final String PRETTY_NEW_DIR_LOCATION_NEW_UNITS = "testCases//hashCommentCases//";

    @Test
    public void test_givenTestCaseTableWithHashCommentLines_whenChangeKeywordNameInLastKeyword_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Input_TestCaseModification_HashAtTheEnd.robot";
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_TestCaseModification_HashAtTheEnd.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(1);
        RobotExecutableRow<TestCase> lastExec = testCase.getExecutionContext()
                .get(testCase.getExecutionContext().size() - 1);
        lastExec.setAction(RobotToken.create(lastExec.getAction().getText() + "2"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenTestCaseTableWithHashCommentLines_whenAddNewTestCaseAndModificationOfLastKeywordInPrevious_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Input_TestCaseModification_HashInTheMiddle.robot";
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_TestCaseModification_HashInTheMiddle.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(1);
        RobotExecutableRow<TestCase> lastExec = testCase.getExecutionContext()
                .get(testCase.getExecutionContext().size() - 1);
        lastExec.setAction(RobotToken.create(lastExec.getAction().getText() + "2"));
        testCaseTable.createTestCase("test5");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }
}
