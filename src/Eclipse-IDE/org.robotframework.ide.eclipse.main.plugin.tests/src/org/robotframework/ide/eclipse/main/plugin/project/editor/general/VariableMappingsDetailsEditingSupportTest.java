/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ColumnViewer;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.general.VariableMappingsDetailsEditingSupport.VariableMappingNameEditingSupport;
import org.robotframework.ide.eclipse.main.plugin.project.editor.general.VariableMappingsDetailsEditingSupport.VariableMappingValueEditingSupport;

public class VariableMappingsDetailsEditingSupportTest {

    @Test
    public void nameIsNotChanged() throws Exception {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final VariableMappingNameEditingSupport editingSupport = new VariableMappingNameEditingSupport(
                mock(ColumnViewer.class), null, eventBroker);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "${var}");

        assertThat(mapping.getName()).isEqualTo("${var}");
        assertThat(mapping.getValue()).isEqualTo("abc");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nameIsChanged() throws Exception {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final VariableMappingNameEditingSupport editingSupport = new VariableMappingNameEditingSupport(
                mock(ColumnViewer.class), null, eventBroker);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "${new}");

        assertThat(mapping.getName()).isEqualTo("${new}");
        assertThat(mapping.getValue()).isEqualTo("abc");
        verify(eventBroker).send(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_NAME_CHANGED, mapping);
    }

    @Test
    public void valueIsNotChanged() throws Exception {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final VariableMappingValueEditingSupport editingSupport = new VariableMappingValueEditingSupport(
                mock(ColumnViewer.class), null, eventBroker);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "abc");

        assertThat(mapping.getName()).isEqualTo("${var}");
        assertThat(mapping.getValue()).isEqualTo("abc");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valueIsChanged() throws Exception {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final VariableMappingValueEditingSupport editingSupport = new VariableMappingValueEditingSupport(
                mock(ColumnViewer.class), null, eventBroker);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "xyz");

        assertThat(mapping.getName()).isEqualTo("${var}");
        assertThat(mapping.getValue()).isEqualTo("xyz");
        verify(eventBroker).send(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_VALUE_CHANGED, mapping);
    }
}
