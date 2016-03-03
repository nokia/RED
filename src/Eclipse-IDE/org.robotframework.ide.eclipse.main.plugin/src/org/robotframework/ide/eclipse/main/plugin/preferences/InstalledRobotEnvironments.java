/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class InstalledRobotEnvironments {

    // active environment is cached, since its retrieval can take a little bit
    private static RobotRuntimeEnvironment active = null;

    private static Map<File, EnvironmentEvaluation> all = null;
    static {
        InstanceScope.INSTANCE.getNode(RedPlugin.PLUGIN_ID).addPreferenceChangeListener(
                new IPreferenceChangeListener() {
                    @Override
                    public void preferenceChange(
                            final org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent event) {
                        if (event == null) {
                            return;
                        } else if (RedPreferences.ACTIVE_RUNTIME.equals(event.getKey())) {
                            active = createRuntimeEnvironment((String) event.getNewValue());
                        } else if (RedPreferences.OTHER_RUNTIMES.equals(event.getKey())) {
                            all = createRuntimeEnvironments((String) event.getNewValue());
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

    public static RobotRuntimeEnvironment getRobotInstallation(final RedPreferences preferences, final File file) {
        if (all == null) {
            all = readAllFromPreferences(preferences);
        }
        if (all.containsKey(file)) {
            final EnvironmentEvaluation environmentEvaluation = all.get(file);
            return toEnvs().apply(environmentEvaluation);
        } else {
            return null;
        }
    }

    public static List<RobotRuntimeEnvironment> getAllRobotInstallation(final RedPreferences preferences) {
        if (all == null) {
            all = readAllFromPreferences(preferences);
        }
        return newArrayList(transform(all.values(), toEnvs()));
    }

    private static Function<EnvironmentEvaluation, RobotRuntimeEnvironment> toEnvs() {
        return new Function<EnvironmentEvaluation, RobotRuntimeEnvironment>() {
            @Override
            public RobotRuntimeEnvironment apply(final EnvironmentEvaluation environmentEvaluation) {
                environmentEvaluation.run();
                return environmentEvaluation.environment.orNull();
            }
        };
    }

    private static RobotRuntimeEnvironment readActiveFromPreferences(final RedPreferences preferences) {
        return createRuntimeEnvironment(preferences.getActiveRuntime());
    }
    
    private static Map<File, EnvironmentEvaluation> readAllFromPreferences(
            final RedPreferences preferences) {
        return createRuntimeEnvironments(preferences.getAllRuntimes());
    }

    private static RobotRuntimeEnvironment createRuntimeEnvironment(final String path) {
        return Strings.isNullOrEmpty(path) ? null : RobotRuntimeEnvironment.create(path);
    }

    private static Map<File, EnvironmentEvaluation> createRuntimeEnvironments(final String allPaths) {
        if (Strings.isNullOrEmpty(allPaths)) {
            return new ConcurrentHashMap<>();
        }
        final Map<File, EnvironmentEvaluation> envs = new ConcurrentHashMap<>();
        for (final String path : allPaths.split(";")) {
            envs.put(new File(path), new EnvironmentEvaluation(path));
        }
        return envs;
    }

    private static class EnvironmentEvaluation implements Runnable {

        private final String path;

        Optional<RobotRuntimeEnvironment> environment = null;

        EnvironmentEvaluation(final String path) {
            this.path = path;
        }

        @Override
        public void run() {
            if (environment == null) {
                environment = Optional.fromNullable(createRuntimeEnvironment(path));
            }
        }
    }
}
