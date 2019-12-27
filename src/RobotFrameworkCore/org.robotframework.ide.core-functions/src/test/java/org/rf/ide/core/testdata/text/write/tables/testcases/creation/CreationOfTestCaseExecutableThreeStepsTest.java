/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfThreeExecutionRowsTest;

public class CreationOfTestCaseExecutableThreeStepsTest extends ACreationOfThreeExecutionRowsTest {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public IExecutableStepsHolder getExecutableWithName() {
        final TestCase execUnit = createModelWithOneTestCaseInside();
        execUnit.getName().setText("TestCase");

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
        execUnit.addElement(new RobotExecutableRow<TestCase>());
        execUnit.addElement(new RobotExecutableRow<TestCase>());
        execUnit.addElement(new RobotExecutableRow<TestCase>());
        testCaseTable.addTest(execUnit);

        return execUnit;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithName(final FileFormat format) {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeLinesWithoutCommentedLineCmpFile(convert("ExecActionAllCombinationsNoCommentLine", format));
        store.setThreeLinesWithCommentAndEmptyLineCmpFile(
                convert("ExecActionWith3ArgsCommentOneCommentedLineAndOneEmpty", format));
        store.setThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile(
                convert("ExecActionEmptyLineInTheMiddleCommentInFirst", format));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName(final FileFormat format) {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeLinesWithoutCommentedLineCmpFile(
                convert("ExecActionAllCombinationsNoCommentLineMissingTestName", format));
        store.setThreeLinesWithCommentAndEmptyLineCmpFile(
                convert("ExecActionWith3ArgsCommentOneCommentedLineAndOneEmptyWithoutTestName", format));
        store.setThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile(
                convert("ExecActionEmptyLineInTheMiddleCommentMissingTestName", format));

        return store;
    }

    private String convert(final String fileName, final FileFormat format) {
        return "testCases/exec/new/oneTc/threeExecs/" + fileName + "." + format.getExtension();
    }
}
