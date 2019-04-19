/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import static com.google.common.collect.Lists.newArrayList;

import java.util.function.Supplier;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;

/**
 * @author Michal Anglart
 */
abstract class VariableMappingsDetailsEditingSupport extends ElementsAddingEditingSupport {

    private final RedProjectEditorInput editorInput;

    private final IEventBroker eventBroker;

    VariableMappingsDetailsEditingSupport(final ColumnViewer viewer, final int index,
            final Supplier<VariableMapping> elementsCreator, final RedProjectEditorInput editorInput,
            final IEventBroker eventBroker) {
        super(viewer, index, elementsCreator);
        this.editorInput = editorInput;
        this.eventBroker = eventBroker;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof VariableMapping) {
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    RedPlugin.DETAILS_EDITING_CONTEXT_ID);
        } else {
            return super.getCellEditor(element);
        }
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof VariableMapping) {
            return getMappingValue((VariableMapping) element);
        } else {
            return null;
        }
    }

    protected abstract String getMappingValue(VariableMapping mapping);

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof VariableMapping) {
            final VariableMapping mapping = (VariableMapping) element;
            final String oldValue = (String) getValue(element);
            final String newValue = (String) value;

            if (!newValue.equals(oldValue)) {
                setMappingValue(mapping, newValue);
                eventBroker.send(getEventTopic(), new RedProjectConfigEventData<>(editorInput.getFile(), mapping));
            }
        } else {
            super.setValue(element, value);
        }
    }

    protected abstract void setMappingValue(VariableMapping mapping, String value);

    protected abstract String getEventTopic();

    static class VariableMappingNameEditingSupport extends VariableMappingsDetailsEditingSupport {

        VariableMappingNameEditingSupport(final ColumnViewer viewer, final Supplier<VariableMapping> elementsCreator,
                final RedProjectEditorInput editorInput, final IEventBroker eventBroker) {
            super(viewer, -1, elementsCreator, editorInput, eventBroker);
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

        VariableMappingValueEditingSupport(final ColumnViewer viewer, final Supplier<VariableMapping> elementsCreator,
                final RedProjectEditorInput editorInput, final IEventBroker eventBroker) {
            super(viewer, -1, elementsCreator, editorInput, eventBroker);
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

    static class VariableMappingCreator implements Supplier<VariableMapping> {

        private final Shell shell;

        private final RedProjectEditorInput editorInput;

        private final IEventBroker eventBroker;

        VariableMappingCreator(final Shell shell, final RedProjectEditorInput editorInput,
                final IEventBroker eventBroker) {
            this.shell = shell;
            this.editorInput = editorInput;
            this.eventBroker = eventBroker;
        }

        @Override
        public VariableMapping get() {
            final VariableMappingDialog dialog = new VariableMappingDialog(shell);
            if (dialog.open() == Window.OK) {
                final VariableMapping mapping = dialog.getMapping();
                final boolean wasAdded = editorInput.getProjectConfiguration().addVariableMapping(mapping);
                if (wasAdded) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_STRUCTURE_CHANGED,
                            new RedProjectConfigEventData<>(editorInput.getFile(), newArrayList(mapping)));
                }
                return mapping;
            }
            return null;
        }
    }
}
