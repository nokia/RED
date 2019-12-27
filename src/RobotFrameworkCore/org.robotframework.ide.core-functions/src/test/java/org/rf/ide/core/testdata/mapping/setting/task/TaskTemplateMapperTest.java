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
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTemplateMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskTemplateMapper mapper = new TaskTemplateMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void theMapperCorrectlyAddsSetting_ifItDoesNotExist() {
        final TaskTemplateMapper mapper = new TaskTemplateMapper();

        final SettingTable settingTable = new SettingTable(null);

        assertThat(settingTable.getTaskTemplates()).isEmpty();
        final boolean isDuplicated = mapper.addSetting(settingTable, RobotToken.create("Task Template"));
        assertThat(isDuplicated).isFalse();
        assertThat(settingTable.getTaskTemplates()).hasSize(1)
                .extracting(TaskTemplate::getDeclaration)
                .flatExtracting(RobotToken::getTypes)
                .contains(RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION);
    }

    @Test
    public void theMapperCorrectlyAddsSetting_ifThereWasAlreadyOne() {
        final TaskTemplateMapper mapper = new TaskTemplateMapper();

        final SettingTable settingTable = new SettingTable(null);
        settingTable.addTaskTemplate(new TaskTemplate(RobotToken.create("task template")));

        assertThat(settingTable.getTaskTemplates()).hasSize(1);
        final boolean isDuplicated = mapper.addSetting(settingTable, RobotToken.create("Task Template"));
        assertThat(isDuplicated).isTrue();
        assertThat(settingTable.getTaskTemplates()).hasSize(2)
                .extracting(TaskTemplate::getDeclaration)
                .flatExtracting(RobotToken::getTypes)
                .contains(RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION);
    }

}
