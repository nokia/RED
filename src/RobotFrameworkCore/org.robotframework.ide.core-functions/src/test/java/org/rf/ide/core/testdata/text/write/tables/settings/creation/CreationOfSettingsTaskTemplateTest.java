/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsTaskTemplateTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_createTaskTemplate_withKeywordName_andThreeUnwanted_andComment(final FileFormat format)
            throws Exception {
        // prepare
        final String fileName = convert("TaskTemplateDeclarationWithKeyword3UnwantedArgsCommentOnly", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final TaskTemplate taskTemplate = settingTable.newTaskTemplate();

        final RobotToken keyword = new RobotToken();
        keyword.setText("keyword");

        taskTemplate.setKeywordName(keyword);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("unArg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("unArg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("unArg3");

        taskTemplate.addUnexpectedTrashArgument(arg1);
        taskTemplate.addUnexpectedTrashArgument(arg2);
        taskTemplate.addUnexpectedTrashArgument(arg3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        taskTemplate.addCommentPart(cm1);
        taskTemplate.addCommentPart(cm2);
        taskTemplate.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "settings/taskTemplate/new/" + fileName + "." + format.getExtension();
    }
}
