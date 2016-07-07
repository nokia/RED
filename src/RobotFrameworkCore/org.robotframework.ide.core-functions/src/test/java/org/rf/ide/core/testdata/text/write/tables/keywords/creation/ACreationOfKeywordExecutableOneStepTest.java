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
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfExecutionRowTest;

public abstract class ACreationOfKeywordExecutableOneStepTest extends ACreationOfExecutionRowTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//exec//new//oneKeyword//oneExec//";

    private final String extension;

    public ACreationOfKeywordExecutableOneStepTest(final String extension) {
        this.extension = extension;
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
        execUnit.addKeywordExecutionRow(new RobotExecutableRow<UserKeyword>());
        keywordTable.addKeyword(execUnit);

        return execUnit;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setActionOnlyCmpFile(convert("KeywordExecutionActionOnly"));
        store.setActionWithCommentCmpFile(convert("KeywordExecutionActionWithComment"));
        store.setActionWithOneArgCmpFile(convert("KeywordExecutionActionWithOneArg"));
        store.setActionWithOneArgAndCommentCmpFile(convert("KeywordExecutionActionWithOneArgAndComment"));
        store.setActionWithThreeArgCmpFile(convert("KeywordExecutionActionWithThreeArg"));
        store.setActionWithThreeArgAndCommentCmpFile(convert("KeywordExecutionActionWithThreeArgAndComment"));
        store.setCommentOnlyCmpFile(convert("KeywordExecutionCommentOnly"));
        store.setEmptyLineCmpFile(convert("KeywordExecutionEmptyLine"));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setActionOnlyCmpFile(convert("KeywordExecutionActionOnlyWithoutKeywordName"));
        store.setActionWithCommentCmpFile(convert("KeywordExecutionActionWithCommentWithoutKeywordName"));
        store.setActionWithOneArgCmpFile(convert("KeywordExecutionActionWithOneArgWithoutKeywordName"));
        store.setActionWithOneArgAndCommentCmpFile(
                convert("KeywordExecutionActionWithOneArgAndCommentWithoutKeywordName"));
        store.setActionWithThreeArgCmpFile(convert("KeywordExecutionActionWithThreeArgWithoutKeywordName"));
        store.setActionWithThreeArgAndCommentCmpFile(
                convert("KeywordExecutionActionWithThreeArgAndCommentWithoutKeywordName"));
        store.setCommentOnlyCmpFile(convert("KeywordExecutionCommentOnlyWithoutKeywordName"));
        store.setEmptyLineCmpFile(convert("KeywordExecutionEmptyLineWithoutKeywordName"));

        return store;
    }

    public String convert(final String fileName) {
        return PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
