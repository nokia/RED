/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.util.function.Supplier;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;

class RemoteLocationsEditingSupport extends ElementsAddingEditingSupport {

    private final RedProjectEditorInput editorInput;

    private final IEventBroker eventBroker;

    RemoteLocationsEditingSupport(final ColumnViewer viewer, final Supplier<RemoteLocation> elementsCreator,
            final RedProjectEditorInput editorInput, final IEventBroker eventBroker) {
        super(viewer, 0, elementsCreator);
        this.editorInput = editorInput;
        this.eventBroker = eventBroker;
    }

    @Override
    protected int getColumnShift() {
        return 1;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof RemoteLocation) {
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    RedPlugin.DETAILS_EDITING_CONTEXT_ID);
        } else {
            return super.getCellEditor(element);
        }
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RemoteLocation) {
            return ((RemoteLocation) element).getUri();
        } else {
            return null;
        }
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RemoteLocation) {
            try {
                final RemoteLocation remoteLocation = (RemoteLocation) element;
                final String oldValue = (String) getValue(element);
                final String newValue = (String) value;

                if (!newValue.equals(oldValue)) {
                    remoteLocation.setUri(newValue);
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_PATH_CHANGED,
                            new RedProjectConfigEventData<>(editorInput.getFile(), remoteLocation));
                }
            } catch (final IllegalArgumentException e) {
                // uri syntax was wrong...
            }
        } else {
            super.setValue(element, value);
        }
    }

    static class RemoteLocationCreator implements Supplier<RemoteLocation> {

        private final Shell shell;

        private final RedProjectEditorInput editorInput;

        private final IEventBroker eventBroker;

        public RemoteLocationCreator(final Shell shell, final RedProjectEditorInput editorInput,
                final IEventBroker eventBroker) {
            this.shell = shell;
            this.editorInput = editorInput;
            this.eventBroker = eventBroker;
        }

        @Override
        public RemoteLocation get() {
            final RemoteLocationDialog dialog = new RemoteLocationDialog(shell);
            if (dialog.open() == Window.OK) {
                final RemoteLocation remoteLocation = dialog.getRemoteLocation();
                final boolean wasAdded = editorInput.getProjectConfiguration().addRemoteLocation(remoteLocation);
                if (wasAdded) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_STRUCTURE_CHANGED,
                            new RedProjectConfigEventData<>(editorInput.getFile(), newArrayList(remoteLocation)));
                }
                return remoteLocation;
            }
            return null;
        }
    }
}
