package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * @author mmarzec
 *
 */
public class RobotDebugElement extends PlatformObject implements IDebugElement {

    protected RobotDebugTarget target;

    public static final String DEBUG_MODEL_ID = "org.eclipse.debug.robot";

    public RobotDebugElement(RobotDebugTarget t) {
        target = t;
    }

    @Override
    public String getModelIdentifier() {
        return DEBUG_MODEL_ID;
    }

    @Override
    public IDebugTarget getDebugTarget() {
        return target;
    }

    @Override
    public ILaunch getLaunch() {
        return getDebugTarget().getLaunch();
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IDebugElement.class) {
            return this;
        }
        return super.getAdapter(adapter);
    }

    protected void abort(String message, Throwable e) throws DebugException {
        throw new DebugException(new Status(IStatus.ERROR, "org.robotframework.ide.eclipse.main.plugin",
                "abort message"));
    }

    /**
     * Fires a debug event
     * 
     * @param event
     *            the event to be fired
     */
    protected void fireEvent(DebugEvent event) {
        DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
    }

    /**
     * Fires a <code>CREATE</code> event for this element.
     */
    protected void fireCreationEvent() {
        fireEvent(new DebugEvent(this, DebugEvent.CREATE));
    }

    /**
     * Fires a <code>RESUME</code> event for this element with
     * the given detail.
     * 
     * @param detail
     *            event detail code
     */
    public void fireResumeEvent(int detail) {
        fireEvent(new DebugEvent(this, DebugEvent.RESUME, detail));
    }

    /**
     * Fires a <code>SUSPEND</code> event for this element with
     * the given detail.
     * 
     * @param detail
     *            event detail code
     */
    public void fireSuspendEvent(int detail) {
        fireEvent(new DebugEvent(this, DebugEvent.SUSPEND, detail));
    }

    /**
     * Fires a <code>TERMINATE</code> event for this element.
     */
    protected void fireTerminateEvent() {
        fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
    }
}
