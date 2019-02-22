/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.function.Consumer;

import org.eclipse.jface.viewers.ColumnViewer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariableMappingsDetailsEditingSupport.VariableMappingNameEditingSupport;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariableMappingsDetailsEditingSupport.VariableMappingValueEditingSupport;

@RunWith(MockitoJUnitRunner.class)
public class VariableMappingsDetailsEditingSupportTest {

    @Mock
    private Consumer<VariableMapping> successHandler;

    @Test
    public void nameIsNotChanged() throws Exception {
        final VariableMappingNameEditingSupport editingSupport = new VariableMappingNameEditingSupport(
                mock(ColumnViewer.class), null, successHandler);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "${var}");

        assertThat(mapping.getName()).isEqualTo("${var}");
        assertThat(mapping.getValue()).isEqualTo("abc");
        verifyZeroInteractions(successHandler);
    }

    @Test
    public void nameIsChanged() throws Exception {
        final VariableMappingNameEditingSupport editingSupport = new VariableMappingNameEditingSupport(
                mock(ColumnViewer.class), null, successHandler);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "${new}");

        assertThat(mapping.getName()).isEqualTo("${new}");
        assertThat(mapping.getValue()).isEqualTo("abc");
        verify(successHandler).accept(mapping);
    }

    @Test
    public void valueIsNotChanged() throws Exception {
        final VariableMappingValueEditingSupport editingSupport = new VariableMappingValueEditingSupport(
                mock(ColumnViewer.class), null, successHandler);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "abc");

        assertThat(mapping.getName()).isEqualTo("${var}");
        assertThat(mapping.getValue()).isEqualTo("abc");
        verifyZeroInteractions(successHandler);
    }

    @Test
    public void valueIsChanged() throws Exception {
        final VariableMappingValueEditingSupport editingSupport = new VariableMappingValueEditingSupport(
                mock(ColumnViewer.class), null, successHandler);
        final VariableMapping mapping = VariableMapping.create("${var}", "abc");
        editingSupport.setValue(mapping, "xyz");

        assertThat(mapping.getName()).isEqualTo("${var}");
        assertThat(mapping.getValue()).isEqualTo("xyz");
        verify(successHandler).accept(mapping);
    }
}
