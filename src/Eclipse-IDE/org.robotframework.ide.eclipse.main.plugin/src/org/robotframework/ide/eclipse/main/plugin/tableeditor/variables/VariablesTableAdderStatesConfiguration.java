/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.red.nattable.configs.AddingElementStyleConfiguration.DropdownImagePainter;

/**
 * @author Michal Anglart
 *
 */
class VariablesTableAdderStatesConfiguration extends AbstractUiBindingConfiguration {

    private final VariablesDataProvider variablesDataProvider;

    VariablesTableAdderStatesConfiguration(final VariablesDataProvider variablesDataProvider) {
        this.variablesDataProvider = variablesDataProvider;
    }

    @Override
    public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerFirstMouseDownBinding(new CellPainterMouseEventMatcher(GridRegion.BODY,
                MouseEventMatcher.LEFT_BUTTON, DropdownImagePainter.class),
                new ChangeAdderStateAction(variablesDataProvider));
    }

    public enum VariablesAdderState implements TokenState {
        SCALAR("scalar", VariableType.SCALAR),
        LIST("list", VariableType.LIST),
        DICTIONARY("dictionary", VariableType.DICTIONARY);

        private String name;

        private final VariableType type;

        private VariablesAdderState(final String name, final VariableType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String getNewObjectTypeName() {
            return name;
        }

        public VariableType getVariableType() {
            return type;
        }
    }

    private static class ChangeAdderStateAction implements IMouseAction {

        private final VariablesDataProvider variablesDataProvider;

        ChangeAdderStateAction(final VariablesDataProvider variablesDataProvider) {
            this.variablesDataProvider = variablesDataProvider;
        }

        @Override
        public void run(final NatTable natTable, final MouseEvent event) {
            variablesDataProvider.switchAddderToNextState();
            natTable.refresh();
        }
    }
}
