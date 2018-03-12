/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.text.write.tables.execution.update;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

/**
 * @author wypych
 */
public class CreationOfNewExecutionRowsInTestCase extends RobotFormatParameterizedTest {

    public CreationOfNewExecutionRowsInTestCase(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_givenTestCaseTableWithOneExecLine_whenAddNewExecutable_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_OneTestCaseWithOneExecRow");
        final String outputFileName = convert("Output_OneTestCaseWithOneExecRow");
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

    @Test
    public void test_givenTestCaseTableWithThreeExecLine_whenAddNewExecutableInTheMiddle_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_OneTestCaseWithThreeExecRow");
        final String outputFileName = convert( "Output_OneTestCaseWithThreeExecRow");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(0);

        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log_new"));
        executionRow.addArgument(RobotToken.create("arg_new"));
        testCase.addElement(executionRow, 2);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenTestCaseTableWithThreeExecLine_whenAddNewExecutableAsLast_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_OneTestCaseWithThreeExecRowAddExecAsLast");
        final String outputFileName = convert("Output_OneTestCaseWithThreeExecRowAddExecAsLast");
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

    private String convert(final String fileName) {
        return "testCases/exec/update/" + fileName + "." + getExtension();
    }
}
