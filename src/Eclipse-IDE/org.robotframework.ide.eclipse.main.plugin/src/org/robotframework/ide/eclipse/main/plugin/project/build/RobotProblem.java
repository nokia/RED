package org.robotframework.ide.eclipse.main.plugin.project.build;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.InstallRobotUsingPipFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.MissingPythonInstallationFixer;

public class RobotProblem {

    public static final String CAUSE_ENUM_CLASS = "class";
    public static final String CAUSE_ATTRIBUTE = "cause";

    private final IProblemCause cause;
    private final String location;
    private Object[] objects;

    private RobotProblem(final IProblemCause cause) {
        this(cause, null);
    }

    private RobotProblem(final IProblemCause cause, final String location) {
        this.cause = cause;
        this.location = location;
        this.objects = null;
    }

    public static RobotProblem causedBy(final IProblemCause cause) {
        return new RobotProblem(cause);
    }

    public void fillFormattedMessageWith(final Object... objects) {
        this.objects = objects;
    }

    public void createMarker(final IResource resource) {
        try {
            final IMarker marker = resource.createMarker(IMarker.PROBLEM);
            marker.setAttribute(IMarker.MESSAGE, getMessage());
            marker.setAttribute(IMarker.SEVERITY, cause.getSeverity().getLevel());
            marker.setAttribute(IMarker.LOCATION, location);
            marker.setAttribute(CAUSE_ENUM_CLASS, cause.getEnumClassName());
            marker.setAttribute(CAUSE_ATTRIBUTE, cause.toString());
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create marker!", e);
        }
    }

    private String getMessage() {
        return cause.getFormattedProblemDescription(objects == null ? new Object[0] : objects);
    }

    public void createMarker(final IFile resource, final int lineNumber) {
        try {
            final IMarker marker = resource.createMarker(IMarker.PROBLEM);
            marker.setAttribute(IMarker.MESSAGE, getMessage());
            marker.setAttribute(IMarker.SEVERITY, cause.getSeverity().getLevel());
            marker.setAttribute(IMarker.LOCATION, "line " + lineNumber);
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            marker.setAttribute(CAUSE_ENUM_CLASS, cause.getEnumClassName());
            marker.setAttribute(CAUSE_ATTRIBUTE, cause.toString());
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create marker!", e);
        }
    }

    public enum Cause {
        CONFIG_FILE_PROBLEM {
            @Override
            public boolean hasResolution() {
                return false;
            }

            @Override
            public List<? extends IMarkerResolution> createFixer() {
                return newArrayList();
            }
        },
        NO_ACTIVE_INSTALLATION {

            @Override
            public boolean hasResolution() {
                return true;
            }

            @Override
            public List<? extends IMarkerResolution> createFixer() {
                return Arrays.asList(new MissingPythonInstallationFixer());
            }
        },
        INVALID_PYTHON_DIRECTORY {
            @Override
            public boolean hasResolution() {
                return true;
            }

            @Override
            public List<? extends IMarkerResolution> createFixer() {
                return Arrays.asList(new MissingPythonInstallationFixer());
            }
        },
        MISSING_ROBOT {
            @Override
            public boolean hasResolution() {
                return true;
            }

            @Override
            public List<? extends IMarkerResolution> createFixer() {
                return Arrays.asList(new InstallRobotUsingPipFixer(), new MissingPythonInstallationFixer());
            }
        };

        public abstract boolean hasResolution();

        public abstract List<? extends IMarkerResolution> createFixer();
    }


}
