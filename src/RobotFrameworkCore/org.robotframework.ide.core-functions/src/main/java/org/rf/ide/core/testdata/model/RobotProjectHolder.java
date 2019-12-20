/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.RobotProjectConfig;

public class RobotProjectHolder {

    private final IRuntimeEnvironment runtimeEnvironment;

    private RobotProjectConfig currentConfiguration;

    private final List<GlobalVariable<?>> globalVariables = new ArrayList<>();

    private final Map<String, String> variableMappings = new HashMap<>();

    private final List<File> modulesSearchPaths = new ArrayList<>();

    // files may be parsed in several threads
    private final Set<RobotFileOutput> parsedModelFiles = Collections.synchronizedSet(new HashSet<>());

    public RobotProjectHolder() {
        this(new NullRuntimeEnvironment());
    }

    public RobotProjectHolder(final IRuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public void configure(final RobotProjectConfig configuration, final File projectLocation) {
        if (currentConfiguration != configuration || globalVariables.isEmpty() && variableMappings.isEmpty()) {
            currentConfiguration = configuration;
            setGlobalVariables(map(runtimeEnvironment.getGlobalVariables()));
            setVariableMappings(
                    VariableMappingsResolver.resolve(currentConfiguration.getVariableMappings(), projectLocation));
            setModuleSearchPaths(runtimeEnvironment.getModuleSearchPaths());
        }
    }

    public IRuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    public void setGlobalVariables(final List<GlobalVariable<?>> variables) {
        globalVariables.clear();
        globalVariables.addAll(variables);
    }

    public List<GlobalVariable<?>> getGlobalVariables() {
        return globalVariables;
    }

    public void setVariableMappings(final Map<String, String> mappings) {
        variableMappings.clear();
        variableMappings.putAll(mappings);
    }

    public Map<String, String> getVariableMappings() {
        return variableMappings;
    }

    public void setModuleSearchPaths(final List<File> paths) {
        modulesSearchPaths.clear();
        modulesSearchPaths.addAll(paths);
    }

    public List<File> getModuleSearchPaths() {
        return modulesSearchPaths;
    }

    private List<GlobalVariable<?>> map(final Map<String, Object> varsRead) {
        final List<GlobalVariable<?>> variables = new ArrayList<>();
        for (final String varName : varsRead.keySet()) {
            final Object varValue = varsRead.get(varName);

            if (varValue instanceof List) {
                final List<?> value = (List<?>) varValue;
                variables.add(new GlobalVariable<List<?>>(varName, value));

            } else if (varValue instanceof Map) {
                final Map<String, Object> value = convert((Map<?, ?>) varValue);
                variables.add(new GlobalVariable<Map<String, ?>>(varName, value));

            } else if (varValue != null && varValue.getClass().isArray()) {
                final List<Object> value = new ArrayList<>();
                final Object[] array = ((Object[]) varValue);
                value.addAll(Arrays.asList(array));
                variables.add(new GlobalVariable<List<?>>(varName, value));

            } else {
                variables.add(new GlobalVariable<>(varName, "" + varValue));
            }
        }
        return variables;
    }

    private Map<String, Object> convert(final Map<?, ?> m) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (final Object key : m.keySet()) {
            map.put("" + key, m.get(key));
        }
        return map;
    }

    public void addParsedFile(final RobotFileOutput robotFile) {
        if (robotFile != null) {
            final File processedFile = robotFile.getProcessedFile();
            if (processedFile != null) {
                parsedModelFiles.remove(findParsedFileByPath(processedFile));
            }
            parsedModelFiles.add(robotFile);
        }
    }

    public void clearParsedFiles() {
        parsedModelFiles.clear();
    }

    public boolean shouldBeParsed(final File file) {
        final RobotFileOutput robotFile = findParsedFileByPath(file);
        return robotFile == null || file.lastModified() != robotFile.getLastModificationEpochTime();
    }

    public RobotFileOutput findParsedFileWithImportedVariableFile(final PathsProvider pathsProvider,
            final File variableFile) {
        // files may be parsed in several threads
        synchronized (parsedModelFiles) {
            for (final RobotFileOutput robotFile : parsedModelFiles) {
                if (robotFile.getVariablesImportReferences(this, pathsProvider)
                        .stream()
                        .anyMatch(r -> r.getVariablesFile().getAbsolutePath().equals(variableFile.getAbsolutePath()))) {
                    return robotFile;
                }
            }
        }
        return null;
    }

    public RobotFileOutput findParsedFileByPath(final File file) {
        // files may be parsed in several threads
        synchronized (parsedModelFiles) {
            for (final RobotFileOutput robotFile : parsedModelFiles) {
                if (robotFile.getProcessedFile() != null
                        && robotFile.getProcessedFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                    return robotFile;
                }
            }
        }
        return null;
    }
}
