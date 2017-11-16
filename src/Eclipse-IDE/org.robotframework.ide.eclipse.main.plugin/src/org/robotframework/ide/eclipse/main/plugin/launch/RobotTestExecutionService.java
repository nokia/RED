/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.services.IDisposable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * This service should be obtained by calling RedPlugin.getTestExecutionService()
 */
public class RobotTestExecutionService {

    // history size set to 1 since we do not use it as of now
    // see RED-967 (#149) and RED-817
    @VisibleForTesting
    static final int LAUNCHES_HISTORY_LIMIT = 1;

    private final Deque<RobotTestsLaunch> launches = new ArrayDeque<>();

    private final List<RobotTestExecutionListener> executionListeners = new ArrayList<>();

    @VisibleForTesting
    Collection<RobotTestsLaunch> getLaunches() {
        return ImmutableList.copyOf(launches);
    }

    public void addExecutionListener(final RobotTestExecutionListener listener) {
        executionListeners.add(listener);
    }

    public void removeExecutionListener(final RobotTestExecutionListener listener) {
        executionListeners.remove(listener);
    }

    public synchronized void forEachLaunch(final Consumer<? super RobotTestsLaunch> action) {
        launches.forEach(action);
    }

    public synchronized Optional<RobotTestsLaunch> getLastLaunch() {
        return launches.stream().findFirst();
    }

    public synchronized RobotTestsLaunch testExecutionStarting(final ILaunchConfiguration configuration) {
        final RobotTestsLaunch newLaunch = new RobotTestsLaunch(configuration);
        launches.addFirst(newLaunch);

        if (launches.size() > LAUNCHES_HISTORY_LIMIT) {
            final RobotTestsLaunch launch = launches.getLast();
            if (launch.isTerminated()) {
                launches.remove(launch);
                launch.dispose();
            }
        }

        executionListeners.forEach(listener -> listener.executionStarting(newLaunch));
        return newLaunch;
    }

    public synchronized void testExecutionEnded(final RobotTestsLaunch launch) {
        launch.setTerminated();

        executionListeners.forEach(listener -> listener.executionEnded(launch));
    }

    public static interface RobotTestExecutionListener {

        void executionStarting(RobotTestsLaunch launch);

        void executionEnded(RobotTestsLaunch launch);
    }

    public static class RobotTestsLaunch {

        private final Map<Class<?>, Object> executionData = new HashMap<>();

        private boolean isTerminated;

        private final ILaunchConfiguration configuration;

        public RobotTestsLaunch(final ILaunchConfiguration configuration) {
            this.configuration = configuration;
        }

        public ILaunchConfiguration getLaunchConfiguration() {
            return configuration;
        }

        public synchronized <T extends IDisposable> T getExecutionData(final Class<? extends T> clazz,
                final Supplier<T> supplyWhenAbsent) {
            final Object data = executionData.computeIfAbsent(clazz, c -> supplyWhenAbsent.get());
            return clazz.cast(data);
        }

        public synchronized <T extends IDisposable> Optional<T> getExecutionData(final Class<? extends T> clazz) {
            return Optional.ofNullable(clazz.cast(executionData.get(clazz)));
        }

        public synchronized <T extends IDisposable> void performOnExecutionData(final Class<? extends T> clazz,
                final Consumer<T> consumer) {
            Optional.ofNullable(clazz.cast(executionData.get(clazz))).ifPresent(consumer);
        }

        private synchronized void dispose() {
            for (final Object data : executionData.values()) {
                IDisposable.class.cast(data).dispose();
            }
            executionData.clear();
        }

        public synchronized boolean isTerminated() {
            return isTerminated;
        }

        private synchronized void setTerminated() {
            isTerminated = true;
        }
    }
}
