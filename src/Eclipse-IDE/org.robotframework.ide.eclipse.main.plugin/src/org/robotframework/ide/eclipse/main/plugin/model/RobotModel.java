/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public class RobotModel implements RobotElement {

    private final List<RobotElement> projects = new ArrayList<>();

    public synchronized RobotProject createRobotProject(final IProject project) {
        if (project == null) {
            return null;
        }
        final RobotProject robotProject = new RobotProject(project);
        if (projects.contains(robotProject)) {
            return (RobotProject) projects.get(projects.indexOf(robotProject));
        } else {
            projects.add(robotProject);
            return robotProject;
        }
    }

    synchronized RobotFolder createRobotSuite(final IFolder folder) {
        if (folder == null) {
            return null;
        }
        if (folder.getParent().equals(folder.getProject())) {
            return createRobotProject((IProject) folder.getParent()).createRobotSuite(folder);
        } else {
            return createRobotSuite((IFolder) folder.getParent()).createRobotSuite(folder);
        }
    }

    public synchronized RobotSuiteFile createSuiteFile(final IFile file) {
        if (file == null) {
            return null;
        }
        if (file.getParent().equals(file.getProject())) {
            return createRobotProject((IProject) file.getParent()).createSuiteFile(file);
        } else {
            return createRobotSuite((IFolder) file.getParent()).createSuiteFile(file);
        }
    }

    @Override
    public List<RobotElement> getChildren() {
        return projects;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public RobotElement getParent() {
        return null;
    }

    @Override
    public int getIndex() {
        return -1;
    }

    @Override
    public ImageDescriptor getImage() {
        return null;
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new OpenStrategy();
    }

    synchronized List<RobotElementChange> removeProject(final IProject project) {
        final List<RobotElementChange> changes = new ArrayList<>();
        
        final List<RobotElement> toRemove = new ArrayList<>();
        for (final RobotElement element : projects) {
            if (((RobotProject) element).getProject().equals(project)) {
                toRemove.add(element);
                changes.add(RobotElementChange.createRemovedElement(element));
            }
        }
        projects.removeAll(toRemove);
        return changes;
    }

    synchronized List<RobotElementChange> synchronizeChanges(final IResourceDelta delta) {
        final List<IProject> toRemove = new ArrayList<>();
        final List<RobotElementChange> changes = new ArrayList<>();

        for (final RobotElement element : projects) {
            final RobotProject project = (RobotProject) element;

            final IResourceDelta projectDelta = delta.findMember(project.getProject().getFullPath());
            if (isRemoved(projectDelta)) {
                changes.add(RobotElementChange.createRemovedElement(element));
                toRemove.add((IProject) projectDelta.getResource());
            } else if (isChanged(projectDelta)) {
                changes.add(RobotElementChange.createAddedElement(element));
                changes.addAll(project.synchronizeChanges(delta));
            }
        }

        for (final IProject project : toRemove) {
            removeProject(project);
        }
        return changes;
    }

    private boolean isChanged(final IResourceDelta projectDelta) {
        return projectDelta != null && projectDelta.getKind() == IResourceDelta.CHANGED;
    }

    private boolean isRemoved(final IResourceDelta projectDelta) {
        return projectDelta != null && projectDelta.getKind() == IResourceDelta.REMOVED;
    }
}
