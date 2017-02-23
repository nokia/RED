/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * @author mmarzec
 *
 */
public class RobotDebugElement extends PlatformObject implements IDebugElement {

    public static final String DEBUG_MODEL_ID = "org.eclipse.debug.robot";

    private final RobotDebugTarget target;

    public RobotDebugElement(final RobotDebugTarget target) {
        this.target = target;
    }

    @Override
    public String getModelIdentifier() {
        return DEBUG_MODEL_ID;
    }

    @Override
    public IDebugTarget getDebugTarget() {
        return this instanceof IDebugTarget ? (IDebugTarget) this : target;
    }

    @Override
    public ILaunch getLaunch() {
        return getDebugTarget().getLaunch();
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
        if (adapter == IDebugElement.class) {
            return this;
        } else if (adapter == ILaunch.class) {
            return getLaunch();
        }
        return super.getAdapter(adapter);
    }

    /**
     * Fires a debug event
     * 
     * @param event
     *            the event to be fired
     */
    protected void fireEvent(final DebugEvent event) {
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
    public void fireResumeEvent(final int detail) {
        fireEvent(new DebugEvent(this, DebugEvent.RESUME, detail));
    }

    /**
     * Fires a <code>SUSPEND</code> event for this element with
     * the given detail.
     * 
     * @param detail
     *            event detail code
     */
    public void fireSuspendEvent(final int detail) {
        fireEvent(new DebugEvent(this, DebugEvent.SUSPEND, detail));
    }

    /**
     * Fires a <code>TERMINATE</code> event for this element.
     */
    protected void fireTerminateEvent() {
        fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
    }
}
