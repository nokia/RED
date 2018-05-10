/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.ide.IDE;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
public class RobotConsolePatternsListener implements IPatternMatchListener {

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
            final PathWithOffset pathWithOffset = findPathWithOffset(matchedLine);
            if (!pathWithOffset.path.isEmpty()) {
                final IHyperlink hyperlink = createHyperlink(pathWithOffset.path);
                final int offset = event.getOffset() + pathWithOffset.offsetInLine;
                final int length = pathWithOffset.path.length();
                console.addHyperlink(hyperlink, offset, length);
            }
        } catch (final BadLocationException | URISyntaxException e) {
            // fine, no hyperlinks then
        }
    }

    private IHyperlink createHyperlink(final String path) throws URISyntaxException {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return new ExecutionWebsiteHyperlink(new URI(path));
        } else {
            return new ExecutionArtifactsHyperlink(robotProject.getProject(), new File(path));
        }
    }

    private PathWithOffset findPathWithOffset(final String matchedLine) {
        if (matchedLine.startsWith("Debug:")) {
            return PathWithOffset.createFromPrefixedLine(matchedLine, "Debug:");
        } else if (matchedLine.startsWith("Output:")) {
            return PathWithOffset.createFromPrefixedLine(matchedLine, "Output:");
        } else if (matchedLine.startsWith("XUnit:")) {
            return PathWithOffset.createFromPrefixedLine(matchedLine, "XUnit:");
        } else if (matchedLine.startsWith("Log:")) {
            return PathWithOffset.createFromPrefixedLine(matchedLine, "Log:");
        } else if (matchedLine.startsWith("Report:")) {
            return PathWithOffset.createFromPrefixedLine(matchedLine, "Report:");
        } else if (matchedLine.startsWith("Command:")) {
            final String listenerArg = "--argumentfile ";
            final int listenerArgIndex = matchedLine.indexOf(listenerArg);
            if (listenerArgIndex == -1) {
                return new PathWithOffset("", 0);
            }
            final int start = listenerArgIndex + listenerArg.length();
            final int end = matchedLine.indexOf(' ', start);
            return new PathWithOffset(matchedLine.substring(start, end), start);
        }
        throw new IllegalStateException();
    }

    @Override
    public String getPattern() {
        return "(Debug|Output|XUnit|Log|Report|Command):\\s*(.*)";
    }

    @Override
    public int getCompilerFlags() {
        return 0;
    }

    @Override
    public String getLineQualifier() {
        return "(Debug|Output|XUnit|Log|Report|Command): ";
    }

    @VisibleForTesting
    static final class ExecutionArtifactsHyperlink implements IHyperlink {

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

            if (!file.isFile() || !file.exists()) { // it could have been deleted in the meantime
                final IStatus status = new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                        "The file " + file.getAbsolutePath() + " does not exist in the file system.");
                ErrorDialog.openError(workbenchWindow.getShell(), "Missing file", "File does not exist", status);
                return;
            }

            final IWorkspaceRoot root = project.getWorkspace().getRoot();
            final RedWorkspace workspace = new RedWorkspace(root);
            IFile wsFile = (IFile) workspace.forUri(file.toURI());

            if (wsFile == null) {
                wsFile = LibspecsFolder.get(project).getFile(file.getName());
                try {
                    wsFile.createLink(file.toURI(), IResource.REPLACE | IResource.HIDDEN, null);
                } catch (final CoreException e) {
                    throw new IllegalArgumentException("Unable to open file", e);
                }
            } else if (!wsFile.exists()) {
                try {
                    refreshAllNeededResources(root, wsFile.getFullPath());
                    refreshFile(wsFile);
                } catch (final CoreException e) {
                    final String message = "Unable to open editor for file: " + wsFile.getName();
                    ErrorDialog.openError(workbenchWindow.getShell(), "Error opening file", message,
                            new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, e));
                }
            }
            try {
                IDE.openEditor(workbenchWindow.getActivePage(), wsFile);
            } catch (final PartInitException e) {
                final String message = "Unable to open editor for file: " + wsFile.getName();
                ErrorDialog.openError(workbenchWindow.getShell(), "Error opening file", message,
                        new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, e));
            }
        }

        private IFile refreshAllNeededResources(final IWorkspaceRoot root, final IPath wsRelative)
                throws CoreException {
            IPath path = wsRelative;
            final List<String> removed = newArrayList();
            while (root.findMember(path) == null) {
                removed.add(0, path.lastSegment());
                path = path.removeLastSegments(1);
            }
            for (final String segment : removed) {
                root.findMember(path).refreshLocal(IResource.DEPTH_ONE, null);
                path = path.append(segment);
            }
            return (IFile) root.findMember(wsRelative);
        }

        private void refreshFile(final IFile wsFile) throws CoreException {
            if (!wsFile.isSynchronized(IResource.DEPTH_ZERO)) {
                wsFile.refreshLocal(IResource.DEPTH_ZERO, null);
            }
        }
    }

    @VisibleForTesting
    static final class ExecutionWebsiteHyperlink implements IHyperlink {

        private final URI link;

        private ExecutionWebsiteHyperlink(final URI link) {
            this.link = link;
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

            final IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
            try {
                final IWebBrowser browser = support.createBrowser("default");
                browser.openURL(link.toURL());
            } catch (PartInitException | MalformedURLException e) {
                final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                final String message = "Unable to open link: " + link.toString();
                ErrorDialog.openError(workbenchWindow.getShell(), "Error opening link", message,
                        new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, e));
            }
        }
    }

    private static class PathWithOffset {

        private final String path;

        private final int offsetInLine;

        private static PathWithOffset createFromPrefixedLine(final String matchedLine, final String prefix) {
            final String path = matchedLine.substring(prefix.length()).trim();
            final int offset = matchedLine.length() - path.length();
            return new PathWithOffset(path, offset);
        }

        private PathWithOffset(final String path, final int offsetInLine) {
            this.path = path;
            this.offsetInLine = offsetInLine;
        }
    }
}
