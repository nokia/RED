/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsTestTeardownTest {

    private static final String PRETTY_NEW_DIR_LOCATION = "settings//testTeardown//new//";

    @Test
    public void test_emptyFile_and_thanCreateTestTeardown() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyTestTeardownDeclarationOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newTestTeardown();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTeardown_andAddComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTeardownDeclarationWithCommentsOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTeardown testTeardown = settingTable.newTestTeardown();

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTeardown.addCommentPart(cm1);
        testTeardown.addCommentPart(cm2);
        testTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTeardown_andKeywordOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTeardownDeclarationWithKeywordOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTeardown testTeardown = settingTable.newTestTeardown();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTeardown.setKeywordName(keyword);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTeardown_andKeyword_andComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTeardownDeclarationWithKeywordCommentsOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTeardown testTeardown = settingTable.newTestTeardown();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTeardown.setKeywordName(keyword);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTeardown.addCommentPart(cm1);
        testTeardown.addCommentPart(cm2);
        testTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTeardown_andKeyword_andThreeArgs() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTeardownDeclarationWithKeyword3ArgsOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTeardown testTeardown = settingTable.newTestTeardown();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTeardown.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        testTeardown.addArgument(arg1);
        testTeardown.addArgument(arg2);
        testTeardown.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTeardown_andKeyword_andThreeArgs_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTeardownDeclarationWithKeyword3ArgsAndCommentOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTeardown testTeardown = settingTable.newTestTeardown();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTeardown.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        testTeardown.addArgument(arg1);
        testTeardown.addArgument(arg2);
        testTeardown.addArgument(arg3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTeardown.addCommentPart(cm1);
        testTeardown.addCommentPart(cm2);
        testTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }
}
