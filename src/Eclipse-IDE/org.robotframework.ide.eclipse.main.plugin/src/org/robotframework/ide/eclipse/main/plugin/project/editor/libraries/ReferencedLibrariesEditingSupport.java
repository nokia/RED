/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.function.Consumer;

import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;


/**
 * @author Michal Anglart
 *
 */
class ReferencedLibrariesEditingSupport extends EditingSupport {

    private final Consumer<RemoteLocation> successHandler;

    ReferencedLibrariesEditingSupport(final ColumnViewer viewer, final Consumer<RemoteLocation> successHandler) {
        super(viewer);
        this.successHandler = successHandler;
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
            try {
                final RemoteLocation remoteLocation = (RemoteLocation) element;
                final String oldValue = (String) getValue(element);
                final String newValue = (String) value;

                if (!newValue.equals(oldValue)) {
                    remoteLocation.setUri(newValue);
                    successHandler.accept(remoteLocation);
                }
            } catch (final IllegalArgumentException e) {
                // uri syntax was wrong...
            }
        }
    }
}
