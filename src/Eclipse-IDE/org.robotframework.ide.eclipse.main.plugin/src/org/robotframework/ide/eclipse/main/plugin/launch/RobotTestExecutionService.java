/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.ui.services.IDisposable;

/**
 * This service should be obtained by calling RedPlugin.getTestExecutionService()
 */
public class RobotTestExecutionService {

    private static final int LAUNCHES_HISTORY_LIMIT = 10;

    private final Deque<RobotTestsLaunch> launches = new ArrayDeque<>();

    private final List<RobotTestExecutionListener> executionListeners = new ArrayList<>();


    public void addExecutionListener(final RobotTestExecutionListener listener) {
        executionListeners.add(listener);
    }

    public void removeExecutionListner(final RobotTestExecutionListener listener) {
        executionListeners.remove(listener);
    }

    public synchronized Optional<RobotTestsLaunch> getLastLaunch() {
        return launches.stream().findFirst();
    }

    public synchronized RobotTestsLaunch testExecutionStarting() {
        final RobotTestsLaunch newLaunch = new RobotTestsLaunch();
        launches.push(newLaunch);

        if (launches.size() > LAUNCHES_HISTORY_LIMIT) {
            final RobotTestsLaunch launch = launches.removeLast();
            launch.dispose();
        }

        for (final RobotTestExecutionListener listener : executionListeners) {
            listener.executionStarting(newLaunch);
        }
        return newLaunch;
    }

    @FunctionalInterface
    public static interface RobotTestExecutionListener {

        void executionStarting(RobotTestsLaunch launch);
    }

    public static class RobotTestsLaunch {

        private final Map<Class<?>, Object> executionData = new HashMap<>();

        public synchronized <T extends IDisposable> T getExecutionData(final Class<? extends T> clazz,
                final Supplier<T> supplyWhenAbsent) {
            final Object data = executionData.computeIfAbsent(clazz, c -> supplyWhenAbsent.get());
            return clazz.cast(data);
        }

        public synchronized <T extends IDisposable> Optional<T> getExecutionData(final Class<? extends T> clazz) {
            return Optional.ofNullable(clazz.cast(executionData.get(clazz)));
        }

        private synchronized void dispose() {
            for (final Object data : executionData.values()) {
                IDisposable.class.cast(data).dispose();
            }
        }
    }
}
