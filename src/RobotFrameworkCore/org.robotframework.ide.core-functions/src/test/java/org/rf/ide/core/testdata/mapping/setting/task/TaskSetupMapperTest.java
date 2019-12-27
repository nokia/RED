/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskSetupMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskSetupMapper mapper = new TaskSetupMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void theMapperCorrectlyAddsSetting_ifItDoesNotExist() {
        final TaskSetupMapper mapper = new TaskSetupMapper();

        final SettingTable settingTable = new SettingTable(null);

        assertThat(settingTable.getTaskSetups()).isEmpty();
        final boolean isDuplicated = mapper.addSetting(settingTable, RobotToken.create("Task Setup"));
        assertThat(isDuplicated).isFalse();
        assertThat(settingTable.getTaskSetups()).hasSize(1)
                .extracting(TaskSetup::getDeclaration)
                .flatExtracting(RobotToken::getTypes)
                .contains(RobotTokenType.SETTING_TASK_SETUP_DECLARATION);
    }

    @Test
    public void theMapperCorrectlyAddsSetting_ifThereWasAlreadyOne() {
        final TaskSetupMapper mapper = new TaskSetupMapper();

        final SettingTable settingTable = new SettingTable(null);
        settingTable.addTaskSetup(new TaskSetup(RobotToken.create("task setup")));

        assertThat(settingTable.getTaskSetups()).hasSize(1);
        final boolean isDuplicated = mapper.addSetting(settingTable, RobotToken.create("Task Setup"));
        assertThat(isDuplicated).isTrue();
        assertThat(settingTable.getTaskSetups()).hasSize(2)
                .extracting(TaskSetup::getDeclaration)
                .flatExtracting(RobotToken::getTypes)
                .contains(RobotTokenType.SETTING_TASK_SETUP_DECLARATION);
    }
}
