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
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsTestTimeoutTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateTestTemplate(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("EmptyTestTimeoutDeclarationOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newTestTimeout();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createTestTemplate_andAddComments(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithCommentsOnly", format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createTestTemplate_andTimeoutValue(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithTimeoutOnly", format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createTestTemplate_andTimeoutValue_andComment(final FileFormat format) throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithTimeoutAndCommentOnly", format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createTestTemplate_andTimeoutValue_withThreeMessages(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithTimeoutAndMsgOnly", format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createTestTemplate_andTimeoutValue_withThreeMessages_andComment(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("TestTimeoutDeclarationWithTimeoutAndMsgAndCommentOnly", format);
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

    private String convert(final String fileName, final FileFormat format) {
        return "settings/testTimeout/new/" + fileName + "." + format.getExtension();
    }
}
