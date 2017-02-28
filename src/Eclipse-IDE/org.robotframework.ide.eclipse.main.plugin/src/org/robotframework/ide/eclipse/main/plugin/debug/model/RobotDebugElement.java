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

/**
 * @author mmarzec
 *
 */
public class RobotDebugElement extends PlatformObject implements IDebugElement {

    public static final String DEBUG_MODEL_ID = "org.eclipse.debug.robot";

    private final RobotDebugTarget target;

    protected RobotDebugElement(final RobotDebugTarget target) {
        this.target = target;
    }

    @Override
    public String getModelIdentifier() {
        return DEBUG_MODEL_ID;
    }

    @Override
    public RobotDebugTarget getDebugTarget() {
        return this instanceof RobotDebugTarget ? (RobotDebugTarget) this : target;
    }

    @Override
    public ILaunch getLaunch() {
        return getDebugTarget().getLaunch();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
        if (adapter == IDebugElement.class) {
            return this;
        } else if (adapter == ILaunch.class) {
            return getLaunch();
        }
        return super.getAdapter(adapter);
    }

    protected final void fireCreationEvent() {
        fireEvent(new DebugEvent(this, DebugEvent.CREATE));
    }

    protected final void fireResumeEvent(final int detail) {
        fireEvent(new DebugEvent(this, DebugEvent.RESUME, detail));
    }

    protected final void fireSuspendEvent(final int detail) {
        fireEvent(new DebugEvent(this, DebugEvent.SUSPEND, detail));
    }

    protected final void fireChangeEvent(final int detail) {
        fireEvent(new DebugEvent(this, DebugEvent.CHANGE, detail));
    }

    protected final void fireTerminateEvent() {
        fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
    }

    private void fireEvent(final DebugEvent event) {
        DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
    }
}
