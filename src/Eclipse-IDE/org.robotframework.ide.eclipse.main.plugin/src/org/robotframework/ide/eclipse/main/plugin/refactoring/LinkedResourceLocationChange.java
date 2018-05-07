/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import org.eclipse.core.internal.resources.AliasManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import com.google.common.annotations.VisibleForTesting;

class LinkedResourceLocationChange extends Change {

    private static final int REFRESH_DELAY = 500;

    private final IWorkspace workspace;

    private final int delay;

    private Job locationsUpdateJob;

    LinkedResourceLocationChange(final IWorkspace workspace) {
        this(workspace, REFRESH_DELAY);
    }

    @VisibleForTesting
    LinkedResourceLocationChange(final IWorkspace workspace, final int delay) {
        this.workspace = workspace;
        this.delay = delay;
    }

    @Override
    public String getName() {
        return "Linked resource locations will be refreshed";
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
        locationsUpdateJob = Job.create("Updating linked resource locations", this::rebuildLinkedLocationsCache);
        locationsUpdateJob.schedule(delay);
        return new LinkedResourceLocationChange(workspace, delay);
    }

    private void rebuildLinkedLocationsCache(final IProgressMonitor monitor) {
        final AliasManager aliasManager = ((Workspace) workspace).getAliasManager();
        aliasManager.shutdown(monitor);
        aliasManager.startup(monitor);
    }

    @Override
    public Object getModifiedElement() {
        return workspace;
    }

    @VisibleForTesting
    Job getLocationsUpdateJob() {
        return locationsUpdateJob;
    }

}
