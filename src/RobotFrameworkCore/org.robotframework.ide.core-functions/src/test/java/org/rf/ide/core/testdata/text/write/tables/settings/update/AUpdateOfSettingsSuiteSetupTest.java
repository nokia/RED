/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.update;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class AUpdateOfSettingsSuiteSetupTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//suiteSetup//update//";

    private final String extension;

    public AUpdateOfSettingsSuiteSetupTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_updateSuiteSetup_andKeywordName() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_KeywordSetNoOtherElements." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_KeywordSetNoOtherElements." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.getSuiteSetups().get(0);
        suiteSetup.setKeywordName("key");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
