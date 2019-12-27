/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.update;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class UpdateKeywordsTableWithAddingNewKeywordTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_update_addingNewKeyword(final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Input_ThreeKeywordsAndAddingNewEmptyOne", format);
        final String outputFileName = convert("Output_ThreeKeywordsAndAddingNewEmptyOne", format);

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test prepare
        final KeywordTable table = modelFile.getKeywordTable();
        table.createUserKeyword("key 3");

        // execute & verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/new/" + fileName + "." + format.getExtension();
    }
}
