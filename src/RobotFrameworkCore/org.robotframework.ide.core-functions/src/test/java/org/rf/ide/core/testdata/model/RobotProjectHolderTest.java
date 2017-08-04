/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;

import com.google.common.collect.ImmutableMap;

public class RobotProjectHolderTest {

    private static final File PROJECT_LOCATION = new File("location");

    @Test
    public void testInitingVariableMappings_forDefaultConfiguration() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotProjectConfig configuration = RobotProjectConfig.create();
        projectHolder.configure(configuration, PROJECT_LOCATION);

        assertThat(projectHolder.getVariableMappings()).containsOnlyKeys("${/}", "${curdir}", "${space}", "${execdir}",
                "${outputdir}");
    }

    @Test
    public void testInitingVariableMappings_forConfigurationWithSimpleVariableMapping() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotProjectConfig configuration = RobotProjectConfig.create();
        configuration.addVariableMapping(VariableMapping.create("${abc}", "x"));
        configuration.addVariableMapping(VariableMapping.create("${Def}", "y"));
        configuration.addVariableMapping(VariableMapping.create("${g_H i}", "z"));
        projectHolder.configure(configuration, PROJECT_LOCATION);

        assertThat(projectHolder.getVariableMappings()).containsOnlyKeys("${/}", "${curdir}", "${space}", "${execdir}",
                "${outputdir}", "${abc}", "${def}", "${ghi}");
        assertThat(projectHolder.getVariableMappings())
                .containsAllEntriesOf(ImmutableMap.of("${abc}", "x", "${def}", "y", "${ghi}", "z"));
    }
}
