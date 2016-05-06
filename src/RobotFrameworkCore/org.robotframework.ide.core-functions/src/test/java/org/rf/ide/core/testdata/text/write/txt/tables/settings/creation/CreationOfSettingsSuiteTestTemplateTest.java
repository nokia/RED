/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsSuiteTestTemplateTest {

    private static final String PRETTY_NEW_DIR_LOCATION = "settings//testTemplate//new//";

    @Test
    public void test_emptyFile_and_thanCreateTestTemplate() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyTestTemplateDeclarationOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newTestTemplate();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_andAddComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTemplateDeclarationWithCommentsOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTemplate.addCommentPart(cm1);
        testTemplate.addCommentPart(cm2);
        testTemplate.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_withKeywordName() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTemplateDeclarationWithKeywordOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTemplate.setKeywordName(keyword);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_withKeywordName_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTemplateDeclarationWithKeywordCommentOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTemplate.setKeywordName(keyword);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTemplate.addCommentPart(cm1);
        testTemplate.addCommentPart(cm2);
        testTemplate.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_withKeywordName_andThreeUnwanted() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTemplateDeclarationWithKeyword3UnwantedArgsOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTemplate.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("unArg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("unArg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("unArg3");

        testTemplate.addUnexpectedTrashArgument(arg1);
        testTemplate.addUnexpectedTrashArgument(arg2);
        testTemplate.addUnexpectedTrashArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_withKeywordName_andThreeUnwanted_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION
                + "TestTemplateDeclarationWithKeyword3UnwantedArgsCommentOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTemplate.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("unArg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("unArg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("unArg3");

        testTemplate.addUnexpectedTrashArgument(arg1);
        testTemplate.addUnexpectedTrashArgument(arg2);
        testTemplate.addUnexpectedTrashArgument(arg3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTemplate.addCommentPart(cm1);
        testTemplate.addCommentPart(cm2);
        testTemplate.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }
}
