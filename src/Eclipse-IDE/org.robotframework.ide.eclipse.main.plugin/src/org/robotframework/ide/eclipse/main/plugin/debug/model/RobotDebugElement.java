/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.util.function.Consumer;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;

import com.google.common.annotations.VisibleForTesting;

public class RobotDebugElement extends PlatformObject
        implements IDebugElement, ITerminate, ISuspendResume, IDisconnect {

    public static final String DEBUG_MODEL_ID = "org.eclipse.debug.robot";

    private final RobotDebugTarget target;

    private final Consumer<DebugEvent> eventsNotifier;

    protected RobotDebugElement(final RobotDebugTarget target) {
        this(target, RobotDebugElement::fireEvent);
    }

    @VisibleForTesting
    RobotDebugElement(final RobotDebugTarget target, final Consumer<DebugEvent> eventsNotifier) {
        this.target = target;
        this.eventsNotifier = eventsNotifier;
    }

    static void fireEvent(final DebugEvent event) {
        DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
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

    public void fireCreationEvent() {
        eventsNotifier.accept(new DebugEvent(this, DebugEvent.CREATE));
    }

    public void fireResumeEvent(final int detail) {
        eventsNotifier.accept(new DebugEvent(this, DebugEvent.RESUME, detail));
    }

    public void fireSuspendEvent(final int detail) {
        eventsNotifier.accept(new DebugEvent(this, DebugEvent.SUSPEND, detail));
    }

    public void fireChangeEvent(final int detail) {
        eventsNotifier.accept(new DebugEvent(this, DebugEvent.CHANGE, detail));
    }

    public void fireTerminateEvent() {
        eventsNotifier.accept(new DebugEvent(this, DebugEvent.TERMINATE));
    }

    @Override
    public boolean canResume() {
        return getDebugTarget().canResume();
    }

    @Override
    public void resume() {
        getDebugTarget().resume();
    }

    @Override
    public boolean canSuspend() {
        return getDebugTarget().canSuspend();
    }

    @Override
    public boolean isSuspended() {
        return getDebugTarget().isSuspended();
    }

    @Override
    public void suspend() {
        getDebugTarget().suspend();
    }

    @Override
    public boolean canDisconnect() {
        return getDebugTarget().canDisconnect();
    }

    @Override
    public boolean isDisconnected() {
        return getDebugTarget().isDisconnected();
    }

    @Override
    public void disconnect() {
        getDebugTarget().disconnect();
    }

    @Override
    public boolean canTerminate() {
        return getDebugTarget().canTerminate();
    }

    @Override
    public boolean isTerminated() {
        return getDebugTarget().isTerminated();
    }

    @Override
    public void terminate() throws DebugException {
        getDebugTarget().terminate();
    }
}
