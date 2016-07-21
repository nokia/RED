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
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;


/**
 * @author Michal Anglart
 *
 */
class ReferencedLibrariesEditingSupport extends EditingSupport {

    private final RedProjectEditorInput editorInput;

    private final IEventBroker eventBroker;

    ReferencedLibrariesEditingSupport(final ColumnViewer viewer, final RedProjectEditorInput editorInput,
            final IEventBroker eventBroker) {
        super(viewer);
        this.editorInput = editorInput;
        this.eventBroker = eventBroker;
    }

    @Override
    protected boolean canEdit(final Object element) {
        return element instanceof RemoteLocation;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof RemoteLocation) {
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    RedPlugin.DETAILS_EDITING_CONTEXT_ID);
        }
        return null;
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
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_PATH_CHANGED, eventData);
            } catch (final IllegalArgumentException e) {
                // uri syntax was wrong...
            }
        }
    }
}
