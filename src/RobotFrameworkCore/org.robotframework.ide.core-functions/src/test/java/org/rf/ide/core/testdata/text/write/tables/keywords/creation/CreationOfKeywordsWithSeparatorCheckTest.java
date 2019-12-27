/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.DumpContext;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public class CreationOfKeywordsWithSeparatorCheckTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenKeywordTable_withOneKeywordMultilined_whenAddNewParameterToExecutable_PIPEsep_thenCheckIfTableIsCorrectlyDumped(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("InputOneKeywordWithExecMultiline", format);
        final String outputFileName = convert("OutputOneKeywordWithExecMultiline", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext(" | ", true);

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        keyword.getExecutionContext().get(0).addArgument(RobotToken.create("INFO"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenKeywordTable_withOneKeyword_whenAddNewParameterToExecutable_TabulatorSep_thenCheckIfTableIsCorrectlyDumped(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeyword_andOneExec_whenAddNewParameter", format);
        final String outputFileName = convert("Output_OneKeyword_andOneExec_whenAddNewParameterPipe", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext("\t", true);

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        keyword.getExecutionContext().get(0).addArgument(RobotToken.create("INFO"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenKeywordTable_withOneKeyword_whenAddNewParameterToExecutable_PIPEsep_thenCheckIfTableIsCorrectlyDumped(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeyword_andOneExec_whenAddNewParameter", format);
        final String outputFileName = convert("Output_OneKeyword_andOneExec_whenAddNewParameterPipe", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext(" | ", true);

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        keyword.getExecutionContext().get(0).addArgument(RobotToken.create("INFO"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenKeywordTable_withOneKeyword_whenAddNewExecutable_thenCheckIfTableIsCorrectlyDumped(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeyword_andThenAddNewExec", format);
        final String outputFileName = convert("Output_OneKeyword_andThenAddNewExec", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext(" | ", true);

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        final RobotExecutableRow<UserKeyword> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log"));
        executionRow.addArgument(RobotToken.create("done EON"));
        keyword.addElement(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_givenEmptyKeywordTable_whenAddNewKeyword_andExecutable_thenCheckIfTableIsCorrectlyDumped(
            final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert(
                "Input_OnlyHeaderOfKeyword_andThenAddNewKeyword_andOneExecLine_sepSpacePipeSpace", format);
        final String outputFileName = convert(
                "Output_OnlyHeaderOfKeyword_andThenAddNewKeyword_andOneExecLine_sepSpacePipeSpace", format);
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext(" | ", true);

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.createUserKeyword("KeywordT");
        final RobotExecutableRow<UserKeyword> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log"));
        executionRow.addArgument(RobotToken.create("done EON"));
        keyword.addElement(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/new/" + fileName + "." + format.getExtension();
    }
}
