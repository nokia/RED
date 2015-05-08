package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

/**
 * @author mmarzec
 *
 */
public class RobotLineBreakpoint extends LineBreakpoint {
    
    public static final String HIT_COUNT_ATTRIBUTE = "robot.breakpoint.hit.count";

    /**
     * Default constructor is required for the breakpoint manager
     * to re-create persisted breakpoints. After instantiating a breakpoint,
     * the <code>setMarker(...)</code> method is called to restore
     * this breakpoint's attributes.
     */
    public RobotLineBreakpoint() {
    }

    /**
     * Constructs a line breakpoint on the given resource at the given
     * line number. The line number is 1-based.
     * 
     * @param resource
     *            file on which to set the breakpoint
     * @param lineNumber
     *            1-based line number of the breakpoint
     * @throws CoreException
     *             if unable to create the breakpoint
     */
    public RobotLineBreakpoint(final IResource resource, final int lineNumber) throws CoreException {

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                IMarker marker = resource.createMarker("org.robotframework.ide.eclipse.main.plugin.robot.lineBreakpoint.marker");
                setMarker(marker);
                marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
                marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
                marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
                marker.setAttribute(IMarker.LOCATION, resource.getName());
                marker.setAttribute(IMarker.MESSAGE, "Line breakpoint: " + resource.getName() + " [line: " + lineNumber
                        + "]");
            }
        };
        run(getMarkerRule(resource), runnable);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
     */
    public String getModelIdentifier() {
        return RobotDebugElement.DEBUG_MODEL_ID;
    }
}
