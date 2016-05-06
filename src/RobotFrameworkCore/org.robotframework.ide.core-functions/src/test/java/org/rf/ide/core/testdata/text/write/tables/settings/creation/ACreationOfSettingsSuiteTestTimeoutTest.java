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
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfSettingsSuiteTestTimeoutTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "settings//testTimeout//new//";

    private final String extension;

    public ACreationOfSettingsSuiteTestTimeoutTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateTestTemplate() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyTestTimeoutDeclarationOnly." + getExtension();
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
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTimeoutDeclarationWithCommentsOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
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
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTimeoutDeclarationWithTimeoutOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        RobotToken timeValue = new RobotToken();
        timeValue.setText("1 minutes");

        testTimeout.setTimeout(timeValue);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createTestTemplate_andTimeoutValue_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTimeoutDeclarationWithTimeoutAndCommentOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        RobotToken timeValue = new RobotToken();
        timeValue.setText("1 minutes");

        testTimeout.setTimeout(timeValue);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
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
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTimeoutDeclarationWithTimeoutAndMsgOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        RobotToken timeValue = new RobotToken();
        timeValue.setText("1 minutes");

        testTimeout.setTimeout(timeValue);

        RobotToken msg1 = new RobotToken();
        msg1.setText("msg1P");
        RobotToken msg2 = new RobotToken();
        msg2.setText("msg2P");
        RobotToken msg3 = new RobotToken();
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
        final String fileName = PRETTY_NEW_DIR_LOCATION + "TestTimeoutDeclarationWithTimeoutAndMsgAndCommentOnly."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TestTimeout testTimeout = settingTable.newTestTimeout();

        RobotToken timeValue = new RobotToken();
        timeValue.setText("1 minutes");

        testTimeout.setTimeout(timeValue);

        RobotToken msg1 = new RobotToken();
        msg1.setText("msg1P");
        RobotToken msg2 = new RobotToken();
        msg2.setText("msg2P");
        RobotToken msg3 = new RobotToken();
        msg3.setText("msg3P");
        testTimeout.addMessageArgument(msg1);
        testTimeout.addMessageArgument(msg2);
        testTimeout.addMessageArgument(msg3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        testTimeout.addCommentPart(cm1);
        testTimeout.addCommentPart(cm2);
        testTimeout.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
