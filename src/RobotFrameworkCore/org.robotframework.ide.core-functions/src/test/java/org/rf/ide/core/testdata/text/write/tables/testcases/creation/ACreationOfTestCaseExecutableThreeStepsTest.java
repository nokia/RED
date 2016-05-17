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

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//exec//new//oneTestCase//threeExecs//";

    private final String extension;

    public ACreationOfTestCaseExecutableThreeStepsTest(final String extension) {
        this.extension = extension;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public IExecutableStepsHolder getExecutableWithName() {
        TestCase execUnit = createModelWithOneTestCaseInside();
        execUnit.getTestName().setText("TestCase");

        return (IExecutableStepsHolder<TestCase>) execUnit;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public IExecutableStepsHolder getExecutableWithoutName() {
        return (IExecutableStepsHolder<TestCase>) createModelWithOneTestCaseInside();
    }

    private TestCase createModelWithOneTestCaseInside() {
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        TestCase execUnit = new TestCase(testName);
        execUnit.addTestExecutionRow(new RobotExecutableRow<TestCase>());
        execUnit.addTestExecutionRow(new RobotExecutableRow<TestCase>());
        execUnit.addTestExecutionRow(new RobotExecutableRow<TestCase>());
        testCaseTable.addTest(execUnit);

        return execUnit;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeLinesWithoutCommentedLineCmpFile(convert("TestExecutionActionWithAllCombinationsNoCommentLine"));
        store.setThreeLinesWithCommentAndEmptyLineCmpFile(
                convert("TestExecutionActionWithThreeArgsCommentAndOneCommentedLineAndOneEmpty"));
        store.setThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile(
                convert("TestExecutionActionEmptyLineInTheMiddleCommentTheFirst"));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeLinesWithoutCommentedLineCmpFile(
                convert("TestExecutionActionWithAllCombinationsNoCommentLineWithoutTestName"));
        store.setThreeLinesWithCommentAndEmptyLineCmpFile(
                convert("TestExecutionActionWithThreeArgsCommentAndOneCommentedLineAndOneEmptyWithoutTestName"));
        store.setThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile(
                convert("TestExecutionActionEmptyLineInTheMiddleCommentTheFirstWithoutTestName"));

        return store;
    }

    public String convert(final String fileName) {
        return PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
