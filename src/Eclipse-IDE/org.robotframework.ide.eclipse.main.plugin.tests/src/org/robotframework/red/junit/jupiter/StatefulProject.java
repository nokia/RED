/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.project.RobotProjectConfig;

public class StatefulProject {

    private final IProject project;

    private final List<IResource> createdResources;

    StatefulProject(final IProject project) {
        this.project = project;
        this.createdResources = new ArrayList<>();
    }

    public String getName() {
        return project.getName();
    }

    public IProject getProject() {
        return project;
    }

    public IWorkspace getWorkspace() {
        return project.getWorkspace();
    }

    public IFolder getDir(final String filePath) {
        return ProjectExtension.getDir(project, filePath);
    }

    public IFile getFile(final String filePath) {
        return ProjectExtension.getFile(project, filePath);
    }

    public void createFile(final String filePath, final String... lines) throws IOException, CoreException {
        createFile(CleanMode.TEMPORAL, filePath, lines);
    }

    public void createFile(final CleanMode mode, final String filePath, final String... lines)
            throws IOException, CoreException {
        final IFile file = ProjectExtension.createFile(project, filePath, lines);
        if (mode == CleanMode.TEMPORAL) {
            createdResources.add(file);
        }
    }

    public void createFileLink(final String filePath, final URI targetUri) throws CoreException {
        createFileLink(CleanMode.TEMPORAL, filePath, targetUri);
    }

    public void createFileLink(final CleanMode mode, final String filePath, final URI targetUri) throws CoreException {
        createLink(mode, getFile(filePath), targetUri);
    }

    private void createLink(final CleanMode mode, final IFile linkingFile, final URI targetUri) throws CoreException {
        linkingFile.createLink(targetUri, IResource.REPLACE, null);
        if (mode == CleanMode.TEMPORAL) {
            createdResources.add(linkingFile);
        }
    }

    public void createDirLink(final String filePath, final URI targetUri) throws CoreException {
        createLink(CleanMode.TEMPORAL, getDir(filePath), targetUri);
    }

    public void createDirLink(final CleanMode mode, final String filePath, final URI targetUri) throws CoreException {
        createLink(mode, getDir(filePath), targetUri);
    }

    private void createLink(final CleanMode mode, final IFolder linkingFolder, final URI targetUri)
            throws CoreException {
        linkingFolder.createLink(targetUri, IResource.REPLACE, null);
        if (mode == CleanMode.TEMPORAL) {
            createdResources.add(linkingFolder);
        }
    }

    public void createVirtualDir(final String filePath) throws CoreException {
        createVirtualDir(CleanMode.TEMPORAL, filePath);
    }

    public void createVirtualDir(final CleanMode mode, final String filePath) throws CoreException {
        createVirtual(mode, getDir(filePath));
    }

    private void createVirtual(final CleanMode mode, final IFolder virtualFolder) throws CoreException {
        virtualFolder.create(IResource.REPLACE | IResource.VIRTUAL, true, null);
        if (mode == CleanMode.TEMPORAL) {
            createdResources.add(virtualFolder);
        }
    }

    public void configure() throws IOException, CoreException {
        ProjectExtension.configure(project);
    }

    public void configure(final RobotProjectConfig config) throws IOException, CoreException {
        ProjectExtension.configure(project, config);
    }

    public void deconfigure() throws CoreException {
        ProjectExtension.deconfigure(project);
    }

    public void move(final File destination) throws CoreException {
        final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
        description.setLocation(new Path(destination.getAbsolutePath()));
        project.move(description, true, null);
    }

    public void cleanUp() {
        for (final IResource resource : createdResources) {
            try {
                resource.delete(true, null);
            } catch (final CoreException e) {
            }
        }
        createdResources.clear();
    }

    public static enum CleanMode {
        TEMPORAL,
        NONTEMPORAL;
    }
}
