/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.edit.DetailCellEditor;
import org.robotframework.red.nattable.edit.HorizontalMovingTextCellEditor;

class VariableValuesEditConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    private final VariablesDataProvider dataProvider;

    private final RobotEditorCommandsStack commandsStack;

    VariableValuesEditConfiguration(final TableTheme theme, final VariablesDataProvider dataProvider,
            final RobotEditorCommandsStack commandsStack) {
        this.theme = theme;
        this.dataProvider = dataProvider;
        this.commandsStack = commandsStack;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new HorizontalMovingTextCellEditor(),
                DisplayMode.NORMAL, VariableType.SCALAR.name());

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new DetailCellEditor<>(new ListVariableDetailsEditingSupport(theme, dataProvider, commandsStack)),
                DisplayMode.NORMAL, VariableType.SCALAR_AS_LIST.name());
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new DetailCellEditor<>(new ListVariableDetailsEditingSupport(theme, dataProvider, commandsStack)),
                DisplayMode.NORMAL, VariableType.LIST.name());
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new DetailCellEditor<>(new ListVariableDetailsEditingSupport(theme, dataProvider, commandsStack)),
                DisplayMode.NORMAL, VariableType.INVALID.name());

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new DetailCellEditor<>(new DictVariableDetailsEditingSupport(theme, dataProvider, commandsStack)),
                DisplayMode.NORMAL, VariableType.DICTIONARY.name());
    }
}