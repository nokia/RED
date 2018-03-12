/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;

public class RfLintProblem {

    public static final String TYPE_ID = RedPlugin.PLUGIN_ID + ".rfLintProblem";

    private final String ruleName;

    private final RfLintViolationSeverity severity;

    private final String message;

    static RfLintProblem causedBy(final String ruleName, final RfLintViolationSeverity severity,
            final String message) {
        return new RfLintProblem(ruleName, severity, message);
    }

    static void cleanProblems(final List<IResource> selectedResources) {
        for (final IResource resource : selectedResources) {
            try {
                resource.deleteMarkers(TYPE_ID, true, IResource.DEPTH_INFINITE);
            } catch (final CoreException e) {
                throw new IllegalStateException(
                        "Unable to remove RfLint problems from " + resource.getFullPath().toOSString());
            }
        }
    }

    private RfLintProblem(final String ruleName, final RfLintViolationSeverity severity, final String message) {
        this.ruleName = ruleName;
        this.severity = severity;
        this.message = message;
    }

    void createMarker(final File file, final int line) {
        final RedWorkspace ws = new RedWorkspace(ResourcesPlugin.getWorkspace().getRoot());
        final IFile wsFile = ws.fileForUri(file.toURI()).orElseThrow(
                () -> new IllegalStateException("The file " + file.getAbsolutePath() + " cannot be find in workspace"));
        createMarker(wsFile, line);
    }

    private void createMarker(final IFile file, final int line) {
        try {
            final IMarker marker = file.createMarker(TYPE_ID);
            marker.setAttribute(IMarker.MESSAGE, getMessage().intern());
            marker.setAttribute(IMarker.SEVERITY, getSeverity());
            marker.setAttribute(IMarker.LOCATION, ("line " + line).intern());
            marker.setAttribute(IMarker.LINE_NUMBER, line);

        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create marker!", e);
        }
    }

    private int getSeverity() {
        switch (severity) {
            case ERROR:
                return IMarker.SEVERITY_ERROR;
            case WARNING:
                return IMarker.SEVERITY_WARNING;
            default:
                return IMarker.SEVERITY_INFO;
        }
    }

    private String getMessage() {
        return message + " (" + ruleName + ")";
    }
}
