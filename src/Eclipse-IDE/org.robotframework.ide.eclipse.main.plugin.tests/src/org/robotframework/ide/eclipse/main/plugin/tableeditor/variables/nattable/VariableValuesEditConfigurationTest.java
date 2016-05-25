/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.edit.DetailCellEditor;
import org.robotframework.red.nattable.edit.HorizontalMovingTextCellEditor;

public class VariableValuesEditConfigurationTest {

    @Test
    public void thereIsATextCellEditorRegisteredForScalarVariableValues() {
        final VariableValuesEditConfiguration config = new VariableValuesEditConfiguration(mock(TableTheme.class), mock(VariablesDataProvider.class),
                mock(IEventBroker.class));

        final IConfigRegistry configRegistry = new ConfigRegistry();
        config.configureRegistry(configRegistry);

        final ICellEditor editor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                DisplayMode.NORMAL,
                VariableType.SCALAR.name());
        assertThat(editor).isInstanceOf(HorizontalMovingTextCellEditor.class);
    }

    @Test
    public void thereIsADetailCellEditorRegisteredForScalarAsListVariableValues() {
        final VariableValuesEditConfiguration config = new VariableValuesEditConfiguration(mock(TableTheme.class),
                mock(VariablesDataProvider.class), mock(IEventBroker.class));

        final IConfigRegistry configRegistry = new ConfigRegistry();
        config.configureRegistry(configRegistry);

        final ICellEditor editor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                DisplayMode.NORMAL, VariableType.SCALAR_AS_LIST.name());
        assertThat(editor).isInstanceOf(DetailCellEditor.class);
    }

    @Test
    public void thereIsADetailCellEditorRegisteredForListVariableValues() {
        final VariableValuesEditConfiguration config = new VariableValuesEditConfiguration(mock(TableTheme.class),
                mock(VariablesDataProvider.class), mock(IEventBroker.class));

        final IConfigRegistry configRegistry = new ConfigRegistry();
        config.configureRegistry(configRegistry);

        final ICellEditor editor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                DisplayMode.NORMAL, VariableType.LIST.name());
        assertThat(editor).isInstanceOf(DetailCellEditor.class);
    }

    @Test
    public void thereIsADetailCellEditorRegisteredForDictionaryVariableValues() {
        final VariableValuesEditConfiguration config = new VariableValuesEditConfiguration(mock(TableTheme.class),
                mock(VariablesDataProvider.class), mock(IEventBroker.class));

        final IConfigRegistry configRegistry = new ConfigRegistry();
        config.configureRegistry(configRegistry);

        final ICellEditor editor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                DisplayMode.NORMAL, VariableType.DICTIONARY.name());
        assertThat(editor).isInstanceOf(DetailCellEditor.class);
    }

    @Test
    public void thereIsADetailCellEditorRegisteredForInvalidVariableValues() {
        final VariableValuesEditConfiguration config = new VariableValuesEditConfiguration(mock(TableTheme.class),
                mock(VariablesDataProvider.class), mock(IEventBroker.class));

        final IConfigRegistry configRegistry = new ConfigRegistry();
        config.configureRegistry(configRegistry);

        final ICellEditor editor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                DisplayMode.NORMAL, VariableType.DICTIONARY.name());
        assertThat(editor).isInstanceOf(DetailCellEditor.class);
    }
}
