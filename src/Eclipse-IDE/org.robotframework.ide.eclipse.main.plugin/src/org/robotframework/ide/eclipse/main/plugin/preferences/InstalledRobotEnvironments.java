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

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class InstalledRobotEnvironments {

    // active environment is cached, since its retrieval can take a little bit
    private static RobotRuntimeEnvironment active = null;
    private static List<RobotRuntimeEnvironment> all = null;
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
                return createRuntimeEnvironment(path);
            }
        }
        return null;
    }

    public static List<RobotRuntimeEnvironment> getAllRobotInstallation(final RedPreferences preferences) {
        if (all == null) {
            all = readAllFromPreferences(preferences);
        }
        return all;
    }

    private static RobotRuntimeEnvironment readActiveFromPreferences(final RedPreferences preferences) {
        return createRuntimeEnvironment(preferences.getActiveRuntime());
    }
    
    private static List<RobotRuntimeEnvironment> readAllFromPreferences(final RedPreferences preferences) {
        return createRuntimeEnvironments(preferences.getAllRuntimes());
    }

    private static RobotRuntimeEnvironment createRuntimeEnvironment(final String path) {
        return Strings.isNullOrEmpty(path) ? null : RobotRuntimeEnvironment.create(path);
    }

    private static List<RobotRuntimeEnvironment> createRuntimeEnvironments(final String allPaths) {
        if (Strings.isNullOrEmpty(allPaths)) {
            return newArrayList();
        }
        final List<String> all = newArrayList(allPaths.split(";"));
        final List<RobotRuntimeEnvironment> envs = newArrayList(Iterables.transform(all, new Function<String, RobotRuntimeEnvironment>() {
            @Override
            public RobotRuntimeEnvironment apply(final String path) {
                return createRuntimeEnvironment(path);
            }
        }));
        return newArrayList(Iterables.filter(envs, Predicates.notNull()));
    }
}
