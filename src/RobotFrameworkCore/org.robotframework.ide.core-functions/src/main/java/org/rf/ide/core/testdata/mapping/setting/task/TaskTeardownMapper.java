/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.task;

import org.rf.ide.core.testdata.mapping.setting.SettingDeclarationMapper;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTeardown;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTeardownMapper extends SettingDeclarationMapper {

    public TaskTeardownMapper() {
        super(RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION, ParsingState.SETTING_TASK_TEARDOWN);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 1));
    }

    @Override
    protected boolean addSetting(final SettingTable settingTable, final RobotToken token) {
        settingTable.addTaskTeardown(new TaskTeardown(token));
        return settingTable.getTaskTeardowns().size() > 1;
    }
}
