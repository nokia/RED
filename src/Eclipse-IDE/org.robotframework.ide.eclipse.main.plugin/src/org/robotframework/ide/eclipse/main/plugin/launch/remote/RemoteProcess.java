/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.rf.ide.core.execution.debug.UserProcessController;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;

public class RemoteProcess implements IRobotProcess, IDisconnect {

    private Map<String, String> attributes;

    private final ILaunch launch;

    private final IStreamsProxy streamsProxy;

    private final String label;

    private UserProcessController userProcessController;
    
    private boolean isConnectedToTests = false;
    private boolean isSuspended = false;
    private boolean isTerminated = false;
    private boolean isDisconnected = false;

    private Runnable onDisconnectHook;

    public RemoteProcess(final ILaunch launch, final String label) {
        this.launch = launch;
        this.label = label;
        this.streamsProxy = new NullStreamsProxy();

        launch.addProcess(this);
        fireEvent(DebugEvent.CREATE);
    }

    @Override
    public void setPythonExecutablePath(final String pythonExecutablePath) {
        // not important for remote process
    }

    @Override
    public void setUserProcessController(final UserProcessController controller) {
        this.userProcessController = controller;
    }

    @Override
    public UserProcessController getUserProcessController() {
        return userProcessController;
    }

    @Override
    public RobotConsoleFacade provideConsoleFacade(final String consoleDescription) {
        return RobotConsoleFacade.provide(launch.getLaunchConfiguration(), consoleDescription);
    }

    @Override
    public void onTerminate(final Runnable operation) {
        this.onDisconnectHook = operation;
    }

    @Override
    public void setConnectedToTests(final boolean isConnected) {
        this.isConnectedToTests = isConnected;
    }

    @Override
    public boolean canResume() {
        return isConnectedToTests && isSuspended;
    }

    @Override
    public void resume() {
        userProcessController.resume(this::resumed);
        fireEvent(DebugEvent.CHANGE);
    }

    @Override
    public void resumed() {
        isSuspended = false;
        fireEvent(DebugEvent.CHANGE);
    }

    @Override
    public boolean canSuspend() {
        return isConnectedToTests && !isSuspended;
    }

    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    @Override
    public void suspend() {
        userProcessController.pause(this::suspended);
        fireEvent(DebugEvent.CHANGE);
    }

    @Override
    public void suspended() {
        isSuspended = true;
        fireEvent(DebugEvent.CHANGE);
    }

    @Override
    public void interrupt() {
        userProcessController.interrupt(() -> {});
        fireEvent(DebugEvent.CHANGE);
    }

    @Override
    public boolean canDisconnect() {
        return isConnectedToTests && !isDisconnected;
    }

    @Override
    public boolean isDisconnected() {
        return isDisconnected;
    }

    @Override
    public void disconnect() {
        userProcessController.disconnect(this::disconnected);
        fireEvent(DebugEvent.CHANGE);
    }

    private void disconnected() {
        isConnectedToTests = false;
        isDisconnected = true;
        isSuspended = false;

        if (onDisconnectHook != null) {
            onDisconnectHook.run();
        }
        fireEvent(DebugEvent.CHANGE);
    }

    @Override
    public boolean canTerminate() {
        return !isTerminated;
    }

    @Override
    public boolean isTerminated() {
        return isTerminated;
    }

    @Override
    public void terminate() {
        if (isConnectedToTests) {
            userProcessController.terminate(this::terminated);
            fireEvent(DebugEvent.CHANGE);
        } else {
            terminated();
        }
    }

    @Override
    public void terminated() {
        isTerminated = true;
        isConnectedToTests = false;
        isDisconnected = true;
        isSuspended = false;

        if (onDisconnectHook != null) {
            onDisconnectHook.run();
        }
        fireEvent(DebugEvent.TERMINATE);
    }

    @Override
    public String getLabel() {
        return (isSuspended() ? "<suspended>" : "") + label;
    }

    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    @Override
    public IStreamsProxy getStreamsProxy() {
        return streamsProxy;
    }

    @Override
    public void setAttribute(final String key, final String value) {
        if (attributes == null) {
            attributes = new HashMap<>(5);
        }
        final Object oldValue = attributes.get(key);
        if (!Objects.equals(oldValue, value)) {
            attributes.put(key, value);
            fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
        }
    }

    @Override
    public String getAttribute(final String key) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(key);
    }

    private void fireEvent(final int kind) {
        fireEvent(new DebugEvent(this, kind));
    }

    private void fireEvent(final DebugEvent event) {
        final DebugPlugin manager = DebugPlugin.getDefault();
        if (manager != null) {
            manager.fireDebugEventSet(new DebugEvent[] { event });
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(final Class<T> adapter) {
        if (adapter == IProcess.class) {
            return (T) this;
        }
        if (adapter == IDebugTarget.class) {
            final ILaunch launch = getLaunch();
            final IDebugTarget[] targets = launch.getDebugTargets();
            for (int i = 0; i < targets.length; i++) {
                if (this.equals(targets[i].getProcess())) {
                    return (T) targets[i];
                }
            }
            return null;
        }
        if (adapter == ILaunch.class) {
            return (T) getLaunch();
        }
        if (adapter == ILaunchConfiguration.class) {
            return (T) getLaunch().getLaunchConfiguration();
        }
        return null;
    }

    @Override
    public int getExitValue() {
        return 0;
    }

    private static class NullStreamsProxy implements IStreamsProxy, IStreamsProxy2 {

        @Override
        public IStreamMonitor getErrorStreamMonitor() {
            return null;
        }

        @Override
        public IStreamMonitor getOutputStreamMonitor() {
            return null;
        }

        @Override
        public void write(final String input) throws IOException {
            // nothing to do
        }

        @Override
        public void closeInputStream() throws IOException {
            // nothing to do
        }
    }
}
