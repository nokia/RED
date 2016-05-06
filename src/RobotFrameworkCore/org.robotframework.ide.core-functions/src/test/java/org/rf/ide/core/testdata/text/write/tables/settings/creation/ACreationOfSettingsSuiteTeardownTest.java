/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfSettingsSuiteTeardownTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//suiteTeardown//new//";

    private final String extension;

    public ACreationOfSettingsSuiteTeardownTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateSuiteTeardown() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptySuiteTeardownDeclarationOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newSuiteTeardown();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteTeardown_andAddComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteTeardownDeclarationWithCommentsOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteTeardown.addCommentPart(cm1);
        suiteTeardown.addCommentPart(cm2);
        suiteTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteTeardown_andKeywordOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteTeardownDeclarationWithKeywordOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteTeardown.setKeywordName(keyword);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteTeardown_andKeyword_andComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteTeardownDeclarationWithKeywordCommentsOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteTeardown.setKeywordName(keyword);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteTeardown.addCommentPart(cm1);
        suiteTeardown.addCommentPart(cm2);
        suiteTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteTeardown_andKeyword_andThreeArgs() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteTeardownDeclarationWithKeyword3ArgsOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteTeardown.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        suiteTeardown.addArgument(arg1);
        suiteTeardown.addArgument(arg2);
        suiteTeardown.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteTeardown_andKeyword_andThreeArgs_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteTeardownDeclarationWithKeyword3ArgsAndCommentOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteTeardown suiteTeardown = settingTable.newSuiteTeardown();

        RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        suiteTeardown.setKeywordName(keyword);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        suiteTeardown.addArgument(arg1);
        suiteTeardown.addArgument(arg2);
        suiteTeardown.addArgument(arg3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteTeardown.addCommentPart(cm1);
        suiteTeardown.addCommentPart(cm2);
        suiteTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
