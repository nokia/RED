/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.project.RobotProjectConfig;

class RedXmlFileChangeListener implements IResourceChangeListener {

    private final IProject project;

    private final OnRedConfigFileChange performOnChange;

    RedXmlFileChangeListener(final IProject project,
            final OnRedConfigFileChange performOnChange) {
        this.project = project;
        this.performOnChange = performOnChange;
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE && event.getDelta() != null) {
            try {
                event.getDelta().accept(delta -> {
                    if (delta.getResource().getFullPath().segmentCount() > 2) {
                        return false;
                    }
                    if (delta.getResource().equals(project.getFile(RobotProjectConfig.FILENAME))) {
                        if (delta.getKind() == IResourceDelta.REMOVED
                                && (delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
                            performOnChange.whenFileMoved(delta.getMovedToPath());
                        } else if (delta.getKind() == IResourceDelta.REMOVED) {
                            performOnChange.whenFileRemoved();
                        } else if (delta.getKind() == IResourceDelta.CHANGED
                                && (delta.getFlags() & IResourceDelta.MARKERS) == 0) {
                            performOnChange.whenFileChanged();
                        } else if (delta.getKind() == IResourceDelta.CHANGED) {
                            performOnChange.whenFileMarkerChanged();
                        }
                        return false;
                    }
                    return true;
                });
            } catch (final CoreException e) {
                // nothing to do
            }
        }
    }

    interface OnRedConfigFileChange {

        void whenFileMoved(IPath movedToPath);

        void whenFileRemoved();

        void whenFileChanged();

        void whenFileMarkerChanged();
    }
}