package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;

/**
 * This interface should be used together with
 * {@link RobotProblem#causedBy(IProblemCause)} static method.
 * 
 * @{author Michal Anglart
 */
public interface IProblemCause {

    /**
     * Returns problem severity. This severity will be used when creating marker
     * for problem.
     * 
     * @return Severity of problem. One of: Error, Warning, Info
     */
    Severity getSeverity();

    /**
     * Return information whether this particular problem have one or more
     * possible resolutions. If true is returned the {@link #createFixers()}
     * method shall return non-empty list of IMarkerResolutions objects
     * supporting QuickFix mechanism.
     * 
     * @return True if there are possible resolutions for QuickFix mechanism.
     */
    boolean hasResolution();

    /**
     * Returns list of objects able to resolve this particular problem. There
     * can be possibly many different fixes applied to the problem.
     * 
     * @return
     */
    List<? extends IMarkerResolution> createFixers();
    
    /**
     * Returns human-readable problem description. Returned string can use will
     * be filled with object array passed by
     * {@link RobotProblem#formatMessageWith(Object...)} method call, so they
     * can use formatted string as in {@link String#format(String, Object...)}
     * method.
     * 
     * @return Human readable string with problem description. Can use
     *         formatting syntax inside.
     */
    String getProblemDescription();

    /**
     * This interface should be implemented by enums and this method should
     * return the class name.
     * 
     * @return Class name of enum implementing this interface.
     */
    String getEnumClassName();

    public static enum Severity {
        ERROR(IMarker.SEVERITY_ERROR),
        WARNING(IMarker.SEVERITY_WARNING),
        INFO(IMarker.SEVERITY_INFO);

        private final int severity;

        private Severity(final int severity) {
            this.severity = severity;
        }

        public int getLevel() {
            return severity;
        }
    }
}
