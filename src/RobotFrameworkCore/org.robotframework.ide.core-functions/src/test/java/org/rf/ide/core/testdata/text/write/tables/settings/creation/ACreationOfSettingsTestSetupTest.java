/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfSettingsTestSetupTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//testSetup//new//";

    private final String extension;

    public ACreationOfSettingsTestSetupTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateTestSetup() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyTestSetupDeclarationOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newTestSetup();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestSetup_andAddComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestSetupDeclarationWithCommentsOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestSetup testSetup = settingTable.newTestSetup();

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testSetup.addCommentPart(cm1);
        testSetup.addCommentPart(cm2);
        testSetup.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestSetup_andKeywordOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestSetupDeclarationWithKeywordOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestSetup testSetup = settingTable.newTestSetup();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testSetup.setKeywordName(keyword);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestSetup_andKeyword_andComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestSetupDeclarationWithKeywordCommentsOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestSetup testSetup = settingTable.newTestSetup();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testSetup.setKeywordName(keyword);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testSetup.addCommentPart(cm1);
        testSetup.addCommentPart(cm2);
        testSetup.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestSetup_andKeyword_andThreeArgs() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestSetupDeclarationWithKeyword3ArgsOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestSetup testSetup = settingTable.newTestSetup();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testSetup.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        testSetup.addArgument(arg1);
        testSetup.addArgument(arg2);
        testSetup.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestSetup_andKeyword_andThreeArgs_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestSetupDeclarationWithKeyword3ArgsAndCommentOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestSetup testSetup = settingTable.newTestSetup();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testSetup.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        testSetup.addArgument(arg1);
        testSetup.addArgument(arg2);
        testSetup.addArgument(arg3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testSetup.addCommentPart(cm1);
        testSetup.addCommentPart(cm2);
        testSetup.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
