/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.update;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

/**
 * @author wypych
 */
public class UpdateExecRowWithCommentOnlySameLineAsKeywordName extends RobotFormatParameterizedTest {

    public UpdateExecRowWithCommentOnlySameLineAsKeywordName(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_update_forLoopFix() throws Exception {
        // prepare
        final String inFileName = convert("Input_ForWithLineContinueAndHashes");
        final String outputFileName = convert("Output_ForWithLineContinueAndHashes");

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void updateTheFirstExecLineWithCommentOnly_and_keywordNameInSameLine() throws Exception {
        // prepare
        final String filePath = convert("InKeywordWithTheFirstCommentLineInTheNameLine");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(filePath);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = table.getKeywords();
        final UserKeyword userKeyword = keywords.get(0);
        userKeyword.getExecutionContext().get(0).setAction(RobotToken.create("keyAdded"));

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(convert("OutKeywordWithTheFirstCommentLineInTheNameLine"),
                modelFile);
    }

    @Test
    public void updateTheFirstExecLineWithPrettyAlignBeforeTheFirstActionVariable_then_updateArgumentOfKeyword()
            throws Exception {
        // prepare
        final String filePath = convert("InKeywordsWithPrettyAlign");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(filePath);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = table.getKeywords();
        final UserKeyword userKeyword = keywords.get(0);
        userKeyword.getExecutionContext().get(0).getArguments().get(1).setText("d_new");

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(convert("OutKeywordsWithPrettyAlign"), modelFile);
    }

    @Test
    public void updateTheFirstExecLineWithPrettyAlignBeforeTheFirstActionVariable_then_updateArgumentOfKeyword_usingModelUpdater()
            throws Exception {
        // prepare
        final String filePath = convert("InKeywordsWithPrettyAlign");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(filePath);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = table.getKeywords();
        final UserKeyword userKeyword = keywords.get(0);

        final KeywordExecutableRowModelOperation execKeyUpdater = new KeywordExecutableRowModelOperation();

        final RobotExecutableRow<UserKeyword> execOneRow = userKeyword.getExecutionContext().get(0);
        execOneRow.getAction().setText("${c} =");
        execKeyUpdater.update(execOneRow, 1, "d_new");

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(convert("OutKeywordsWithPrettyAlign"), modelFile);
    }

    @Test
    public void givenKeywordTableAndThenThreeEmptyLines_then_updateTheFirstExecLineWithPrettyAlignBeforeTheFirstActionVariable_then_updateArgumentOfKeyword_usingModelUpdater()
            throws Exception {
        // prepare
        final String filePath = convert("InKeywordsWithPrettyAlignWithThreeEmptyLinesBefore");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(filePath);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getLazyParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = table.getKeywords();
        final UserKeyword userKeyword = keywords.get(0);

        final KeywordExecutableRowModelOperation execKeyUpdater = new KeywordExecutableRowModelOperation();

        final RobotExecutableRow<UserKeyword> execOneRow = userKeyword.getExecutionContext().get(0);
        execOneRow.getAction().setText("${c} =");
        execKeyUpdater.update(execOneRow, 1, "d_new");

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(
                convert("OutKeywordsWithPrettyAlignWithThreeEmptyLinesBefore"), modelFile);
    }

    private String convert(final String fileName) {
        return "keywords/exec/update/" + fileName + "." + getExtension();
    }
}
