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

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//exec//new//oneTc//oneExec//";

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

        store.setActionOnlyCmpFile(convert("ExecActionOnly"));
        store.setActionWithCommentCmpFile(convert("ExecActionWithComment"));
        store.setActionWithOneArgCmpFile(convert("ExecActionWithOneArg"));
        store.setActionWithOneArgAndCommentCmpFile(convert("ExecActionWithOneArgAndComment"));
        store.setActionWithThreeArgCmpFile(convert("ExecActionWithThreeArg"));
        store.setActionWithThreeArgAndCommentCmpFile(convert("ExecActionWithThreeArgAndComment"));
        store.setCommentOnlyCmpFile(convert("ExecCommentOnly"));
        store.setEmptyLineCmpFile(convert("ExecEmptyLine"));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setActionOnlyCmpFile(convert("ExecActionOnlyWithoutTestName"));
        store.setActionWithCommentCmpFile(convert("ExecActionWithCommentWithoutTestName"));
        store.setActionWithOneArgCmpFile(convert("ExecActionWithOneArgWithoutTestName"));
        store.setActionWithOneArgAndCommentCmpFile(convert("ExecActionWithOneArgAndCommentWithoutTestName"));
        store.setActionWithThreeArgCmpFile(convert("ExecActionWithThreeArgWithoutTestName"));
        store.setActionWithThreeArgAndCommentCmpFile(convert("ExecActionWithThreeArgAndCommentWithoutTestName"));
        store.setCommentOnlyCmpFile(convert("ExecCommentOnlyWithoutTestName"));
        store.setEmptyLineCmpFile(convert("ExecEmptyLineWithoutTestName"));

        return store;
    }

    public String convert(final String fileName) {
        return PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
