/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;


/**
 * @author Michal Anglart
 *
 */
class RobotConsolePatternsListener implements IPatternMatchListener {

    private TextConsole console;

    private final RobotProject robotProject;

    public RobotConsolePatternsListener(final RobotProject robotProject) {
        this.robotProject = robotProject;
    }

    @Override
    public void connect(final TextConsole console) {
        this.console = console;
    }

    @Override
    public void disconnect() {
        this.console = null;
    }

    @Override
    public void matchFound(final PatternMatchEvent event) {
        try {
            final String matchedLine = console.getDocument().get(event.getOffset(), event.getLength());
            final String strippedFile = getPath(matchedLine);
            final File file = new File(strippedFile);

            if (file.exists() && file.isFile()) {
                final int offset = event.getOffset() + (event.getLength() - strippedFile.length());
                final int length = strippedFile.length();

                console.addHyperlink(new ExecutionArtifactsHyperlink(robotProject.getProject(), file), offset, length);
            }

        } catch (final BadLocationException e) {
            // fine, no hyperlinks then
        }
    }

    private String getPath(final String matchedLine) {
        if (matchedLine.startsWith("Output:")) {
            return matchedLine.substring("Output:".length()).trim();
        } else if (matchedLine.startsWith("Log:")) {
            return matchedLine.substring("Log:".length()).trim();
        } else if (matchedLine.startsWith("Report:")) {
            return matchedLine.substring("Report:".length()).trim();
        }
        return null;
    }

    @Override
    public String getPattern() {
        return "(Output|Log|Report):.*";
    }

    @Override
    public int getCompilerFlags() {
        return 0;
    }

    @Override
    public String getLineQualifier() {
        return "(Output|Log|Report): ";
    }

    private static final class ExecutionArtifactsHyperlink implements IHyperlink {

        private final IProject project;
        private final File file;

        private ExecutionArtifactsHyperlink(final IProject project, final File file) {
            this.project = project;
            this.file = file;
        }

        @Override
        public void linkExited() {
            // nothing to do
        }

        @Override
        public void linkEntered() {
            // nothing to do
        }

        @Override
        public void linkActivated() {
            final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

            if (!file.exists()) { // it could have been deleted in the meantime
                final IStatus status = new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                        "The file " + file.getAbsolutePath() + " does not exist in the file system anymore.");
                ErrorDialog.openError(workbenchWindow.getShell(), "Missing file", "File does not exist", status);
                return;
            }

            final IPath fileAsPath = new Path(file.getAbsolutePath());
            final IPath wsRelative = PathsConverter.toWorkspaceRelativeIfPossible(fileAsPath);
            final boolean wasConverted = !wsRelative.equals(fileAsPath);

            IFile wsFile;
            if (!wasConverted) {
                wsFile = LibspecsFolder.get(project).getFile(fileAsPath.lastSegment());
                try {
                    wsFile.createLink(wsRelative, IResource.REPLACE | IResource.HIDDEN, null);
                } catch (final CoreException e) {
                    throw new IllegalArgumentException("Unable to open file", e);
                }
            } else {
                final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                wsFile = (IFile) root.findMember(wsRelative);
            }

            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

            try {
                if (wsFile == null) {
                    wsFile = refreshParentDir(root, wsRelative);
                }
                refreshFile(wsFile);
                openInEditor(workbenchWindow, wsFile);
            } catch (final CoreException e) {
                final String message = "Unable to open editor for file: " + wsFile.getName();
                ErrorDialog.openError(workbenchWindow.getShell(), "Error opening file", message,
                        new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, e));
            }
        }

        private IFile refreshParentDir(final IWorkspaceRoot root, final IPath wsRelative) throws CoreException {
            root.findMember(wsRelative.removeLastSegments(1)).refreshLocal(IResource.DEPTH_ONE, null);
            return (IFile) root.findMember(wsRelative);
        }

        private void refreshFile(final IFile wsFile) throws CoreException {
            if (!wsFile.isSynchronized(IResource.DEPTH_ZERO)) {
                wsFile.refreshLocal(IResource.DEPTH_ZERO, null);
            }
        }

        private void openInEditor(final IWorkbenchWindow workbenchWindow, final IFile wsFile) throws PartInitException {
            final IEditorDescriptor desc = IDE.getEditorDescriptor(wsFile);
            workbenchWindow.getActivePage().openEditor(new FileEditorInput(wsFile), desc.getId());
        }
    }
}
