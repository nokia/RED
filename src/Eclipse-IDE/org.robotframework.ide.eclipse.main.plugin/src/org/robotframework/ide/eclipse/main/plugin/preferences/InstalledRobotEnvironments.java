/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class InstalledRobotEnvironments {

    // active environment is cached, since its retrieval can take a little bit
    private static RobotRuntimeEnvironment active = null;

    private static Map<InterpreterWithLocation, Supplier<RobotRuntimeEnvironment>> all = null;
    static {
        InstanceScope.INSTANCE.getNode(RedPlugin.PLUGIN_ID).addPreferenceChangeListener(
                new IPreferenceChangeListener() {
                    @Override
                    public void preferenceChange(
                            final org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent event) {
                        if (event == null) {
                            return;
                        } else if (RedPreferences.ACTIVE_RUNTIME.equals(event.getKey())) {
                            active = createRuntimeEnvironment((String) event.getNewValue(),
                                    RedPlugin.getDefault().getPreferences().getActiveRuntimeExec());
                        } else if (RedPreferences.OTHER_RUNTIMES.equals(event.getKey())) {
                            all = createRuntimeEnvironments((String) event.getNewValue(),
                                    RedPlugin.getDefault().getPreferences().getAllRuntimesExecs());
                        }
                    }
                });
    }

    public static RobotRuntimeEnvironment getActiveRobotInstallation(final RedPreferences preferences) {
        if (active == null) {
            active = readActiveFromPreferences(preferences);
        }
        return active;
    }

    public static RobotRuntimeEnvironment getRobotInstallation(final RedPreferences preferences, final File file,
            final SuiteExecutor executor) {
        if (all == null) {
            all = readAllFromPreferences(preferences);
        }

        final InterpreterWithLocation key;
        if (executor == null) {
            final List<PythonInstallationDirectory> installations = RobotRuntimeEnvironment
                    .possibleInstallationsFor(file);
            if (installations.isEmpty()) {
                key = new InterpreterWithLocation(file, null);
            } else {
                key = new InterpreterWithLocation(file, installations.get(0).getInterpreter());
            }
        } else {
            key = new InterpreterWithLocation(file, executor);
        }
        return all.containsKey(key) ? all.get(key).get() : null;
    }

    public static List<RobotRuntimeEnvironment> getAllRobotInstallation(final RedPreferences preferences) {
        if (all == null) {
            all = readAllFromPreferences(preferences);
        }
        return newArrayList(transform(all.values(), Suppliers.<RobotRuntimeEnvironment> supplierFunction()));
    }

    private static RobotRuntimeEnvironment readActiveFromPreferences(final RedPreferences preferences) {
        return createRuntimeEnvironment(preferences.getActiveRuntime(), preferences.getActiveRuntimeExec());
    }
    
    private static Map<InterpreterWithLocation, Supplier<RobotRuntimeEnvironment>> readAllFromPreferences(
            final RedPreferences preferences) {
        return createRuntimeEnvironments(preferences.getAllRuntimes(), preferences.getAllRuntimesExecs());
    }

    private static RobotRuntimeEnvironment createRuntimeEnvironment(final String path, final String exec) {
        if (Strings.isNullOrEmpty(path)) {
            return null;
        } else if (exec.isEmpty()) {
            return RobotRuntimeEnvironment.create(path);
        } else {
            return RobotRuntimeEnvironment.create(path, SuiteExecutor.fromName(exec));
        }
    }

    private static Map<InterpreterWithLocation, Supplier<RobotRuntimeEnvironment>> createRuntimeEnvironments(
            final String allPaths, final String allExecs) {
        if (Strings.isNullOrEmpty(allPaths)) {
            return new ConcurrentHashMap<>();
        }
        final Map<InterpreterWithLocation, Supplier<RobotRuntimeEnvironment>> envs = Collections
                .synchronizedMap(new LinkedHashMap<InterpreterWithLocation, Supplier<RobotRuntimeEnvironment>>());

        final String[] paths = allPaths.split(";");
        final String[] execs = allExecs.isEmpty() ? new String[0] : allExecs.split(";");
        for (int i = 0; i < paths.length; i++) {
            final String path = paths[i];
            final String exec = execs.length == 0 ? "" : execs[i];

            if (exec.isEmpty()) {
                final File location = new File(path);
                final List<PythonInstallationDirectory> installations = RobotRuntimeEnvironment
                        .possibleInstallationsFor(location);
                final SuiteExecutor executor = installations.isEmpty() ? null : installations.get(0).getInterpreter();
                envs.put(new InterpreterWithLocation(location, executor), environmentSupplier(path, executor.name()));
            } else {
                final SuiteExecutor executor = SuiteExecutor.fromName(exec);
                envs.put(new InterpreterWithLocation(new File(path), executor), environmentSupplier(path, exec));
            }
        }
        return envs;
    }

    private static Supplier<RobotRuntimeEnvironment> environmentSupplier(final String path, final String exec) {
        return new Supplier<RobotRuntimeEnvironment>() {

            private RobotRuntimeEnvironment environment;

            @Override
            public RobotRuntimeEnvironment get() {
                if (environment == null) {
                    environment = createRuntimeEnvironment(path, exec);
                }
                return environment;
            }
        };
    }

    private static class InterpreterWithLocation {

        private final File location;

        private final SuiteExecutor executor;

        public InterpreterWithLocation(final File location, final SuiteExecutor executor) {
            this.location = location;
            this.executor = executor;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == InterpreterWithLocation.class) {
                final InterpreterWithLocation that = (InterpreterWithLocation) obj;
                return Objects.equals(this.location, that.location) && this.executor == that.executor;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, executor);
        }
    }
}
