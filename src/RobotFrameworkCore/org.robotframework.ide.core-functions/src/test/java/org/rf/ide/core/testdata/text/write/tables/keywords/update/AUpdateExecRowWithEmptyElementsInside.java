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
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public abstract class AUpdateExecRowWithEmptyElementsInside {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//new//";

    private final String extension;

    public AUpdateExecRowWithEmptyElementsInside(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_forIssueRelatedToDump() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_OneKeywordWithSpaceBeforeTestCaseHeader."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_OneKeywordWithSpaceBeforeTestCaseHeader."
                + getExtension();

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute
        final UserKeyword userKeyword = modelFile.getKeywordTable().getKeywords().get(0);
        final RobotExecutableRow<UserKeyword> newRow = new RobotExecutableRow<>();
        newRow.setAction(RobotToken.create("Log"));
        newRow.addArgument(RobotToken.create("ok"));
        userKeyword.addKeywordExecutionRow(newRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_forIssueRelatedToEndPositionCheck() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_OneKeywordWithSpaceBeforeTestCaseHeader."
                + getExtension();

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute
        final UserKeyword userKeyword = modelFile.getKeywordTable().getKeywords().get(0);
        final RobotExecutableRow<UserKeyword> newRow = new RobotExecutableRow<>();
        newRow.setAction(RobotToken.create("Log"));
        newRow.addArgument(RobotToken.create("ok"));
        userKeyword.addKeywordExecutionRow(newRow);

        DumpedResult dumpToResultObject = new RobotFileDumper().dumpToResultObject(modelFile.getParent());
        QuickTokenListenerBaseTwoModelReferencesLinker linker = new QuickTokenListenerBaseTwoModelReferencesLinker();
        linker.update(modelFile.getParent(), dumpToResultObject);

        // verify
        final FilePosition endPositionUK = userKeyword.getEndPosition();
        assertThat(endPositionUK).isNotNull();
        assertThat(endPositionUK.getOffset()).isEqualTo(111);
    }

    public String convert(final String fileName) {
        return PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
