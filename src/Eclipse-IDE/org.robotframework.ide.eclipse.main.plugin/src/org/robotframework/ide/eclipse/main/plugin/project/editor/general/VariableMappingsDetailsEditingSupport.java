/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.general;

import java.util.function.Supplier;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;


/**
 * @author Michal Anglart
 *
 */
abstract class VariableMappingsDetailsEditingSupport extends ElementsAddingEditingSupport {

    VariableMappingsDetailsEditingSupport(final ColumnViewer viewer, final int index,
            final Supplier<VariableMapping> elementsCreator) {
        super(viewer, index, elementsCreator);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof VariableMapping) {
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(
                    getViewer().getColumnViewerEditor(), parent, RedPlugin.DETAILS_EDITING_CONTEXT_ID);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof VariableMapping) {
            return getMappingValue((VariableMapping) element);
        }
        return "";
    }

    protected abstract String getMappingValue(VariableMapping mapping);

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof VariableMapping) {
            setMappingValue(((VariableMapping) element), (String) value);

            getEventBroker().send(getEventTopic(), element);
        } else {
            super.setValue(element, value);
        }
    }

    protected abstract String getEventTopic();

    protected abstract void setMappingValue(VariableMapping mapping, String value);

    private IEventBroker getEventBroker() {
        return PlatformUI.getWorkbench().getService(IEventBroker.class);
    }

    static class VariableMappingNameEditingSupport extends VariableMappingsDetailsEditingSupport {

        VariableMappingNameEditingSupport(final ColumnViewer viewer,
                final Supplier<VariableMapping> elementsCreator) {
            super(viewer, -1, elementsCreator);
        }

        @Override
        protected String getMappingValue(final VariableMapping mapping) {
            return mapping.getName();
        }

        @Override
        protected void setMappingValue(final VariableMapping mapping, final String value) {
            mapping.setName(value);
        }

        @Override
        protected String getEventTopic() {
            return RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_NAME_CHANGED;
        }
    }

    static class VariableMappingValueEditingSupport extends VariableMappingsDetailsEditingSupport {

        VariableMappingValueEditingSupport(final ColumnViewer viewer,
                final Supplier<VariableMapping> elementsCreator) {
            super(viewer, -1, elementsCreator);
        }

        @Override
        protected String getMappingValue(final VariableMapping mapping) {
            return mapping.getValue();
        }

        @Override
        protected void setMappingValue(final VariableMapping mapping, final String value) {
            mapping.setValue(value);
        }

        @Override
        protected String getEventTopic() {
            return RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_VALUE_CHANGED;
        }
    }
}
