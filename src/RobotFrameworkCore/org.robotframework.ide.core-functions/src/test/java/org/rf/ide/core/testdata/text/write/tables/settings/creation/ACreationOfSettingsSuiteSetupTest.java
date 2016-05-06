/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfSettingsSuiteSetupTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//suiteSetup//new//";

    private final String extension;

    public ACreationOfSettingsSuiteSetupTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateSuiteSetup() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptySuiteSetupDeclarationOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newSuiteSetup();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteSetup_andAddComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteSetupDeclarationWithCommentsOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

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
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteSetup_andKeywordOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteSetupDeclarationWithKeywordOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteSetup.setKeywordName(keyword);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteSetup_andKeyword_andComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteSetupDeclarationWithKeywordCommentsOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteSetup.setKeywordName(keyword);

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
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteSetup_andKeyword_andThreeArgs() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteSetupDeclarationWithKeyword3ArgsOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteSetup.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        suiteSetup.addArgument(arg1);
        suiteSetup.addArgument(arg2);
        suiteSetup.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteSetup_andKeyword_andThreeArgs_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteSetupDeclarationWithKeyword3ArgsAndCommentOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteSetup.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        suiteSetup.addArgument(arg1);
        suiteSetup.addArgument(arg2);
        suiteSetup.addArgument(arg3);

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
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
