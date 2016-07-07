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
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfExecutionRowTest;

public abstract class ACreationOfTestCaseExecutableOneStepTest extends ACreationOfExecutionRowTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//exec//new//oneTestCase//oneExec//";

    private final String extension;

    public ACreationOfTestCaseExecutableOneStepTest(final String extension) {
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
        testCaseTable.addTest(execUnit);

        return execUnit;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setActionOnlyCmpFile(convert("TestCaseExecutionActionOnly"));
        store.setActionWithCommentCmpFile(convert("TestCaseExecutionActionWithComment"));
        store.setActionWithOneArgCmpFile(convert("TestCaseExecutionActionWithOneArg"));
        store.setActionWithOneArgAndCommentCmpFile(convert("TestCaseExecutionActionWithOneArgAndComment"));
        store.setActionWithThreeArgCmpFile(convert("TestCaseExecutionActionWithThreeArg"));
        store.setActionWithThreeArgAndCommentCmpFile(convert("TestCaseExecutionActionWithThreeArgAndComment"));
        store.setCommentOnlyCmpFile(convert("TestCaseExecutionCommentOnly"));
        store.setEmptyLineCmpFile(convert("TestCaseExecutionEmptyLine"));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setActionOnlyCmpFile(convert("TestCaseExecutionActionOnlyWithoutTestName"));
        store.setActionWithCommentCmpFile(convert("TestCaseExecutionActionWithCommentWithoutTestName"));
        store.setActionWithOneArgCmpFile(convert("TestCaseExecutionActionWithOneArgWithoutTestName"));
        store.setActionWithOneArgAndCommentCmpFile(
                convert("TestCaseExecutionActionWithOneArgAndCommentWithoutTestName"));
        store.setActionWithThreeArgCmpFile(convert("TestCaseExecutionActionWithThreeArgWithoutTestName"));
        store.setActionWithThreeArgAndCommentCmpFile(
                convert("TestCaseExecutionActionWithThreeArgAndCommentWithoutTestName"));
        store.setCommentOnlyCmpFile(convert("TestCaseExecutionCommentOnlyWithoutTestName"));
        store.setEmptyLineCmpFile(convert("TestCaseExecutionEmptyLineWithoutTestName"));

        return store;
    }

    public String convert(final String fileName) {
        return PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
