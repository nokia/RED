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
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Jakub Szkatula
 */
class LibraryModifyChange extends Change {

    private final IFile redXmlFile;

    private final ReferencedLibrary library;

    private final ReferencedLibrary newLibrary;

    private final IEventBroker eventBroker;

    LibraryModifyChange(final IFile redXmlFile, final ReferencedLibrary library, final ReferencedLibrary newLibrary) {
        this(redXmlFile, library, newLibrary, PlatformUI.getWorkbench().getService(IEventBroker.class));
    }

    @VisibleForTesting
    LibraryModifyChange(final IFile redXmlFile, final ReferencedLibrary library, final ReferencedLibrary newLibrary,
            final IEventBroker eventBroker) {
        this.redXmlFile = redXmlFile;
        this.library = library;
        this.newLibrary = newLibrary;
        this.eventBroker = eventBroker;
    }

    @Override
    public String getName() {
        return "The library '" + library.getName() + "' (" + library.getPath() + ") will be changed to '"
                + newLibrary.getName() + "' (" + newLibrary.getPath() + ")";
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
        final ReferencedLibrary oldLibrary = ReferencedLibrary.create(library.provideType(), library.getName(),
                library.getPath());

        library.setName(newLibrary.getName());
        library.setPath(newLibrary.getPath());

        final List<ReferencedLibrary> changedPaths = new ArrayList<>();
        changedPaths.add(library);
        final RedProjectConfigEventData<List<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(redXmlFile,
                changedPaths);

        eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);

        return new LibraryModifyChange(redXmlFile, library, oldLibrary);
    }

    @Override
    public Object getModifiedElement() {
        return library;
    }
}
