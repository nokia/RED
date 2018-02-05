/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.DumpContext;
import org.rf.ide.core.testdata.model.RobotFile;
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
public class CreationOfKeywordsWithSeparatorCheckTest extends RobotFormatParameterizedTest {

    public CreationOfKeywordsWithSeparatorCheckTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_givenKeywordTable_withOneKeywordMultilined_whenAddNewParameterToExecutable_PIPEsep_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("InputOneKeywordWithExecMultiline");
        final String outputFileName = convert("OutputOneKeywordWithExecMultiline");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getLazyParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator(" | ");

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        keyword.getExecutionContext().get(0).addArgument(RobotToken.create("INFO"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @Test
    public void test_givenKeywordTable_withOneKeyword_whenAddNewParameterToExecutable_TabulatorSep_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeyword_andOneExec_whenAddNewParameter");
        final String outputFileName = convert("Output_OneKeyword_andOneExec_whenAddNewParameterPipe");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getLazyParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator("\t");

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        keyword.getExecutionContext().get(0).addArgument(RobotToken.create("INFO"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @Test
    public void test_givenKeywordTable_withOneKeyword_whenAddNewParameterToExecutable_PIPEsep_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeyword_andOneExec_whenAddNewParameter");
        final String outputFileName = convert("Output_OneKeyword_andOneExec_whenAddNewParameterPipe");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getLazyParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator(" | ");

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        keyword.getExecutionContext().get(0).addArgument(RobotToken.create("INFO"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @Test
    public void test_givenKeywordTable_withOneKeyword_whenAddNewExecutable_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeyword_andThenAddNewExec");
        final String outputFileName = convert("Output_OneKeyword_andThenAddNewExec");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getLazyParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator(" | ");

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

    @Test
    public void test_givenEmptyKeywordTable_whenAddNewKeyword_andExecutable_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = convert(
                "Input_OnlyHeaderOfKeyword_andThenAddNewKeyword_andOneExecLine_sepSpacePipeSpace");
        final String outputFileName = convert(
                "Output_OnlyHeaderOfKeyword_andThenAddNewKeyword_andOneExecLine_sepSpacePipeSpace");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getLazyParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator(" | ");

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

    private String convert(final String fileName) {
        return "keywords/new/" + fileName + "." + getExtension();
    }
}
