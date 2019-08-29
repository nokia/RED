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
import org.rf.ide.core.testdata.model.table.setting.TaskTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfSettingsTaskTeardownTest extends RobotFormatParameterizedTest {

    public CreationOfSettingsTaskTeardownTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_createTaskTeardown_andKeyword_andThreeArgs_andComment() throws Exception {
        // prepare
        final String fileName = convert("TaskTeardownDeclarationWithKeyword3ArgsAndCommentOnly");
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("3.1");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TaskTeardown taskTeardown = settingTable.newTaskTeardown();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        taskTeardown.setKeywordName(keyword);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");
        taskTeardown.addArgument(arg1);
        taskTeardown.addArgument(arg2);
        taskTeardown.addArgument(arg3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        taskTeardown.addCommentPart(cm1);
        taskTeardown.addCommentPart(cm2);
        taskTeardown.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName) {
        return "settings/taskTeardown/new/" + fileName + "." + getExtension();
    }
}
