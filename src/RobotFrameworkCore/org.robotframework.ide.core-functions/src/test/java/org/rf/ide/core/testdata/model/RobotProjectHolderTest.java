/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.project.NullRobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.rf.ide.core.testdata.imported.ARobotInternalVariable;
import org.rf.ide.core.testdata.imported.DictionaryRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ListRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ScalarRobotInternalVariable;

import com.google.common.collect.ImmutableMap;

public class RobotProjectHolderTest {

    private static final File PROJECT_LOCATION = new File("location");

    @Test
    public void testInitingVariableMappings_forNotDefinedConfigurationAndNotDefinedProjectLocation() {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotProjectConfig configuration = new NullRobotProjectConfig();
        projectHolder.configure(configuration, null);

        assertThat(projectHolder.getVariableMappings()).containsOnlyKeys("${/}", "${curdir}", "${space}");
    }

    @Test
    public void testInitingVariableMappings_forNotDefinedConfiguration() {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotProjectConfig configuration = new NullRobotProjectConfig();
        projectHolder.configure(configuration, PROJECT_LOCATION);

        assertThat(projectHolder.getVariableMappings()).containsOnlyKeys("${/}", "${curdir}", "${space}", "${execdir}",
                "${outputdir}");
    }

    @Test
    public void testInitingVariableMappings_forDefaultConfigurationAndNotDefinedProjectLocation() {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotProjectConfig configuration = RobotProjectConfig.create();
        projectHolder.configure(configuration, null);

        assertThat(projectHolder.getVariableMappings()).containsOnlyKeys("${/}", "${curdir}", "${space}");
    }

    @Test
    public void testInitingVariableMappings_forDefaultConfiguration() {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotProjectConfig configuration = RobotProjectConfig.create();
        projectHolder.configure(configuration, PROJECT_LOCATION);

        assertThat(projectHolder.getVariableMappings()).containsOnlyKeys("${/}", "${curdir}", "${space}", "${execdir}",
                "${outputdir}");
    }

    @Test
    public void testInitingVariableMappings_forConfigurationWithVariableMappingsNotContainingVariablesInValues() {
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

    @Test
    public void testInitingVariableMappings_forConfigurationWithVariableMappingsContainingVariablesInValues() {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotProjectConfig configuration = RobotProjectConfig.create();
        configuration.addVariableMapping(VariableMapping.create("${ROOT}", "/home/test"));
        configuration.addVariableMapping(VariableMapping.create("${RESOURCES}", "${ROOT}/resources"));
        configuration.addVariableMapping(VariableMapping.create("${LIBS}", "${RESOURCES}/libs"));
        projectHolder.configure(configuration, PROJECT_LOCATION);

        assertThat(projectHolder.getVariableMappings()).containsOnlyKeys("${/}", "${curdir}", "${space}", "${execdir}",
                "${outputdir}", "${root}", "${resources}", "${libs}");
        assertThat(projectHolder.getVariableMappings()).containsAllEntriesOf(ImmutableMap.of("${root}", "/home/test",
                "${resources}", "/home/test/resources", "${libs}", "/home/test/resources/libs"));
    }

    @Test
    public void testInitingGlobalVariables_forNotDefinedRobotRuntime() {
        final RobotProjectHolder projectHolder = new RobotProjectHolder();
        final RobotProjectConfig configuration = RobotProjectConfig.create();
        projectHolder.configure(configuration, null);

        assertThat(projectHolder.getGlobalVariables()).isEmpty();
    }

    @Test
    public void testInitingGlobalVariables_forDefinedRobotRuntime() {
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getGlobalVariables()).thenReturn(ImmutableMap.of("SCALAR_VAR", true, "LIST_VAR",
                Arrays.asList("x", "y"), "DICT_VAR", ImmutableMap.of("k", "v"), "ARRAY_VAR", new Integer[] { 1, 2 }));

        final RobotProjectHolder projectHolder = new RobotProjectHolder(env);
        final RobotProjectConfig configuration = RobotProjectConfig.create();
        projectHolder.configure(configuration, null);

        final List<ARobotInternalVariable<?>> variables = projectHolder.getGlobalVariables();
        assertThat(variables).extracting(ARobotInternalVariable::getName)
                .containsExactly("SCALAR_VAR", "LIST_VAR", "DICT_VAR", "ARRAY_VAR");
        assertThat(variables).extracting(v -> v.getValue().toString())
                .containsExactly("true", "[x, y]", "{k=v}", "[1, 2]");
        assertThat(variables.get(0)).isInstanceOf(ScalarRobotInternalVariable.class);
        assertThat(variables.get(1)).isInstanceOf(ListRobotInternalVariable.class);
        assertThat(variables.get(2)).isInstanceOf(DictionaryRobotInternalVariable.class);
        assertThat(variables.get(3)).isInstanceOf(ListRobotInternalVariable.class);
    }

    @Test
    public void testIfProjectIsConfiguredOnlyOnce_forTheSameConfiguration() {
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getGlobalVariables()).thenReturn(ImmutableMap.of("A", 1, "B", 2, "C", 3));

        final RobotProjectHolder projectHolder = new RobotProjectHolder(env);
        final RobotProjectConfig configuration = RobotProjectConfig.create();
        projectHolder.configure(configuration, null);
        projectHolder.configure(configuration, null);
        projectHolder.configure(configuration, null);

        assertThat(projectHolder.getGlobalVariables()).extracting(ARobotInternalVariable::getName)
                .containsExactly("A", "B", "C");
        assertThat(projectHolder.getVariableMappings()).containsOnlyKeys("${/}", "${curdir}", "${space}");

        verify(env).getGlobalVariables();
        verify(env).getModuleSearchPaths();
        verifyNoMoreInteractions(env);
    }

    @Test
    public void testIfProjectIsConfiguredTwice_whenConfigurationIsChanged() {
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getGlobalVariables()).thenReturn(ImmutableMap.of("A", 1));

        final RobotProjectHolder projectHolder = new RobotProjectHolder(env);
        projectHolder.configure(RobotProjectConfig.create(), null);
        final RobotProjectConfig configuration = RobotProjectConfig.create();
        configuration.addVariableMapping(VariableMapping.create("${abc}", "x"));
        when(env.getGlobalVariables()).thenReturn(ImmutableMap.of("A", 1, "B", 2));
        projectHolder.configure(configuration, null);

        assertThat(projectHolder.getGlobalVariables()).extracting(ARobotInternalVariable::getName)
                .containsExactly("A", "B");
        assertThat(projectHolder.getVariableMappings()).containsOnlyKeys("${/}", "${curdir}", "${space}", "${abc}");

        verify(env, times(2)).getGlobalVariables();
        verify(env, times(2)).getModuleSearchPaths();
        verifyNoMoreInteractions(env);
    }
}
