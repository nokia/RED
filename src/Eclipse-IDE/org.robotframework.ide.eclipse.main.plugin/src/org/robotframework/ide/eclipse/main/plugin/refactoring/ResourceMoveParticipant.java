/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.Optional;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;


public class ResourceMoveParticipant extends MoveParticipant {

    private IResource movedResource;

    @Override
    protected boolean initialize(final Object element) {
        this.movedResource = (IResource) element;
        return true;
    }

    @Override
    public String getName() {
        return "Robot resource move participant";
    }

    @Override
    public RefactoringStatus checkConditions(final IProgressMonitor pm, final CheckConditionsContext context)
            throws OperationCanceledException {
        return new RefactoringStatus();
    }

    @Override
    public Change createPreChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
        if (!getArguments().getUpdateReferences()) {
            return null;
        }
        final IPath newPath = ((IContainer) getArguments().getDestination()).getFullPath()
                .append(movedResource.getName());

        final Optional<Change> change = new RedXmlChangesCollector().collect(movedResource, Optional.of(newPath));
        return change.isPresent() && !(change.get() instanceof NullChange) ? change.get() : null;
    }

    @Override
    public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
        // This workaround solves problem with resource location cache
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=534090
        return movedResource.isLinked() ? new LinkedResourceLocationChange(movedResource.getProject().getWorkspace())
                : null;
    }
}
