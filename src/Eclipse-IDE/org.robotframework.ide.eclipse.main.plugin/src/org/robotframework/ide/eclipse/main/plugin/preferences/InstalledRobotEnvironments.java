/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.InvalidPythonRuntimeEnvironment;
import org.rf.ide.core.environment.MissingRobotRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

public class InstalledRobotEnvironments {

    // active environment is cached, since its retrieval can take a little bit
    private static IRuntimeEnvironment active = null;

    private static Map<InterpreterWithPath, Supplier<IRuntimeEnvironment>> all = null;

    static {
        InstanceScope.INSTANCE.getNode(RedPlugin.PLUGIN_ID).addPreferenceChangeListener(event -> {
            if (event == null) {
                return;
            } else if (RedPreferences.ACTIVE_INSTALLATION.equals(event.getKey())) {
                active = initializeActiveEnvironment((String) event.getNewValue());
            } else if (RedPreferences.ALL_INSTALLATIONS.equals(event.getKey())) {
                all = initializeAllEnvironments((String) event.getNewValue());
            }
        });
    }

    public static IRuntimeEnvironment getActiveInstallation(final RedPreferences preferences) {
        if (active == null) {
            active = initializeActiveEnvironment(preferences.getActiveInstallation());
        }
        return active;
    }

    public static IRuntimeEnvironment getInstallation(final RedPreferences preferences,
            final InterpreterWithPath installation) {
        if (all == null) {
            all = initializeAllEnvironments(preferences.getAllInstallations());
        }
        return all.getOrDefault(installation, NullRuntimeEnvironment::new).get();
    }

    public static List<IRuntimeEnvironment> getAllInstallations(final RedPreferences preferences) {
        if (all == null) {
            all = initializeAllEnvironments(preferences.getAllInstallations());
        }
        return all.values().stream().map(Supplier::get).collect(Collectors.toList());
    }

    private static IRuntimeEnvironment initializeActiveEnvironment(final String jsonMapping) {
        final InterpreterWithPath installation = readInstallation(jsonMapping);
        return createEnvironment(installation, PythonInstallationDirectoryFinder::findPossibleInstallationsFor);
    }

    private static Map<InterpreterWithPath, Supplier<IRuntimeEnvironment>> initializeAllEnvironments(
            final String jsonMapping) {
        final List<InterpreterWithPath> installations = readInstallations(jsonMapping);
        final Map<InterpreterWithPath, Supplier<IRuntimeEnvironment>> environments = createEnvironments(installations,
                PythonInstallationDirectoryFinder::findPossibleInstallationsFor);
        return Collections.synchronizedMap(environments);
    }

    @VisibleForTesting
    static IRuntimeEnvironment createEnvironment(final InterpreterWithPath installation,
            final Function<String, List<PythonInstallationDirectory>> pythonInstallationFinder) {
        if (installation.path == null) {
            return new NullRuntimeEnvironment();
        }

        final List<PythonInstallationDirectory> pythonInstallations = pythonInstallationFinder.apply(installation.path);
        final Optional<PythonInstallationDirectory> pythonInstallation = installation.interpreter == null
                ? pythonInstallations.stream().findFirst()
                : pythonInstallations.stream()
                        .filter(location -> location.getInterpreter() == installation.interpreter)
                        .findFirst();

        final Optional<IRuntimeEnvironment> pythonEnvironment = pythonInstallation.map(location -> {
            return location.getRobotVersion()
                    .map(version -> new RobotRuntimeEnvironment(location, version))
                    .orElseGet(() -> new MissingRobotRuntimeEnvironment(location));
        });
        return pythonEnvironment.orElseGet(() -> new InvalidPythonRuntimeEnvironment(new File(installation.path)));
    }

    @VisibleForTesting
    static Map<InterpreterWithPath, Supplier<IRuntimeEnvironment>> createEnvironments(
            final List<InterpreterWithPath> installations,
            final Function<String, List<PythonInstallationDirectory>> pythonInstallationFinder) {
        final Map<InterpreterWithPath, Supplier<IRuntimeEnvironment>> envs = new LinkedHashMap<>();
        for (final InterpreterWithPath installation : installations) {
            envs.put(installation, new Supplier<IRuntimeEnvironment>() {

                private IRuntimeEnvironment environment;

                @Override
                public IRuntimeEnvironment get() {
                    if (environment == null) {
                        environment = createEnvironment(installation, pythonInstallationFinder);
                    }
                    return environment;
                }
            });
        }
        return envs;
    }

    public static String writeInstallation(final InterpreterWithPath installation) {
        try {
            return new ObjectMapper().writeValueAsString(installation);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static InterpreterWithPath readInstallation(final String jsonMapping) {
        try {
            final TypeReference<InterpreterWithPath> valueType = new TypeReference<InterpreterWithPath>() {
            };
            return new ObjectMapper().readValue(jsonMapping, valueType);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String writeInstallations(final List<InterpreterWithPath> installations) {
        try {
            return new ObjectMapper().writeValueAsString(installations);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<InterpreterWithPath> readInstallations(final String jsonMapping) {
        try {
            final TypeReference<List<InterpreterWithPath>> valueType = new TypeReference<List<InterpreterWithPath>>() {
            };
            return new ObjectMapper().readValue(jsonMapping, valueType);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class InterpreterWithPath {

        private final SuiteExecutor interpreter;

        private final String path;

        public InterpreterWithPath() {
            this(null, null);
        }

        public InterpreterWithPath(final SuiteExecutor interpreter, final String path) {
            this.interpreter = interpreter;
            this.path = path;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == InterpreterWithPath.class) {
                final InterpreterWithPath that = (InterpreterWithPath) obj;
                return this.interpreter == that.interpreter && Objects.equals(this.path, that.path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(interpreter, path);
        }
    }

}
