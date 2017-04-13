/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;

import com.google.common.annotations.VisibleForTesting;


/**
 * @author Jakub Szkatula
 */
class LibraryPathRemoveChange extends Change {

    private final IFile redXmlFile;

    private final ReferencedLibrary libraryPathToRemove;

    private final RobotProjectConfig config;

    private final IEventBroker eventBroker;

    LibraryPathRemoveChange(final IFile redXmlFile, final RobotProjectConfig config,
            final ReferencedLibrary lib) {
        this(redXmlFile, config, lib,
                (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class));
    }

    @VisibleForTesting
    LibraryPathRemoveChange(final IFile redXmlFile, final RobotProjectConfig config,
            final ReferencedLibrary excludedPathToRemove, final IEventBroker eventBroker) {
        this.redXmlFile = redXmlFile;
        this.config = config;
        this.libraryPathToRemove = excludedPathToRemove;
        this.eventBroker = eventBroker;
    }

    @Override
    public String getName() {
        return "The path '" + libraryPathToRemove.getPath() + "' will be removed";
    }

    @Override
    public void initializeValidationData(final IProgressMonitor pm) {
        // nothing to do currently
    }

    @Override
    public RefactoringStatus isValid(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return new RefactoringStatus();
    }

    @Override
    public Change perform(final IProgressMonitor pm) throws CoreException {
        config.getLibraries().remove(libraryPathToRemove);
        
        final List<ReferencedLibrary>  changedPaths = new ArrayList<>();
        changedPaths.add(libraryPathToRemove);
        final RedProjectConfigEventData<List<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(
                redXmlFile, changedPaths);


        final RobotProject robotProject = RedPlugin.getModelManager().createProject(redXmlFile.getProject());
        eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);
        new LibrariesAutoDiscoverer(robotProject, Collections.<IResource> emptyList(), eventBroker).start();

        return new LibraryPathAddChange(redXmlFile, config, libraryPathToRemove);
    }

    @Override
    public Object getModifiedElement() {
        return libraryPathToRemove;
    }
}
