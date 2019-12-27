/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsSuiteTeardownTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateSuiteTeardown(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptySuiteTeardownDeclarationOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newSuiteTeardown();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createSuiteTeardown_andAddComments(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("SuiteTeardownDeclarationWithCommentsOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteTeardown.addCommentPart(cm1);
        suiteTeardown.addCommentPart(cm2);
        suiteTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createSuiteTeardown_andKeywordOnly(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("SuiteTeardownDeclarationWithKeywordOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteTeardown.setKeywordName(keyword);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createSuiteTeardown_andKeyword_andComments(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("SuiteTeardownDeclarationWithKeywordCommentsOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteTeardown.setKeywordName(keyword);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteTeardown.addCommentPart(cm1);
        suiteTeardown.addCommentPart(cm2);
        suiteTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createSuiteTeardown_andKeyword_andThreeArgs(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("SuiteTeardownDeclarationWithKeyword3ArgsOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteTeardown.setKeywordName(keyword);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        suiteTeardown.addArgument(arg1);
        suiteTeardown.addArgument(arg2);
        suiteTeardown.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createSuiteTeardown_andKeyword_andThreeArgs_andComment(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("SuiteTeardownDeclarationWithKeyword3ArgsAndCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteTeardown.setKeywordName(keyword);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        suiteTeardown.addArgument(arg1);
        suiteTeardown.addArgument(arg2);
        suiteTeardown.addArgument(arg3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteTeardown.addCommentPart(cm1);
        suiteTeardown.addCommentPart(cm2);
        suiteTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "settings/suiteTeardown/new/" + fileName + "." + format.getExtension();
    }
}
