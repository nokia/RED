package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

import com.google.common.collect.Range;

public class RobotProblem {

    public static final String TYPE_ID = RobotFramework.PLUGIN_ID + ".robotProblem";

    public static final String CAUSE_ENUM_CLASS = "class";
    public static final String CAUSE_ATTRIBUTE = "cause";

    private final IProblemCause cause;
    private Object[] objects;

    private RobotProblem(final IProblemCause cause) {
        this.cause = cause;
        this.objects = null;
    }

    public static RobotProblem causedBy(final IProblemCause cause) {
        return new RobotProblem(cause);
    }

    public RobotProblem formatMessageWith(final Object... objects) {
        this.objects = objects;
        return this;
    }

    IMarker createMarker(final IFile file, final int lineNumber) {
        return createMarker(file, lineNumber, null);
    }

    IMarker createMarker(final IFile file, final int lineNumber, final Range<Integer> charRange) {
        try {
            final IMarker marker = file.createMarker(TYPE_ID);
            marker.setAttribute(IMarker.MESSAGE, getMessage());
            marker.setAttribute(IMarker.SEVERITY, cause.getSeverity().getLevel());
            if (lineNumber >= 0) {
                marker.setAttribute(IMarker.LOCATION, "line " + lineNumber);
                marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            } else {
                marker.setAttribute(IMarker.LOCATION, "unknown line");
            }
            if (charRange != null && charRange.hasLowerBound() && charRange.hasUpperBound()) {
                marker.setAttribute(IMarker.CHAR_START, charRange.lowerEndpoint());
                marker.setAttribute(IMarker.CHAR_END, charRange.upperEndpoint());
            }

            marker.setAttribute(CAUSE_ENUM_CLASS, cause.getEnumClassName());
            marker.setAttribute(CAUSE_ATTRIBUTE, cause.toString());
            return marker;
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create marker!", e);
        }
    }

    public void createMarker(final IFile file, final int lineNumber, final Range<Integer> charRange,
            final Map<String, Object> additionalAttributes) {
        try {
            final IMarker marker = file.createMarker(TYPE_ID);
            marker.setAttribute(IMarker.MESSAGE, getMessage());
            marker.setAttribute(IMarker.SEVERITY, cause.getSeverity().getLevel());
            if (lineNumber >= 0) {
                marker.setAttribute(IMarker.LOCATION, "line " + lineNumber);
                marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            } else {
                marker.setAttribute(IMarker.LOCATION, "unknown line");
            }
            if (charRange != null && charRange.hasLowerBound() && charRange.hasUpperBound()) {
                marker.setAttribute(IMarker.CHAR_START, charRange.lowerEndpoint());
                marker.setAttribute(IMarker.CHAR_END, charRange.upperEndpoint());
            }

            marker.setAttribute(CAUSE_ENUM_CLASS, cause.getEnumClassName());
            marker.setAttribute(CAUSE_ATTRIBUTE, cause.toString());
            for (final Entry<String, Object> entry : additionalAttributes.entrySet()) {
                marker.setAttribute(entry.getKey(), entry.getValue());
            }
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create marker!", e);
        }
    }

    private String getMessage() {
        return String.format(cause.getProblemDescription(), objects == null ? new Object[0] : objects);
    }
}
