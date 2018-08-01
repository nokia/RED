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
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class UpdateOfSettingsSuiteSetupTest extends RobotFormatParameterizedTest {

    public UpdateOfSettingsSuiteSetupTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_createArgumentOnPosition_2and3_whichNotExists_shouldReturnSingleLineSuiteSetup() throws Exception {
        // prepare
        final String inFileName = convert("Input_TwoSuiteSetups_commonViewUpdatedByArgInNotExistingPosition");
        final String outputFileName = convert("Output_TwoSuiteSetups_commonViewUpdatedByArgInNotExistingPosition");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getParser("2.9"));

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.getSuiteSetupsViews().get(0);
        suiteSetup.setArgument(2, "arg3");
        suiteSetup.setArgument(3, "");

        // verify
        assertThat(settingTable.getSuiteSetups()).hasSize(1);
        final SuiteSetup suiteSetupCurrent = settingTable.getSuiteSetups().get(0);
        assertThat(suiteSetupCurrent).isSameAs(suiteSetup);
        assertThat(suiteSetupCurrent.getArguments()).hasSize(4);
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_emptyFile_updateSuiteSetup_withSetArgumentToEmptyValue() throws Exception {
        // prepare
        final String inFileName = convert("Input_SuiteSetupWithKeywordAndTwoArgumentsOneToRemove");
        final String outputFileName = convert("Output_SuiteSetupWithKeywordAndTwoArgumentsOneToRemove");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getParser("2.9"));

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
        final String inFileName = convert("Input_KeywordSetNoOtherElements");
        final String outputFileName = convert("Output_KeywordSetNoOtherElements");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getParser("2.9"));

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
        final String inFileName = convert("Input_CommentSetNoOtherElements");
        final String outputFileName = convert("Output_CommentSetNoOtherElements");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getParser("2.9"));

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.getSuiteSetups().get(0);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
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
        final String inFileName = convert("Input_KeywordExistsAndCommentSetNoOtherElements");
        final String outputFileName = convert("Output_KeywordExistsAndCommentSetNoOtherElements");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getParser("2.9"));

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.getSuiteSetups().get(0);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
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
        final String inFileName = convert("Input_KeywordArgumentInDifferentLine");
        final String outputFileName = convert("Ouput_KeywordArgumentInDifferentLine");
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile,
                RobotModelTestProvider.getParser("2.9"));

        // test data prepare
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.getSuiteSetups().get(0);

        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");

        suiteSetup.addArgument(arg2);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    private String convert(final String fileName) {
        return "settings/suiteSetup/update/" + fileName + "." + getExtension();
    }
}
