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
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public abstract class ACreationOfKeywordsWithSeparatorCheckTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//new//";

    private final String extension;

    public ACreationOfKeywordsWithSeparatorCheckTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_givenKeywordTable_withOneKeywordMultilined_whenAddNewParameterToExecutable_PIPEsep_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "InputOneKeywordWithExecMultiline." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "OutputOneKeywordWithExecMultiline." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator(" | ");

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        keyword.getKeywordExecutionRows().get(0).addArgument(RobotToken.create("INFO"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @Test
    public void test_givenKeywordTable_withOneKeyword_whenAddNewParameterToExecutable_TabulatorSep_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_OneKeyword_andOneExec_whenAddNewParameter."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_OneKeyword_andOneExec_whenAddNewParameterPipe."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator("\t");

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        keyword.getKeywordExecutionRows().get(0).addArgument(RobotToken.create("INFO"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @Test
    public void test_givenKeywordTable_withOneKeyword_whenAddNewParameterToExecutable_PIPEsep_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_OneKeyword_andOneExec_whenAddNewParameter."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_OneKeyword_andOneExec_whenAddNewParameterPipe."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator(" | ");

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        keyword.getKeywordExecutionRows().get(0).addArgument(RobotToken.create("INFO"));

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @Test
    public void test_givenKeywordTable_withOneKeyword_whenAddNewdExecutable_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_OneKeyword_andThenAddNewExec." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_OneKeyword_andThenAddNewExec." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator(" | ");

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.getKeywords().get(0);
        RobotExecutableRow<UserKeyword> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log"));
        executionRow.addArgument(RobotToken.create("done EON"));
        keyword.addKeywordExecutionRow(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    @Test
    public void test_givenEmptyKeywordTable_whenAddNewKeyword_andExecutable_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION
                + "Input_OnlyHeaderOfKeyword_andThenAddNewKeyword_andOneExecLine_sepSpacePipeSpace." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION
                + "Output_OnlyHeaderOfKeyword_andThenAddNewKeyword_andOneExecLine_sepSpacePipeSpace." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        final DumpContext ctx = new DumpContext();
        ctx.setPreferedSeparator(" | ");

        // test data prepare
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final UserKeyword keyword = keywordTable.createUserKeyword("KeywordT");
        RobotExecutableRow<UserKeyword> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log"));
        executionRow.addArgument(RobotToken.create("done EON"));
        keyword.addKeywordExecutionRow(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile, ctx);
    }

    public String getExtension() {
        return extension;
    }
}
