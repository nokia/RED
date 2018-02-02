/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfSettingsSuiteTestTemplateTest extends RobotFormatParameterizedTest {

    public CreationOfSettingsSuiteTestTemplateTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTemplate() throws Exception {
        // prepare
        final String fileName = convert("EmptyTestTemplateDeclarationOnly");
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
        final String fileName = convert("TestTemplateDeclarationWithCommentsOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
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
        final String fileName = convert("TestTemplateDeclarationWithKeywordOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTemplate.setKeywordName(keyword);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_withKeywordName_andComment() throws Exception {
        // prepare
        final String fileName = convert("TestTemplateDeclarationWithKeywordCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTemplate.setKeywordName(keyword);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
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
        final String fileName = convert("TestTemplateDeclarationWithKeyword3UnwantedArgsOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTemplate.setKeywordName(keyword);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("unArg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("unArg2");
        final RobotToken arg3 = new RobotToken();
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
        final String fileName = convert("TestTemplateDeclarationWithKeyword3UnwantedArgsCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTemplate testTemplate = settingTable.newTestTemplate();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        testTemplate.setKeywordName(keyword);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("unArg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("unArg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("unArg3");

        testTemplate.addUnexpectedTrashArgument(arg1);
        testTemplate.addUnexpectedTrashArgument(arg2);
        testTemplate.addUnexpectedTrashArgument(arg3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTemplate.addCommentPart(cm1);
        testTemplate.addCommentPart(cm2);
        testTemplate.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName) {
        return "settings/testTemplate/new/" + fileName + "." + getExtension();
    }
}
