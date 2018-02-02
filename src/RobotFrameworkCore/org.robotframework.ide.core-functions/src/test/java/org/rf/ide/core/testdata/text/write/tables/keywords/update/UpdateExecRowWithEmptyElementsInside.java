/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.update;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.mapping.QuickTokenListenerBaseTwoModelReferencesLinker;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
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
public class UpdateExecRowWithEmptyElementsInside extends RobotFormatParameterizedTest {

    public UpdateExecRowWithEmptyElementsInside(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_forIssueRelatedToDump() throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeywordWithSpaceBeforeTestCaseHeader");
        final String outputFileName = convert("Output_OneKeywordWithSpaceBeforeTestCaseHeader");

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute
        final UserKeyword userKeyword = modelFile.getKeywordTable().getKeywords().get(0);
        final RobotExecutableRow<UserKeyword> newRow = new RobotExecutableRow<>();
        newRow.setAction(RobotToken.create("Log"));
        newRow.addArgument(RobotToken.create("ok"));
        userKeyword.addElement(newRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_forIssueRelatedToEndPositionCheck() throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeywordWithSpaceBeforeTestCaseHeader");

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute
        final UserKeyword userKeyword = modelFile.getKeywordTable().getKeywords().get(0);
        final RobotExecutableRow<UserKeyword> newRow = new RobotExecutableRow<>();
        newRow.setAction(RobotToken.create("Log"));
        newRow.addArgument(RobotToken.create("ok"));
        userKeyword.addElement(newRow);

        final DumpedResult dumpToResultObject = new RobotFileDumper().dumpToResultObject(modelFile.getParent());
        final QuickTokenListenerBaseTwoModelReferencesLinker linker = new QuickTokenListenerBaseTwoModelReferencesLinker();
        linker.update(modelFile.getParent(), dumpToResultObject);

        // verify
        final FilePosition endPositionUK = userKeyword.getEndPosition();
        assertThat(endPositionUK).isNotNull();
        assertThat(endPositionUK.getOffset()).isEqualTo(113);
    }

    private String convert(final String fileName) {
        return "keywords/new/" + fileName + "." + getExtension();
    }
}
