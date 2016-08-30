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
public abstract class AUpdateTestCaseTableWithAddingNewTestCase {

    private static final String PRETTY_NEW_DIR_LOCATION_NEW_UNITS = "testCases//update//";

    private final String extension;

    public AUpdateTestCaseTableWithAddingNewTestCase(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_update_addingNewTestCase() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Input_OneTestCase_andAddNewOne_withOneExecInside." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_OneTestCase_andAddNewOne_withOneExecInside." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase newTestT2 = testCaseTable.createTestCase("T2");
        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log2"));
        executionRow.addArgument(RobotToken.create("ok2"));
        newTestT2.addTestExecutionRow(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    public String getExtension() {
        return this.extension;
    }
}
