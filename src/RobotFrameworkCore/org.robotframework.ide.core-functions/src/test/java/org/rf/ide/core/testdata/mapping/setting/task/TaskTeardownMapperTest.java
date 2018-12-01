/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTeardownMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskTeardownMapper mapper = new TaskTeardownMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void theMapperCorrectlyAddsSetting_ifItDoesNotExist() {
        final TaskTeardownMapper mapper = new TaskTeardownMapper();

        final SettingTable settingTable = new SettingTable(null);

        assertThat(settingTable.getTaskTeardowns()).isEmpty();
        final boolean isDuplicated = mapper.addSetting(settingTable, RobotToken.create("Task Teardown"));
        assertThat(isDuplicated).isFalse();
        assertThat(settingTable.getTaskTeardowns()).hasSize(1)
                .extracting(TaskTeardown::getDeclaration)
                .flatExtracting(RobotToken::getTypes)
                .contains(RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION);
    }

    @Test
    public void theMapperCorrectlyAddsSetting_ifThereWasAlreadyOne() {
        final TaskTeardownMapper mapper = new TaskTeardownMapper();

        final SettingTable settingTable = new SettingTable(null);
        settingTable.addTaskTeardown(new TaskTeardown(RobotToken.create("task teardown")));

        assertThat(settingTable.getTaskTeardowns()).hasSize(1);
        final boolean isDuplicated = mapper.addSetting(settingTable, RobotToken.create("Task Teardown"));
        assertThat(isDuplicated).isTrue();
        assertThat(settingTable.getTaskTeardowns()).hasSize(2)
                .extracting(TaskTeardown::getDeclaration)
                .flatExtracting(RobotToken::getTypes)
                .contains(RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION);
    }

}
