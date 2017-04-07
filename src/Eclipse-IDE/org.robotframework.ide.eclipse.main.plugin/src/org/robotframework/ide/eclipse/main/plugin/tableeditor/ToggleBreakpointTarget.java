/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ToggleBreakpointHandler.E4ToggleBreakpointHandler;

public class ToggleBreakpointTarget implements IToggleBreakpointsTarget {

    @Override
    public boolean canToggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection) {
        if (part instanceof RobotFormEditor && selection instanceof ITextSelection) {
            return ((RobotFormEditor) part).getEditorInput() instanceof IFileEditorInput;
        }
        return false;
    }

    @Override
    public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
        final IEditorInput input = ((RobotFormEditor) part).getEditorInput();
        final IFile file = ((IFileEditorInput) input).getFile();
        final int line = ((ITextSelection) selection).getStartLine() + 1;
        E4ToggleBreakpointHandler.toggle(file, line);
    }

    @Override
    public boolean canToggleMethodBreakpoints(final IWorkbenchPart part, final ISelection selection) {
        return false;
    }

    @Override
    public void toggleMethodBreakpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
        // nothing to do
    }

    @Override
    public boolean canToggleWatchpoints(final IWorkbenchPart part, final ISelection selection) {
        return false;
    }

    @Override
    public void toggleWatchpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
        // nothing to do
    }
}
