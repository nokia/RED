/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.update;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class AUpdateOfSettingsSuiteSetupTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//suiteSetup//update//";

    private final String extension;

    public AUpdateOfSettingsSuiteSetupTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_createArgumentOnPosition_2and3_whichNotExists_shouldReturnSingleLineSuiteSetup() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION
                + "Input_TwoSuiteSetups_commonViewUpdatedByArgInNotExistingPosition." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION
                + "Output_TwoSuiteSetups_commonViewUpdatedByArgInNotExistingPosition." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.suiteSetup().get();
        suiteSetup.setArgument(2, "arg3");
        suiteSetup.setArgument(3, "");

        // verify
        assertThat(settingTable.getSuiteSetups()).hasSize(1);
        SuiteSetup suiteSetupCurrent = settingTable.getSuiteSetups().get(0);
        assertThat(suiteSetupCurrent).isSameAs(suiteSetup);
        assertThat(suiteSetupCurrent.getArguments()).hasSize(4);
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_emptyFile_updateSuiteSetup_withSetArgumentToEmptyValue() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_SuiteSetupWithKeywordAndTwoArgumentsOneToRemove."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION
                + "Output_SuiteSetupWithKeywordAndTwoArgumentsOneToRemove." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.getSuiteSetups().get(0);
        suiteSetup.setArgument(0, "");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
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

    @Test
    public void test_emptyFile_updateSuiteSetup_andCommentOnly() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_CommentSetNoOtherElements." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_CommentSetNoOtherElements." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.getSuiteSetups().get(0);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteSetup.addCommentPart(cm1);
        suiteSetup.addCommentPart(cm2);
        suiteSetup.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_emptyFile_updateSuiteSetup_withKeywordExists_andCommentOnly() throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_KeywordExistsAndCommentSetNoOtherElements."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Output_KeywordExistsAndCommentSetNoOtherElements."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.getSuiteSetups().get(0);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteSetup.addCommentPart(cm1);
        suiteSetup.addCommentPart(cm2);
        suiteSetup.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_emptyFile_updateSuiteSetup_withKeywordExists_withArgToAdd_andCommentAlreadyInside()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION + "Input_KeywordArgumentInDifferentLine." + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION + "Ouput_KeywordArgumentInDifferentLine."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.getSuiteSetups().get(0);

        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");

        suiteSetup.addArgument(arg2);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
