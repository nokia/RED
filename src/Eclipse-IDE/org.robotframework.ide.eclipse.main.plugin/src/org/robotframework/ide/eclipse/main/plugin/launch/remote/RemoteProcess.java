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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;

public class RemoteProcess implements IRobotProcess {

    private Map<String, String> attributes;

    private final ILaunch launch;

    private final IStreamsProxy streamsProxy;

    private final String label;

    private boolean isTerminated;

    private final Runnable serverCloser;

    public RemoteProcess(final ILaunch launch, final Runnable serverCloser, final String label) {
        this.launch = launch;
        this.serverCloser = serverCloser;
        this.label = label;
        this.streamsProxy = new NullStreamsProxy();
        this.isTerminated = false;

        launch.addProcess(this);
        fireEvent(new DebugEvent(this, DebugEvent.CREATE));
    }

    @Override
    public RobotConsoleFacade provideConsoleFacade(final String consoleDescription) {
        return RobotConsoleFacade.provide(launch.getLaunchConfiguration(), consoleDescription);
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
        serverCloser.run();
        terminated();
    }

    void terminated() {
        if (!isTerminated) {
            isTerminated = true;

            fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
        }
    }

    @Override
    public String getLabel() {
        return label;
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
    public int getExitValue() throws DebugException {
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
