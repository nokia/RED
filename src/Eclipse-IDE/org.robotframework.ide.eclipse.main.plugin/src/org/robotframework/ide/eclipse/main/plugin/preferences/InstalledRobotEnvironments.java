/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class InstalledRobotEnvironments {

    // active environment is cached, since its retrieval can take a little bit
    private static RobotRuntimeEnvironment active = null;

    private static Map<InterpreterWithLocation, Supplier<RobotRuntimeEnvironment>> all = null;
    static {
        InstanceScope.INSTANCE.getNode(RedPlugin.PLUGIN_ID).addPreferenceChangeListener(event -> {
            if (event == null) {
                return;
            } else if (RedPreferences.ACTIVE_RUNTIME.equals(event.getKey())) {
                active = createRuntimeEnvironment((String) event.getNewValue(),
                        RedPlugin.getDefault().getPreferences().getActiveRuntimeExec());
            } else if (RedPreferences.ACTIVE_RUNTIME_EXEC.equals(event.getKey())
                    && newHashSet(event.getOldValue(), event.getNewValue())
                            .equals(newHashSet(SuiteExecutor.IronPython.name(), SuiteExecutor.IronPython64.name()))) {
                active = createRuntimeEnvironment(RedPlugin.getDefault().getPreferences().getActiveRuntime(),
                        (String) event.getNewValue());
            } else if (RedPreferences.OTHER_RUNTIMES.equals(event.getKey())) {
                all = createRuntimeEnvironments((String) event.getNewValue(),
                        RedPlugin.getDefault().getPreferences().getAllRuntimesExecs());
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

        final InterpreterWithLocation key = InterpreterWithLocation.create(file, executor);
        return all.containsKey(key) ? all.get(key).get() : null;
    }

    public static List<RobotRuntimeEnvironment> getAllRobotInstallation(final RedPreferences preferences) {
        if (all == null) {
            all = readAllFromPreferences(preferences);
        }
        return all.values().stream().map(Supplier::get).collect(Collectors.toList());
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

        final List<String> paths = Splitter.on(';').splitToList(allPaths);
        final List<String> execs = allExecs.isEmpty() ? new ArrayList<>() : Splitter.on(';').splitToList(allExecs);

        for (int i = 0; i < paths.size(); i++) {
            final String path = paths.get(i);
            final File location = new File(path);

            final String exec = execs.size() == 0 ? "" : execs.get(i);
            final SuiteExecutor executor = exec.isEmpty() ? null : SuiteExecutor.fromName(exec);

            final InterpreterWithLocation key = InterpreterWithLocation.create(location, executor);
            final Supplier<RobotRuntimeEnvironment> envSupplier = environmentSupplier(path,
                    key.executor == null ? "" : key.executor.name());
            envs.put(key, envSupplier);
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

        private InterpreterWithLocation(final File location, final SuiteExecutor executor) {
            this.location = location;
            this.executor = executor;
        }

        static InterpreterWithLocation create(final File file, final SuiteExecutor executor) {
            if (executor == null) {
                final List<PythonInstallationDirectory> installations = RobotRuntimeEnvironment
                        .possibleInstallationsFor(file);
                if (installations.isEmpty()) {
                    return new InterpreterWithLocation(file, null);
                } else {
                    return new InterpreterWithLocation(file, installations.get(0).getInterpreter());
                }
            } else {
                return new InterpreterWithLocation(file, executor);
            }
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
