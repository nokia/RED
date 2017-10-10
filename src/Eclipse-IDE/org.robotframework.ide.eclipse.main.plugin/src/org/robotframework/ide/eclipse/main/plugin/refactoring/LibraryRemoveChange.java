/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Jakub Szkatula
 */
class LibraryRemoveChange extends Change {

    private final IFile redXmlFile;

    private final ReferencedLibrary libraryToRemove;

    private final RobotProjectConfig config;

    private final IEventBroker eventBroker;

    LibraryRemoveChange(final IFile redXmlFile, final RobotProjectConfig config,
            final ReferencedLibrary libraryToRemove) {
        this(redXmlFile, config, libraryToRemove, PlatformUI.getWorkbench().getService(IEventBroker.class));
    }

    @VisibleForTesting
    LibraryRemoveChange(final IFile redXmlFile, final RobotProjectConfig config,
            final ReferencedLibrary libraryToRemove, final IEventBroker eventBroker) {
        this.redXmlFile = redXmlFile;
        this.config = config;
        this.libraryToRemove = libraryToRemove;
        this.eventBroker = eventBroker;
    }

    @Override
    public String getName() {
        return "The library '" + libraryToRemove.getName() + "' (" + libraryToRemove.getPath() + ") will be removed";
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
        config.getLibraries().remove(libraryToRemove);

        final List<ReferencedLibrary> changedPaths = new ArrayList<>();
        changedPaths.add(libraryToRemove);
        final RedProjectConfigEventData<List<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(redXmlFile,
                changedPaths);

        eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);

        return new LibraryAddChange(redXmlFile, config, libraryToRemove);
    }

    @Override
    public Object getModifiedElement() {
        return libraryToRemove;
    }
}
