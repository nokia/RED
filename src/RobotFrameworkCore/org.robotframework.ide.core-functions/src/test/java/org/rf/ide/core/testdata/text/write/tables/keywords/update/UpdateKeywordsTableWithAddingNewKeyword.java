/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.update;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class UpdateKeywordsTableWithAddingNewKeyword extends RobotFormatParameterizedTest {

    public UpdateKeywordsTableWithAddingNewKeyword(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_update_addingNewKeyword() throws Exception {
        // prepare
        final String inFileName = convert("Input_ThreeKeywordsAndAddingNewEmptyOne");
        final String outputFileName = convert("Output_ThreeKeywordsAndAddingNewEmptyOne");

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        table.createUserKeyword("key 3");

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName) {
        return "keywords/new/" + fileName + "." + getExtension();
    }
}
