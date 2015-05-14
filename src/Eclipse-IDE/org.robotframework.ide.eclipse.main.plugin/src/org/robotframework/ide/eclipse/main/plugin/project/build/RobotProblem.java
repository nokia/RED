package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class RobotProblem {

    public static final String CAUSE_ATTRIBUTE = "cause";

    private final Severity severity;
    private final Cause cause;
    private final String location;
    private final String message;

    public RobotProblem(final Severity severity, final Cause cause, final String message) {
        this(severity, cause, message, null);
    }

    public RobotProblem(final Severity severity, final Cause cause, final String message, final String location) {
        this.severity = severity;
        this.cause = cause;
        this.message = message;
        this.location = location;
    }

    public void createMarker(final IResource resource) {
        try {
            final IMarker marker = resource.createMarker(IMarker.PROBLEM);
            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.SEVERITY, severity.getLevel());
            marker.setAttribute(IMarker.LOCATION, location);
            marker.setAttribute(CAUSE_ATTRIBUTE, cause.toString());
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create marker!", e);
        }
    }

    public void createMarker(final IFile resource, final int lineNumber) {
        try {
            final IMarker marker = resource.createMarker(IMarker.PROBLEM);
            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.SEVERITY, severity.getLevel());
            marker.setAttribute(IMarker.LOCATION, "line " + lineNumber);
            marker.setAttribute(CAUSE_ATTRIBUTE, cause.toString());
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create marker!", e);
        }
    }

    public enum Cause {
        MISSING_BUILDPATHS_FILE,
        NO_ACTIVE_INSTALLATION,
        INVALID_PYTHON_DIRECTORY,
        MISSING_ROBOT
    }

    public enum Severity {
        ERROR {
            @Override
            int getLevel() {
                return IMarker.SEVERITY_ERROR;
            }
        },
        WARNING {
            @Override
            int getLevel() {
                return IMarker.SEVERITY_WARNING;
            }
        },
        INFO {
            @Override
            int getLevel() {
                return IMarker.SEVERITY_INFO;
            }
        };

        abstract int getLevel();
    }
}
