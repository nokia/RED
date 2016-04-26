/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;

import com.google.common.base.Optional;


public class ResourceDeleteParticipant extends DeleteParticipant {

    private IResource deletedResource;

    @Override
    protected boolean initialize(final Object element) {
        this.deletedResource = (IResource) element;
        return true;
    }

    @Override
    public String getName() {
        return "Robot resource delete participant";
    }

    @Override
    public RefactoringStatus checkConditions(final IProgressMonitor pm, final CheckConditionsContext context)
            throws OperationCanceledException {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
        final Optional<IPath> newPath = Optional.absent();

        final Optional<Change> change = new RedXmlChangesCollector().collect(deletedResource, newPath);
        return change.isPresent() && !(change.get() instanceof NullChange) ? change.get() : null;
    }

}
