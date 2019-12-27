/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.update;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public class UpdateExecRowWithCommentOnlySameLineAsKeywordNameTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_update_forLoopFix(final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_ForWithLineContinueAndHashes", format);
        final String outputFileName = convert("Output_ForWithLineContinueAndHashes", format);

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void updateTheFirstExecLineWithCommentOnly_and_keywordNameInSameLine(final FileFormat format)
            throws Exception {
        // prepare
        final String filePath = convert("InKeywordWithTheFirstCommentLineInTheNameLine", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(filePath);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = table.getKeywords();
        final UserKeyword userKeyword = keywords.get(0);
        ((RobotEmptyRow<UserKeyword>) userKeyword.getElements().get(0)).setEmpty(RobotToken.create("keyAdded"));

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(
                convert("OutKeywordWithTheFirstCommentLineInTheNameLine", format),
                modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void updateTheFirstExecLineWithPrettyAlignBeforeTheFirstActionVariable_then_updateArgumentOfKeyword(
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert("InKeywordsWithPrettyAlign", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(filePath);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = table.getKeywords();
        final UserKeyword userKeyword = keywords.get(0);
        userKeyword.getExecutionContext().get(0).getArguments().get(1).setText("d_new");

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(convert("OutKeywordsWithPrettyAlign", format), modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void updateTheFirstExecLineWithPrettyAlignBeforeTheFirstActionVariable_then_updateArgumentOfKeyword_usingModelUpdater(
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert("InKeywordsWithPrettyAlign", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(filePath);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = table.getKeywords();
        final UserKeyword userKeyword = keywords.get(0);

        final RobotExecutableRow<UserKeyword> execOneRow = userKeyword.getExecutionContext().get(0);
        execOneRow.updateToken(2, "d_new");

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(convert("OutKeywordsWithPrettyAlign", format), modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void givenKeywordTableAndThenThreeEmptyLines_then_updateTheFirstExecLineWithPrettyAlignBeforeTheFirstActionVariable_then_updateArgumentOfKeyword_usingModelUpdater(
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert("InKeywordsWithPrettyAlignWithThreeEmptyLinesBefore", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(filePath);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = table.getKeywords();
        final UserKeyword userKeyword = keywords.get(0);

        final RobotExecutableRow<UserKeyword> execOneRow = userKeyword.getExecutionContext().get(0);
        execOneRow.updateToken(2, "d_new");

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(
                convert("OutKeywordsWithPrettyAlignWithThreeEmptyLinesBefore", format), modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/exec/update/" + fileName + "." + format.getExtension();
    }
}
