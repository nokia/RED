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
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfSettingsSuiteSetupTest extends RobotFormatParameterizedTest {

    public CreationOfSettingsSuiteSetupTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateSuiteSetup() throws Exception {
        // prepare
        final String fileName = convert("EmptySuiteSetupDeclarationOnly");
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
        final String fileName = convert("SuiteSetupDeclarationWithCommentsOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

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
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteSetup_andKeywordOnly() throws Exception {
        // prepare
        final String fileName = convert("SuiteSetupDeclarationWithKeywordOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteSetup.setKeywordName(keyword);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteSetup_andKeyword_andComments() throws Exception {
        // prepare
        final String fileName = convert("SuiteSetupDeclarationWithKeywordCommentsOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteSetup.setKeywordName(keyword);

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
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteSetup_andKeyword_andThreeArgs() throws Exception {
        // prepare
        final String fileName = convert("SuiteSetupDeclarationWithKeyword3ArgsOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteSetup.setKeywordName(keyword);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
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
        final String fileName = convert("SuiteSetupDeclarationWithKeyword3ArgsAndCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteSetup suiteSetup = settingTable.newSuiteSetup();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteSetup.setKeywordName(keyword);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        suiteSetup.addArgument(arg1);
        suiteSetup.addArgument(arg2);
        suiteSetup.addArgument(arg3);

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
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName) {
        return "settings/suiteSetup/new/" + fileName + "." + getExtension();
    }
}
