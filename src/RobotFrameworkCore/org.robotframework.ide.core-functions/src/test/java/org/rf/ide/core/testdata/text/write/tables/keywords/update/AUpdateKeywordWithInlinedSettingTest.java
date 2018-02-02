/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.update;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class AUpdateKeywordWithInlinedSettingTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//setting//";

    private final String extension;

    public AUpdateKeywordWithInlinedSettingTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_forIssueRelatedToDump() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Output_OneKeywordWithInlinedSetting." + extension;
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_OneKeywordWithInlinedSetting." + extension;

        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // execute
        final UserKeyword keyword = modelFile.getKeywordTable().getKeywords().get(0);
        final AModelElement<UserKeyword> inlinedSetting = keyword.getAllElements().get(0);
        final RobotToken inlinedSettingText = inlinedSetting.getElementTokens().get(1);
        inlinedSettingText.setText("modified");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }
}
