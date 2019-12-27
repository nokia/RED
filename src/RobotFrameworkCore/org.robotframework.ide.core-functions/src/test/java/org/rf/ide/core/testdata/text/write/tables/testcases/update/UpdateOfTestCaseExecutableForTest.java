/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.update;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class UpdateOfTestCaseExecutableForTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_update_forLoopFix(final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_ForWithLineContinueAndHashes", format);
        final String outputFileName = convert("Output_ForWithLineContinueAndHashes", format);

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_update_secondTestCaseWithCommentBetween(final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_TestExecutionActionWithForCommentInside", format);
        final String outputFileName = convert("Output_TestExecutionActionWithForCommentInside", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(1);
        final RobotExecutableRow<TestCase> rExecRow = testCase.getExecutionContext().get(1);
        rExecRow.setArgument(1, "${x2}");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);

    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_updateExecRowFor_andTestNameExists(final FileFormat format) throws Exception {
        assert_emptyFile_updateExecRowFor_andTestNameNotExists("Input_TestExecutionActionWithFor",
                "Output_TestExecutionActionWithFor", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_updateExecRowFor_andTestNameNotExists(final FileFormat format) throws Exception {
        assert_emptyFile_updateExecRowFor_andTestNameNotExists("Input_TestExecutionActionWithForWithoutTestName",
                "Output_TestExecutionActionWithForWithoutTestName", format);
    }

    private void assert_emptyFile_updateExecRowFor_andTestNameNotExists(final String inFileNameWithoutExt,
            final String outFileNameWithoutExt, final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert(inFileNameWithoutExt, format);
        final String outputFileName = convert(outFileNameWithoutExt, format);
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

    private String convert(final String fileName, final FileFormat format) {
        return "testCases/exec/update/" + fileName + "." + format.getExtension();
    }
}
