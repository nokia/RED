/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;


/**
 * @author Michal Anglart
 *
 */
public class VariableFileArgumentsEditingSupport extends ElementsAddingEditingSupport {

    public VariableFileArgumentsEditingSupport(final ColumnViewer viewer, final int index,
            final NewElementsCreator<ReferencedVariableFile> creator) {
        super(viewer, index, creator);
    }

    @Override
    protected int getColumnShift() {
        return -index + 1; // if new element will be added this will always activate first argument cell
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof ReferencedVariableFile) {
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    RedPlugin.DETAILS_EDITING_CONTEXT_ID);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof ReferencedVariableFile) {
            final ReferencedVariableFile varFile = (ReferencedVariableFile) element;
            return index < varFile.getArguments().size() ? varFile.getArguments().get(index) : "";
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof ReferencedVariableFile) {
            final String argument = (String) value;
            final ReferencedVariableFile variableFile = (ReferencedVariableFile) element;
            List<String> arguments = variableFile.getArguments();
            if (arguments.isEmpty()) {
                arguments = new ArrayList<>();
                variableFile.setArguments(arguments);
            }

            if (index < arguments.size()) {
                arguments.set(index, argument);
            } else {
                arguments.add(argument);
            }

            final List<String> filteredArguments = new ArrayList<>();
            Collections.reverse(arguments);
            boolean nonWhitespaceFound = false;
            for (final String arg : arguments) {
                if (!arg.isEmpty()) {
                    nonWhitespaceFound = true;
                }
                if (nonWhitespaceFound) {
                    filteredArguments.add(arg);
                }
            }
            Collections.reverse(filteredArguments);
            variableFile.setArguments(filteredArguments);

            getEventBroker().send(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_FILE_ARGUMENT_CHANGED, variableFile);
        } else {
            super.setValue(element, value);
        }
    }

    private IEventBroker getEventBroker() {
        return (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
    }
}
