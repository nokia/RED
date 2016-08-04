/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.update;

import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class AUpdateOfTestCaseExecutableForTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//exec//update//";

    private final String extension;

    public AUpdateOfTestCaseExecutableForTest(final String extension) {
        this.extension = extension;
    }

    @Ignore(value = "RED-441")
    @Test
    public void test_update_secondTestCaseWithCommentBetween() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_TestExecutionActionWithForCommentInside."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_TestExecutionActionWithForCommentInside."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(1);
        final RobotExecutableRow<TestCase> rExecRow = testCase.getExecutionContext().get(1);
        rExecRow.setArgument(0, "${x2}");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);

    }

    @Test
    public void test_emptyFile_updateExecRowFor_andTestNameExists() throws Exception {
        assert_emptyFile_updateExecRowFor_andTestNameNotExists("Input_TestExecutionActionWithFor.",
                "Output_TestExecutionActionWithFor.");
    }

    @Test
    public void test_emptyFile_updateExecRowFor_andTestNameUnexists() throws Exception {
        assert_emptyFile_updateExecRowFor_andTestNameNotExists("Input_TestExecutionActionWithForWithoutTestName.",
                "Output_TestExecutionActionWithForWithoutTestName.");
    }

    private void assert_emptyFile_updateExecRowFor_andTestNameNotExists(final String inFileNameWithoutExt,
            final String outFileNameWithoutExt) throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + inFileNameWithoutExt + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + outFileNameWithoutExt + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(0);
        final RobotExecutableRow<TestCase> rExecRow = testCase.getExecutionContext().get(1);
        rExecRow.setArgument(3, "INFO");
        rExecRow.removeArgument(2);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
