/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfExecutionRowTest;

public class CreationOfKeywordExecutableOneStepTest extends ACreationOfExecutionRowTest {

    public CreationOfKeywordExecutableOneStepTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public IExecutableStepsHolder getExecutableWithName() {
        final UserKeyword execUnit = createModelWithOneKeywordInside();
        execUnit.getKeywordName().setText("UserKeyword");

        return execUnit;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public IExecutableStepsHolder getExecutableWithoutName() {
        return createModelWithOneKeywordInside();
    }

    private UserKeyword createModelWithOneKeywordInside() {
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");
        modelFile.includeKeywordTableSection();
        final KeywordTable keywordTable = modelFile.getKeywordTable();

        final RobotToken keywordName = new RobotToken();
        final UserKeyword execUnit = new UserKeyword(keywordName);
        execUnit.addElement(new RobotExecutableRow<UserKeyword>());
        keywordTable.addKeyword(execUnit);

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

        store.setActionOnlyCmpFile(convert("ExecActionOnlyWithoutKeywordName"));
        store.setActionWithCommentCmpFile(convert("ExecActionWithCommentWithoutKeywordName"));
        store.setActionWithOneArgCmpFile(convert("ExecActionWithOneArgWithoutKeywordName"));
        store.setActionWithOneArgAndCommentCmpFile(convert("ExecActionWithOneArgAndCommentWithoutKeywordName"));
        store.setActionWithThreeArgCmpFile(convert("ExecActionWithThreeArgWithoutKeywordName"));
        store.setActionWithThreeArgAndCommentCmpFile(convert("ExecActionWithThreeArgAndCommentWithoutKeywordName"));
        store.setCommentOnlyCmpFile(convert("ExecCommentOnlyWithoutKeywordName"));
        store.setEmptyLineCmpFile(convert("ExecEmptyLineWithoutKeywordName"));

        return store;
    }

    private String convert(final String fileName) {
        return "keywords/exec/new/oneKw/oneExec/" + fileName + "." + getExtension();
    }
}
