/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfSettingsSuiteTestTimeoutTest extends RobotFormatParameterizedTest {

    public CreationOfSettingsSuiteTestTimeoutTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTemplate() throws Exception {
        // prepare
        final String fileName = convert("EmptyTestTimeoutDeclarationOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newTestTimeout();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_andAddComments() throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithCommentsOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTimeout.addCommentPart(cm1);
        testTimeout.addCommentPart(cm2);
        testTimeout.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_andTimeoutValue() throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithTimeoutOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        final RobotToken timeValue = new RobotToken();
        timeValue.setText("1 minutes");

        testTimeout.setTimeout(timeValue);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_andTimeoutValue_andComment() throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithTimeoutAndCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        final RobotToken timeValue = new RobotToken();
        timeValue.setText("1 minutes");

        testTimeout.setTimeout(timeValue);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTimeout.addCommentPart(cm1);
        testTimeout.addCommentPart(cm2);
        testTimeout.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_andTimeoutValue_withThreeMessages() throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithTimeoutAndMsgOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        final RobotToken timeValue = new RobotToken();
        timeValue.setText("1 minutes");

        testTimeout.setTimeout(timeValue);

        final RobotToken msg1 = new RobotToken();
        msg1.setText("msg1P");
        final RobotToken msg2 = new RobotToken();
        msg2.setText("msg2P");
        final RobotToken msg3 = new RobotToken();
        msg3.setText("msg3P");
        testTimeout.addMessageArgument(msg1);
        testTimeout.addMessageArgument(msg2);
        testTimeout.addMessageArgument(msg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_andTimeoutValue_withThreeMessages_andComment() throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithTimeoutAndMsgAndCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        final RobotToken timeValue = new RobotToken();
        timeValue.setText("1 minutes");

        testTimeout.setTimeout(timeValue);

        final RobotToken msg1 = new RobotToken();
        msg1.setText("msg1P");
        final RobotToken msg2 = new RobotToken();
        msg2.setText("msg2P");
        final RobotToken msg3 = new RobotToken();
        msg3.setText("msg3P");
        testTimeout.addMessageArgument(msg1);
        testTimeout.addMessageArgument(msg2);
        testTimeout.addMessageArgument(msg3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTimeout.addCommentPart(cm1);
        testTimeout.addCommentPart(cm2);
        testTimeout.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName) {
        return "settings/testTimeout/new/" + fileName + "." + getExtension();
    }
}
