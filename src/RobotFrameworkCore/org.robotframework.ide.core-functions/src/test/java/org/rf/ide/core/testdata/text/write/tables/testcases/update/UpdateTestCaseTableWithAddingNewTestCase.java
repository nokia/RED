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
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

/**
 * @author wypych
 */
public class UpdateTestCaseTableWithAddingNewTestCase extends RobotFormatParameterizedTest {

    public UpdateTestCaseTableWithAddingNewTestCase(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_update_addingNewTestCase() throws Exception {
        // prepare
        final String inFileName = convert("Input_OneTestCase_andAddNewOne_withOneExecInside");
        final String outputFileName = convert("Output_OneTestCase_andAddNewOne_withOneExecInside");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase newTestT2 = testCaseTable.createTestCase("T2");
        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log2"));
        executionRow.addArgument(RobotToken.create("ok2"));
        newTestT2.addElement(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_update_addingNewEmptyTestCase() throws Exception {
        // prepare
        final String inFileName = convert("Input_ThreeTestCasesAndAddingNewEmptyOne");
        final String outputFileName = convert("Output_ThreeTestCasesAndAddingNewEmptyOne");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        testCaseTable.createTestCase("case 3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName) {
        return "testCases/new/" + fileName + "." + getExtension();
    }
}
