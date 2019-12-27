/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.text.write.tables.execution.update;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.FileFormat;
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
public class CreationOfNewExecutionRowsInTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenTestCaseTableWithOneExecLine_whenAddNewExecutable_thenCheckIfTableIsCorrectlyDumped(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_OneTestCaseWithOneExecRow", format);
        final String outputFileName = convert("Output_OneTestCaseWithOneExecRow", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(0);
        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("LogMe"));
        executionRow.addArgument(RobotToken.create("arg1"));
        testCase.addElement(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenTestCaseTableWithThreeExecLine_whenAddNewExecutableInTheMiddle_thenCheckIfTableIsCorrectlyDumped(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_OneTestCaseWithThreeExecRow", format);
        final String outputFileName = convert("Output_OneTestCaseWithThreeExecRow", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(0);

        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log_new"));
        executionRow.addArgument(RobotToken.create("arg_new"));
        testCase.addElement(2, executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenTestCaseTableWithThreeExecLine_whenAddNewExecutableAsLast_thenCheckIfTableIsCorrectlyDumped(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_OneTestCaseWithThreeExecRowAddExecAsLast", format);
        final String outputFileName = convert("Output_OneTestCaseWithThreeExecRowAddExecAsLast", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(0);

        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log_new"));
        executionRow.addArgument(RobotToken.create("arg_new"));
        testCase.addElement(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "testCases/exec/update/" + fileName + "." + format.getExtension();
    }
}
