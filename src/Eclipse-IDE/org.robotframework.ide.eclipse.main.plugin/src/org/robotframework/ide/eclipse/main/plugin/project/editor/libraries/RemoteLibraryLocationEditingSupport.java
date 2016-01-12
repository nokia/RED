/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;


/**
 * @author Michal Anglart
 *
 */
public class RemoteLibraryLocationEditingSupport extends ElementsAddingEditingSupport {

    private final RedProjectEditorInput editorInput;

    public RemoteLibraryLocationEditingSupport(final ColumnViewer viewer, final RedProjectEditorInput editorInput,
            final NewElementsCreator<?> creator) {
        super(viewer, -1, creator);
        this.editorInput = editorInput;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof RemoteLocation) {
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    RobotElementEditingSupport.DETAILS_EDITING_CONTEXT_ID);
        } else {
            return super.getCellEditor(element);
        }
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RemoteLocation) {
            return ((RemoteLocation) element).getUri();
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RemoteLocation) {
            final String newAddress = (String) value;

            try {
                final RemoteLocation remoteLocation = (RemoteLocation) element;
                remoteLocation.setUri(newAddress);

                final RedProjectConfigEventData<RemoteLocation> eventData = new RedProjectConfigEventData<RobotProjectConfig.RemoteLocation>(
                        editorInput.getRobotProject().getConfigurationFile(), remoteLocation);
                getEventBroker().send(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_PATH_CHANGED, eventData);
            } catch (final IllegalArgumentException e) {
                // uri syntax was wrong...
            }
        } else {
            super.setValue(element, value);
        }
    }

    private IEventBroker getEventBroker() {
        return (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
    }
}
