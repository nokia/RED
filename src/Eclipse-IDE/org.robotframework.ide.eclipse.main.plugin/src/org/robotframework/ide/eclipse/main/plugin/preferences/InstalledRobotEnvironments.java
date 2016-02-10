/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

import com.google.common.base.Strings;

public class InstalledRobotEnvironments {

    // active environment is cached, since its retrieval can take a little bit
    private static RobotRuntimeEnvironment active = null;
    private static Map<File, RobotRuntimeEnvironment> all = null;
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
        final String allRuntimes = preferences.getAllRuntimes();
        final ArrayList<String> paths = newArrayList(allRuntimes.split(";"));
        for (final String path : paths) {
            if (new File(path).equals(file)) {
                if (all != null && all.get(file) != null) {
                    return all.get(file);
                } else {
                    final RobotRuntimeEnvironment env = createRuntimeEnvironment(file.getAbsolutePath());
                    if (env != null) {
                        if (all == null) {
                            all = new ConcurrentHashMap<>();
                        }
                        all.put(file, env);
                        return env;
                    }
                }
            }
        }
        return null;
    }

    public static List<RobotRuntimeEnvironment> getAllRobotInstallation(final RedPreferences preferences) {
        if (all == null) {
            all = readAllFromPreferences(preferences);
        }
        return newArrayList(all.values());
    }

    private static RobotRuntimeEnvironment readActiveFromPreferences(final RedPreferences preferences) {
        return createRuntimeEnvironment(preferences.getActiveRuntime());
    }
    
    private static Map<File, RobotRuntimeEnvironment> readAllFromPreferences(final RedPreferences preferences) {
        return createRuntimeEnvironments(preferences.getAllRuntimes());
    }

    private static RobotRuntimeEnvironment createRuntimeEnvironment(final String path) {
        return Strings.isNullOrEmpty(path) ? null : RobotRuntimeEnvironment.create(path);
    }

    private static Map<File, RobotRuntimeEnvironment> createRuntimeEnvironments(final String allPaths) {
        if (Strings.isNullOrEmpty(allPaths)) {
            return new ConcurrentHashMap<>();
        }
        final Map<File, RobotRuntimeEnvironment> envs = new ConcurrentHashMap<>();
        for (final String path : allPaths.split(";")) {
            final RobotRuntimeEnvironment env = createRuntimeEnvironment(path);
            if (env != null) {
                envs.put(env.getFile(), env);
            }
        }
        return envs;
    }
}
