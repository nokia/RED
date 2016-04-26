/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;

import com.google.common.annotations.VisibleForTesting;


/**
 * @author Michal Anglart
 *
 */
class ExcludedPathModifyChange extends Change {

    private final IFile redXmlFile;

    private final ExcludedFolderPath excludedPath;

    private final IPath newPath;

    private final IEventBroker eventBroker;

    ExcludedPathModifyChange(final IFile redXmlFile, final ExcludedFolderPath excludedPath, final IPath newPath) {
        this(redXmlFile, excludedPath, newPath, PlatformUI.getWorkbench().getService(IEventBroker.class));
    }

    @VisibleForTesting
    ExcludedPathModifyChange(final IFile redXmlFile, final ExcludedFolderPath excludedPath, final IPath newPath,
            final IEventBroker eventBroker) {
        this.redXmlFile = redXmlFile;
        this.excludedPath = excludedPath;
        this.newPath = newPath;
        this.eventBroker = eventBroker;
    }

    @Override
    public String getName() {
        return "The path '" + excludedPath.getPath() + "' will change to '" + newPath.toPortableString() + "'";
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
        final IPath oldPath = excludedPath.asPath();
        excludedPath.setPath(newPath.toPortableString());

        final Collection<IPath> changedPaths = new ArrayList<IPath>();
        final RedProjectConfigEventData<Collection<IPath>> eventData = new RedProjectConfigEventData<>(
                redXmlFile, changedPaths);

        eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED, eventData);

        return new ExcludedPathModifyChange(redXmlFile, excludedPath, oldPath);
    }

    @Override
    public Object getModifiedElement() {
        return excludedPath;
    }
}
