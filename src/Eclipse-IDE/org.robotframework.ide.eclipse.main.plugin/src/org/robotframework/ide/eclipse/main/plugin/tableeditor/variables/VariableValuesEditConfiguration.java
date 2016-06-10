/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.assist.VariablesContentProposingSupport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.edit.DetailCellEditor;
import org.robotframework.red.nattable.edit.RedTextCellEditor;

class VariableValuesEditConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    private final VariablesDataProvider dataProvider;

    private final RobotEditorCommandsStack commandsStack;

    private final RobotSuiteFile suiteFile;

    VariableValuesEditConfiguration(final TableTheme theme, final RobotSuiteFile suiteFile,
            final VariablesDataProvider dataProvider, final RobotEditorCommandsStack commandsStack) {
        this.theme = theme;
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
        this.commandsStack = commandsStack;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        configureNamesCellEditors(configRegistry);
        configureValuesCellEditors(configRegistry);
    }

    private void configureNamesCellEditors(final IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(2, 1), DisplayMode.NORMAL,
                VariableTypesAndColumnsLabelAccumulator.getNameColumnLabel(VariableType.SCALAR));
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(2, 1), DisplayMode.NORMAL,
                VariableTypesAndColumnsLabelAccumulator.getNameColumnLabel(VariableType.SCALAR_AS_LIST));
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(2, 1), DisplayMode.NORMAL,
                VariableTypesAndColumnsLabelAccumulator.getNameColumnLabel(VariableType.LIST));
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(2, 1), DisplayMode.NORMAL,
                VariableTypesAndColumnsLabelAccumulator.getNameColumnLabel(VariableType.DICTIONARY));
    }

    private void configureValuesCellEditors(final IConfigRegistry configRegistry) {
        final VariablesContentProposingSupport proposalsSupport = new VariablesContentProposingSupport(suiteFile);

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(proposalsSupport), DisplayMode.NORMAL,
                VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.SCALAR));

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new DetailCellEditor<>(new ListVariableDetailsEditingSupport(theme, dataProvider, commandsStack),
                        proposalsSupport),
                DisplayMode.NORMAL,
                VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.SCALAR_AS_LIST));
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new DetailCellEditor<>(new ListVariableDetailsEditingSupport(theme, dataProvider, commandsStack),
                        proposalsSupport),
                DisplayMode.NORMAL, VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.LIST));
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new DetailCellEditor<>(new ListVariableDetailsEditingSupport(theme, dataProvider, commandsStack),
                        proposalsSupport),
                DisplayMode.NORMAL, VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.INVALID));

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new DetailCellEditor<>(new DictVariableDetailsEditingSupport(theme, dataProvider, commandsStack),
                        proposalsSupport),
                DisplayMode.NORMAL,
                VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.DICTIONARY));
    }
}