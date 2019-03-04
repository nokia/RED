/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ColumnViewer;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariableMappingsDetailsEditingSupport.VariableMappingNameEditingSupport;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariableMappingsDetailsEditingSupport.VariableMappingValueEditingSupport;

public class VariableMappingsDetailsEditingSupportTest {

    @Test
    public void nameIsNotChanged() throws Exception {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final IFile file = mock(IFile.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);

        final VariableMappingNameEditingSupport editingSupport = new VariableMappingNameEditingSupport(
                mock(ColumnViewer.class), null, input, eventBroker);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "${var}");

        assertThat(mapping.getName()).isEqualTo("${var}");
        assertThat(mapping.getValue()).isEqualTo("abc");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nameIsChanged() throws Exception {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final IFile file = mock(IFile.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);

        final VariableMappingNameEditingSupport editingSupport = new VariableMappingNameEditingSupport(
                mock(ColumnViewer.class), null, input, eventBroker);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "${new}");

        assertThat(mapping.getName()).isEqualTo("${new}");
        assertThat(mapping.getValue()).isEqualTo("abc");
        verify(eventBroker).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_NAME_CHANGED),
                argThat(hasCorrectEventData(file, mapping)));
    }

    @Test
    public void valueIsNotChanged() throws Exception {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final IFile file = mock(IFile.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);

        final VariableMappingValueEditingSupport editingSupport = new VariableMappingValueEditingSupport(
                mock(ColumnViewer.class), null, input, eventBroker);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "abc");

        assertThat(mapping.getName()).isEqualTo("${var}");
        assertThat(mapping.getValue()).isEqualTo("abc");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valueIsChanged() throws Exception {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final IFile file = mock(IFile.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);

        final VariableMappingValueEditingSupport editingSupport = new VariableMappingValueEditingSupport(
                mock(ColumnViewer.class), null, input, eventBroker);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "xyz");

        assertThat(mapping.getName()).isEqualTo("${var}");
        assertThat(mapping.getValue()).isEqualTo("xyz");
        verify(eventBroker).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_VALUE_CHANGED),
                argThat(hasCorrectEventData(file, mapping)));
    }

    private static ArgumentMatcher<Object> hasCorrectEventData(final IFile file, final VariableMapping mapping) {
        return object -> object instanceof RedProjectConfigEventData<?>
                && file.equals(((RedProjectConfigEventData<?>) object).getUnderlyingFile())
                && mapping.equals(((RedProjectConfigEventData<?>) object).getChangedElement());
    }
}
