/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.execution.debug.contexts.ModelBuilder;

public class SettingTableTest {

    @Test
    public void testTemplateInUse_forRfUnder30() {
        final SettingTable settingsTable1 = ModelBuilder.modelForFile(new RobotVersion(2, 9))
                .withSettingsTable()
                .withTestTemplate("keyword")
                .build()
                .getSettingTable();
        final SettingTable settingsTable2 = ModelBuilder.modelForFile(new RobotVersion(2, 9))
                .withSettingsTable()
                .withTestTemplate("keyword", "2")
                .build()
                .getSettingTable();
        final SettingTable settingsTable3 = ModelBuilder.modelForFile(new RobotVersion(2, 9))
                .withSettingsTable()
                .withTestTemplate("keyword", "2")
                .withTestTemplate("keyword", "4")
                .build().getSettingTable();

        assertThat(settingsTable1.getTestTemplateInUse()).isEqualTo("keyword");
        assertThat(settingsTable2.getTestTemplateInUse()).isEqualTo("keyword 2");
        assertThat(settingsTable3.getTestTemplateInUse()).isEqualTo("keyword 2 keyword 4");
    }

    @Test
    public void testTemplateInUse_forRf30And31() {
        final SettingTable settingsTable1 = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withSettingsTable()
                .withTestTemplate("keyword")
                .build()
                .getSettingTable();
        final SettingTable settingsTable2 = ModelBuilder.modelForFile(new RobotVersion(3, 0))
                .withSettingsTable()
                .withTestTemplate("keyword", "2")
                .build()
                .getSettingTable();
        final SettingTable settingsTable3 = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withSettingsTable()
                .withTestTemplate("keyword", "2")
                .withTestTemplate("keyword", "4")
                .build()
                .getSettingTable();

        assertThat(settingsTable1.getTestTemplateInUse()).isEqualTo("keyword");
        assertThat(settingsTable2.getTestTemplateInUse()).isEqualTo("keyword 2");
        assertThat(settingsTable3.getTestTemplateInUse()).isNull();
    }

    @Test
    public void taskTemplateInUse_forRf30And31() {
        final SettingTable settingsTable1 = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withSettingsTable()
                .withTaskTemplate("keyword")
                .build()
                .getSettingTable();
        final SettingTable settingsTable2 = ModelBuilder.modelForFile(new RobotVersion(3, 0))
                .withSettingsTable()
                .withTaskTemplate("keyword", "2")
                .build()
                .getSettingTable();
        final SettingTable settingsTable3 = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withSettingsTable()
                .withTaskTemplate("keyword", "2")
                .withTaskTemplate("keyword", "4")
                .build()
                .getSettingTable();

        assertThat(settingsTable1.getTaskTemplateInUse()).isEqualTo("keyword");
        assertThat(settingsTable2.getTaskTemplateInUse()).isEqualTo("keyword 2");
        assertThat(settingsTable3.getTaskTemplateInUse()).isNull();
    }

    @Test
    public void testTemplateInUse_forRf32() {
        final SettingTable settingsTable1 = ModelBuilder.modelForFile(new RobotVersion(3, 2))
                .withSettingsTable()
                .withTestTemplate("keyword")
                .build()
                .getSettingTable();
        final SettingTable settingsTable2 = ModelBuilder.modelForFile(new RobotVersion(3, 2))
                .withSettingsTable()
                .withTestTemplate("keyword", "2")
                .build()
                .getSettingTable();
        final SettingTable settingsTable3 = ModelBuilder.modelForFile(new RobotVersion(3, 2))
                .withSettingsTable()
                .withTestTemplate("keyword", "2")
                .withTestTemplate("keyword", "4")
                .build()
                .getSettingTable();

        assertThat(settingsTable1.getTestTemplateInUse()).isEqualTo("keyword");
        assertThat(settingsTable2.getTestTemplateInUse()).isNull();
        assertThat(settingsTable3.getTestTemplateInUse()).isNull();
    }

    @Test
    public void taskTemplateInUse_forRf32() {
        final SettingTable settingsTable1 = ModelBuilder.modelForFile(new RobotVersion(3, 2))
                .withSettingsTable()
                .withTaskTemplate("keyword")
                .build()
                .getSettingTable();
        final SettingTable settingsTable2 = ModelBuilder.modelForFile(new RobotVersion(3, 2))
                .withSettingsTable()
                .withTaskTemplate("keyword", "2")
                .build()
                .getSettingTable();
        final SettingTable settingsTable3 = ModelBuilder.modelForFile(new RobotVersion(3, 2))
                .withSettingsTable()
                .withTaskTemplate("keyword", "2")
                .withTaskTemplate("keyword", "4")
                .build()
                .getSettingTable();

        assertThat(settingsTable1.getTaskTemplateInUse()).isEqualTo("keyword");
        assertThat(settingsTable2.getTaskTemplateInUse()).isNull();
        assertThat(settingsTable3.getTaskTemplateInUse()).isNull();
    }
}
