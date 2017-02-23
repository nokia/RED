/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class RemoteRobotDebugTarget extends RobotDebugElement implements IDebugTarget {

    private final String name;

    private final IProcess process;

    private final ILaunch launch;

    private IThread[] threads;

    private boolean isSuspended;

    public RemoteRobotDebugTarget(final String name, final ILaunch launch, final IProcess process) {
        super(null);
        this.name = name;
        this.launch = launch;
        this.process = process;
    }

    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    @Override
    public String getName() throws DebugException {
        return name;
    }

    @Override
    public IProcess getProcess() {
        return process;
    }

    @Override
    public boolean hasThreads() throws DebugException {
        return true;
    }

    @Override
    public IThread[] getThreads() throws DebugException {
        return threads;
    }

    private RobotThread getThread() {
        return (RobotThread) threads[0];
    }

    @Override
    public boolean canTerminate() {
        return getProcess().canTerminate();
    }

    @Override
    public boolean isTerminated() {
        return getProcess().isTerminated();
    }

    @Override
    public void terminate() throws DebugException {
        // send terminate action to agent
        getProcess().terminate();
    }

    @Override
    public boolean canResume() {
        return !isTerminated() && isSuspended();
    }

    @Override
    public void resume() throws DebugException {
        getThread().setStepping(false);
        // sendMessageToAgent(RedToAgentMessage.RESUME_EXECUTION);
        // resumed(DebugEvent.CLIENT_REQUEST);
    }

    @Override
    public boolean canSuspend() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    @Override
    public void suspend() throws DebugException {
        // TODO Auto-generated method stub

    }

    @Override
    public void breakpointAdded(final IBreakpoint breakpoint) {
        // nothing to do
    }

    @Override
    public void breakpointRemoved(final IBreakpoint breakpoint, final IMarkerDelta delta) {
        // nothing to do
    }

    @Override
    public void breakpointChanged(final IBreakpoint breakpoint, final IMarkerDelta delta) {
        // nothing to do
    }

    @Override
    public boolean canDisconnect() {
        return false;
    }

    @Override
    public void disconnect() throws DebugException {
        throw new DebugException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, DebugException.NOT_SUPPORTED,
                "Disconnecting from running tests are not supported", null));
    }

    @Override
    public boolean isDisconnected() {
        return false;
    }

    @Override
    public boolean supportsStorageRetrieval() {
        return false;
    }

    @Override
    public IMemoryBlock getMemoryBlock(final long startAddress, final long length) throws DebugException {
        return null;
    }

    @Override
    public boolean supportsBreakpoint(final IBreakpoint breakpoint) {
        return RobotDebugElement.DEBUG_MODEL_ID.equals(breakpoint.getModelIdentifier());
    }
}
