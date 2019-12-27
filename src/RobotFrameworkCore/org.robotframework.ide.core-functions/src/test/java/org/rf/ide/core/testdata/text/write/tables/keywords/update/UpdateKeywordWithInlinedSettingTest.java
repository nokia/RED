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
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class UpdateKeywordWithInlinedSettingTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_forIssueRelatedToDump(final FileFormat format) throws Exception {
        // prepare
        final String inFileName = convert("Output_OneKeywordWithInlinedSetting", format);
        final String outputFileName = convert("Output_OneKeywordWithInlinedSetting", format);

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute
        final UserKeyword keyword = modelFile.getKeywordTable().getKeywords().get(0);
        final AModelElement<UserKeyword> inlinedSetting = keyword.getElements().get(0);
        final RobotToken inlinedSettingText = inlinedSetting.getElementTokens().get(1);
        inlinedSettingText.setText("modified");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/setting/" + fileName + "." + format.getExtension();
    }
}
