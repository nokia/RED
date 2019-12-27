/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.update;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.DumpContext;
import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.mapping.QuickTokenListenerBaseTwoModelReferencesLinker;
import org.rf.ide.core.testdata.model.FileFormat;
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
public class UpdateExecRowWithEmptyElementsInsideTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_forIssueRelatedToDump(final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeywordWithSpaceBeforeTestCaseHeader", format);
        final String outputFileName = convert("Output_OneKeywordWithSpaceBeforeTestCaseHeader", format);

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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_forIssueRelatedToEndPositionCheck(final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_OneKeywordWithSpaceBeforeTestCaseHeader", format);

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute
        final UserKeyword userKeyword = modelFile.getKeywordTable().getKeywords().get(0);
        final RobotExecutableRow<UserKeyword> newRow = new RobotExecutableRow<>();
        newRow.setAction(RobotToken.create("Log"));
        newRow.addArgument(RobotToken.create("ok"));
        userKeyword.addElement(newRow);

        final DumpedResult dumpToResultObject = new RobotFileDumper().dump(new DumpContext(), modelFile.getParent());
        final QuickTokenListenerBaseTwoModelReferencesLinker linker = new QuickTokenListenerBaseTwoModelReferencesLinker();
        linker.update(modelFile.getParent(), dumpToResultObject);

        // verify
        final FilePosition endPositionUK = userKeyword.getEndPosition();
        assertThat(endPositionUK).isNotNull();
        assertThat(endPositionUK.getOffset()).isEqualTo(116);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/new/" + fileName + "." + format.getExtension();
    }
}
