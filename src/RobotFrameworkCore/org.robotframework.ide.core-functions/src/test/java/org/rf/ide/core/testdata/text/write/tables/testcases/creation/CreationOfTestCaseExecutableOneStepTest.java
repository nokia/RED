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
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfExecutionRowTest;

public class CreationOfTestCaseExecutableOneStepTest extends ACreationOfExecutionRowTest {

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
        testCaseTable.addTest(execUnit);

        return execUnit;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithName(final FileFormat format) {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setActionOnlyCmpFile(convert("ExecActionOnly", format));
        store.setActionWithCommentCmpFile(convert("ExecActionWithComment", format));
        store.setActionWithOneArgCmpFile(convert("ExecActionWithOneArg", format));
        store.setActionWithOneArgAndCommentCmpFile(convert("ExecActionWithOneArgAndComment", format));
        store.setActionWithThreeArgCmpFile(convert("ExecActionWithThreeArg", format));
        store.setActionWithThreeArgAndCommentCmpFile(convert("ExecActionWithThreeArgAndComment", format));
        store.setCommentOnlyCmpFile(convert("ExecCommentOnly", format));
        store.setEmptyLineCmpFile(convert("ExecEmptyLine", format));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName(final FileFormat format) {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setActionOnlyCmpFile(convert("ExecActionOnlyWithoutTestName", format));
        store.setActionWithCommentCmpFile(convert("ExecActionWithCommentWithoutTestName", format));
        store.setActionWithOneArgCmpFile(convert("ExecActionWithOneArgWithoutTestName", format));
        store.setActionWithOneArgAndCommentCmpFile(convert("ExecActionWithOneArgAndCommentWithoutTestName", format));
        store.setActionWithThreeArgCmpFile(convert("ExecActionWithThreeArgWithoutTestName", format));
        store.setActionWithThreeArgAndCommentCmpFile(
                convert("ExecActionWithThreeArgAndCommentWithoutTestName", format));
        store.setCommentOnlyCmpFile(convert("ExecCommentOnlyWithoutTestName", format));
        store.setEmptyLineCmpFile(convert("ExecEmptyLineWithoutTestName", format));

        return store;
    }

    private String convert(final String fileName, final FileFormat format) {
        return "testCases/exec/new/oneTc/oneExec/" + fileName + "." + format.getExtension();
    }
}
