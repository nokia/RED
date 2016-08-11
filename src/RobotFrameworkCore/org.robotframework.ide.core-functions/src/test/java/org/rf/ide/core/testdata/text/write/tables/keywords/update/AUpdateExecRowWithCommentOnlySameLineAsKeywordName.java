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
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public abstract class AUpdateExecRowWithCommentOnlySameLineAsKeywordName {

    public static final String PRETTY_NEW_DIR_LOCATION_NEW_UNITS = "keywords//new//";

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//exec//update//";

    private final String extension;

    public AUpdateExecRowWithCommentOnlySameLineAsKeywordName(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_update_addingNewKeyword() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Input_ThreeKeywordsAndAddingNewEmptyOne."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Output_ThreeKeywordsAndAddingNewEmptyOne."
                + getExtension();

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        KeywordTable table = modelFile.getKeywordTable();
        table.createUserKeyword("key 3");

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
        KeywordTable table = modelFile.getKeywordTable();
        List<UserKeyword> keywords = table.getKeywords();
        UserKeyword userKeyword = keywords.get(0);
        userKeyword.getKeywordExecutionRows().get(0).setAction(RobotToken.create("keyAdded"));

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(convert("OutKeywordWithTheFirstCommentLineInTheNameLine"),
                modelFile);
    }

    public String convert(final String fileName) {
        return PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
