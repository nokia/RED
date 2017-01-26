/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfThreeExecutionRowsTest;

public abstract class ACreationOfTestCaseExecutableThreeStepsTest extends ACreationOfThreeExecutionRowsTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//exec//new//oneTc//threeExecs//";

    private final String extension;

    public ACreationOfTestCaseExecutableThreeStepsTest(final String extension) {
        this.extension = extension;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public IExecutableStepsHolder getExecutableWithName() {
        final TestCase execUnit = createModelWithOneTestCaseInside();
        execUnit.getTestName().setText("TestCase");

        return execUnit;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public IExecutableStepsHolder getExecutableWithoutName() {
        return createModelWithOneTestCaseInside();
    }

    private TestCase createModelWithOneTestCaseInside() {
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        final TestCase execUnit = new TestCase(testName);
        execUnit.addTestExecutionRow(new RobotExecutableRow<TestCase>());
        execUnit.addTestExecutionRow(new RobotExecutableRow<TestCase>());
        execUnit.addTestExecutionRow(new RobotExecutableRow<TestCase>());
        testCaseTable.addTest(execUnit);

        return execUnit;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeLinesWithoutCommentedLineCmpFile(convert("ExecActionAllCombinationsNoCommentLine"));
        store.setThreeLinesWithCommentAndEmptyLineCmpFile(
                convert("ExecActionWith3ArgsCommentOneCommentedLineAndOneEmpty"));
        store.setThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile(
                convert("ExecActionEmptyLineInTheMiddleCommentInFirst"));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeLinesWithoutCommentedLineCmpFile(
                convert("ExecActionAllCombinationsNoCommentLineMissingTestName"));
        store.setThreeLinesWithCommentAndEmptyLineCmpFile(
                convert("ExecActionWith3ArgsCommentOneCommentedLineAndOneEmptyWithoutTestName"));
        store.setThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile(
                convert("ExecActionEmptyLineInTheMiddleCommentMissingTestName"));

        return store;
    }

    public String convert(final String fileName) {
        return PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
