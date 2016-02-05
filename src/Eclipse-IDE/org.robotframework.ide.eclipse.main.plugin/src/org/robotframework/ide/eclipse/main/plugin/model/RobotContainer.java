/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public abstract class RobotContainer implements RobotElement {

    private final RobotElement parent;
    protected final IContainer container;
    private final List<RobotElement> elements;

    RobotContainer(final RobotElement parent, final IContainer container) {
        this.parent = parent;
        this.container = container;
        this.elements = new ArrayList<>();
    }

    synchronized RobotFolder createRobotSuite(final IFolder folder) {
        if (folder == null) {
            return null;
        }
        final RobotFolder robotFolder = new RobotFolder(this, folder);
        if (elements.contains(robotFolder)) {
            return (RobotFolder) elements.get(elements.indexOf(robotFolder));
        } else {
            elements.add(robotFolder);
            return robotFolder;
        }
    }

    synchronized RobotSuiteFile createSuiteFile(final IFile file) {
        if (file == null) {
            return null;
        }
        final RobotSuiteFile robotFile = new RobotSuiteFile(this, file);
        if (elements.contains(robotFile)) {
            return (RobotSuiteFile) elements.get(elements.indexOf(robotFile));
        } else {
            elements.add(robotFile);
            return robotFile;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == getClass()) {
            final RobotContainer other = (RobotContainer) obj;
            return container.equals(other.container);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return container.hashCode();
    }

    @Override
    public String getName() {
        return container.getName();
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    @Override
    public List<RobotElement> getChildren() {
        return elements;
    }

    @Override
    public ImageDescriptor getImage() {
        return null;
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new OpenStrategy();
    }

    List<RobotElementChange> synchronizeChanges(final IResourceDelta delta) {
        final List<RobotElement> toRemove = new ArrayList<>();
        final List<RobotElementChange> changes = new ArrayList<>();

        for (final RobotElement element : elements) {
            if (element instanceof RobotContainer) {
                final RobotContainer folder = (RobotContainer) element;
                final IResourceDelta elementDelta = delta.findMember(folder.container.getFullPath());

                if (isRemoved(elementDelta)) {
                    changes.add(RobotElementChange.createRemovedElement(folder));
                    toRemove.add(folder);
                } else if (isChanged(elementDelta)) {
                    changes.add(RobotElementChange.createChangedElement(folder));
                    changes.addAll(folder.synchronizeChanges(delta));
                }
            } else if (element instanceof RobotSuiteFile) {
                final RobotSuiteFile file = (RobotSuiteFile) element;
                final IResourceDelta elementDelta = delta.findMember(file.getFile().getFullPath());

                if (isRemoved(elementDelta)) {
                    changes.add(RobotElementChange.createRemovedElement(file));
                    toRemove.add(file);
                } else if (isChanged(elementDelta)) {
                    changes.add(RobotElementChange.createChangedElement(file));
                    changes.addAll(file.synchronizeChanges(elementDelta));
                }
            }
        }
        elements.removeAll(toRemove);
        return changes;
    }

    private boolean isChanged(final IResourceDelta elementDelta) {
        return elementDelta != null && elementDelta.getKind() == IResourceDelta.CHANGED
                && !((elementDelta.getFlags() & IResourceDelta.MARKERS) == IResourceDelta.MARKERS);
    }

    private boolean isRemoved(final IResourceDelta elementDelta) {
        return elementDelta != null && elementDelta.getKind() == IResourceDelta.REMOVED;
    }
}
