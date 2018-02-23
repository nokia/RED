package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public final class RobotTask {

    public static final String TYPE_ID = RedPlugin.PLUGIN_ID + ".robotTask";

    private final Priority priority;

    private final int lineNumber;

    private final String description;

    public RobotTask(final Priority priority, final String description, final int lineNumber) {
        this.priority = priority;
        this.lineNumber = lineNumber;
        this.description = description;
    }

    void createMarker(final IFile file) {
        try {
            final IMarker marker = file.createMarker(TYPE_ID);
            marker.setAttribute(IMarker.DONE, false);
            marker.setAttribute(IMarker.USER_EDITABLE, false);
            marker.setAttribute(IMarker.PRIORITY, priority.priority);
            marker.setAttribute(IMarker.MESSAGE, description.intern());
            marker.setAttribute(IMarker.LOCATION, ("line " + lineNumber).intern());
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create marker!", e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == RobotTask.class) {
            final RobotTask that = (RobotTask) obj;
            return this.priority == that.priority && this.lineNumber == that.lineNumber
                    && this.description.equals(that.description);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, lineNumber, description);
    }

    public static enum Priority {
        LOW(IMarker.PRIORITY_LOW),
        NORMAL(IMarker.PRIORITY_NORMAL),
        HIGH(IMarker.PRIORITY_HIGH);

        private int priority;

        private Priority(final int priority) {
            this.priority = priority;
        }
    }
}