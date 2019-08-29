/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfSettingsTaskTimeoutTest extends RobotFormatParameterizedTest {

    public CreationOfSettingsTaskTimeoutTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_createTaskTemplate_andTimeoutValue_withThreeMessages_andComment() throws Exception {
        // prepare
        final String fileName = convert("TaskTimeoutDeclarationWithTimeoutAndMsgAndCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TaskTimeout taskTimeout = settingTable.newTaskTimeout();

        final RobotToken timeValue = new RobotToken();
        timeValue.setText("1 minutes");

        taskTimeout.setTimeout(timeValue);

        final RobotToken msg1 = new RobotToken();
        msg1.setText("msg1P");
        final RobotToken msg2 = new RobotToken();
        msg2.setText("msg2P");
        final RobotToken msg3 = new RobotToken();
        msg3.setText("msg3P");
        taskTimeout.addMessageArgument(msg1);
        taskTimeout.addMessageArgument(msg2);
        taskTimeout.addMessageArgument(msg3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        taskTimeout.addCommentPart(cm1);
        taskTimeout.addCommentPart(cm2);
        taskTimeout.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName) {
        return "settings/taskTimeout/new/" + fileName + "." + getExtension();
    }
}
