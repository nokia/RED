/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static com.google.common.collect.Maps.newHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.rf.ide.core.testdata.imported.ARobotInternalVariable;
import org.rf.ide.core.testdata.imported.DictionaryRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ListRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ScalarRobotInternalVariable;
import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;

import com.google.common.annotations.VisibleForTesting;

public class RobotProjectHolder {

    private final RobotRuntimeEnvironment robotRuntime;

    private RobotProjectConfig currentConfiguration;

    private final List<RobotFileOutput> readableProjectFiles = new ArrayList<>();

    private final List<ARobotInternalVariable<?>> globalVariables = new ArrayList<>();

    private Map<String, String> variableMappings = new HashMap<>();

    private List<File> modulesSearchPath;

    @VisibleForTesting
    public RobotProjectHolder() {
        this.robotRuntime = null;
    }

    public RobotProjectHolder(final RobotRuntimeEnvironment robotRuntime) {
        this.robotRuntime = robotRuntime;
    }

    public void configure(final RobotProjectConfig configuration, final File projectLocation) {
        if (this.currentConfiguration == configuration) {
            return;
        }
        this.currentConfiguration = configuration;
        initGlobalVariables();
        initVariableMappings(projectLocation);
    }

    @VisibleForTesting
    protected void initGlobalVariables() {
        final Map<String, Object> variables = robotRuntime == null ? new HashMap<String, Object>()
                : robotRuntime.getGlobalVariables();
        globalVariables.addAll(map(variables));
    }

    private void initVariableMappings(final File projectLocation) {
        final Map<String, String> knownVariables = newHashMap();
        knownVariables.put("${/}", File.separator);
        knownVariables.put("${curdir}", ".");
        knownVariables.put("${space}", " ");
        if (projectLocation != null) {
            knownVariables.put("${execdir}", projectLocation.getAbsolutePath());
            knownVariables.put("${outputdir}", projectLocation.getAbsolutePath());
        }
        if (currentConfiguration != null) {
            for (final VariableMapping mapping : currentConfiguration.getVariableMappings()) {
                knownVariables.put(VariableNamesSupport.extractUnifiedVariableName(mapping.getName()),
                        mapping.getValue());
            }
        }
        this.variableMappings = knownVariables;
    }

    public RobotRuntimeEnvironment getRobotRuntime() {
        return robotRuntime;
    }

    public List<ARobotInternalVariable<?>> getGlobalVariables() {
        return globalVariables;
    }

    public Map<String, String> getVariableMappings() {
        return variableMappings;
    }

    public void setModuleSearchPaths(final List<File> paths) {
        this.modulesSearchPath = paths;
    }

    public List<File> getModuleSearchPaths() {
        if (modulesSearchPath == null) {
            modulesSearchPath = robotRuntime.getModuleSearchPaths();
        }
        return modulesSearchPath;
    }

    private List<ARobotInternalVariable<?>> map(final Map<String, Object> varsRead) {
        final List<ARobotInternalVariable<?>> variables = new ArrayList<>();
        for (final String varName : varsRead.keySet()) {
            final Object varValue = varsRead.get(varName);

            if (varValue instanceof List) {
                final List<?> value = (List<?>) varValue;
                variables.add(new ListRobotInternalVariable(varName, value));
            } else if (varValue instanceof Map) {
                final Map<String, Object> value = convert((Map<?, ?>) varValue);
                variables.add(new DictionaryRobotInternalVariable(varName, value));
            } else if (varValue != null && varValue.getClass().isArray()) {
                final List<Object> value = new ArrayList<>();
                final Object[] array = ((Object[]) varValue);
                value.addAll(Arrays.asList(array));
                variables.add(new ListRobotInternalVariable(varName, value));
            } else {
                variables.add(new ScalarRobotInternalVariable(varName, "" + varValue));
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

    public void addModelFile(final RobotFileOutput robotOutput) {
        if (robotOutput != null) {
            final File processedFile = robotOutput.getProcessedFile();
            if (processedFile != null) {
                final RobotFileOutput file = findFileByName(processedFile);
                removeModelFile(file);
            }

            readableProjectFiles.add(robotOutput);
        }
    }

    public void clearModelFiles() {
        readableProjectFiles.clear();
    }

    public void removeModelFile(final RobotFileOutput robotOutput) {
        readableProjectFiles.remove(robotOutput);
    }

    public void addImportedResources(final List<ResourceImportReference> referenced) {
        for (final ResourceImportReference ref : referenced) {
            addImportedResource(ref);
        }
    }

    public void addImportedResource(final ResourceImportReference referenced) {
        readableProjectFiles.add(referenced.getReference());
    }

    public boolean shouldBeLoaded(final RobotFileOutput robotOutput) {
        return (robotOutput != null && shouldBeLoaded(robotOutput.getProcessedFile()));
    }

    public boolean shouldBeLoaded(final File file) {
        final RobotFileOutput foundFile = findFileByName(file);
        return (foundFile == null) || (file.lastModified() != foundFile.getLastModificationEpochTime());
    }

    public List<RobotFileOutput> findFilesWithImportedVariableFile(final PathsProvider pathsProvider,
            final File variableFile) {
        final List<RobotFileOutput> found = new ArrayList<>();
        final List<Integer> foundFiles = findFile(new SearchByVariablesImport(pathsProvider, variableFile));
        for (final Integer fileId : foundFiles) {
            found.add(readableProjectFiles.get(fileId));
        }

        return found;
    }

    private class SearchByVariablesImport implements ISearchCriteria {

        private final File toFound;

        private final PathsProvider pathsProvider;

        public SearchByVariablesImport(final PathsProvider pathsProvider, final File toFound) {
            this.pathsProvider = pathsProvider;
            this.toFound = toFound;
        }

        @Override
        public boolean matchCriteria(final RobotFileOutput robotFile) {
            boolean matchResult = false;
            if (robotFile != null) {
                final List<VariablesFileImportReference> varImports = robotFile
                        .getVariablesImportReferences(RobotProjectHolder.this, pathsProvider);
                for (final VariablesFileImportReference variablesFileImportReference : varImports) {
                    if (variablesFileImportReference.getVariablesFile()
                            .getAbsolutePath()
                            .equals(toFound.getAbsolutePath())) {
                        matchResult = true;
                        break;
                    }
                }
            }

            return matchResult;
        }

    }

    public RobotFileOutput findFileByName(final File file) {
        RobotFileOutput found = null;
        final List<Integer> findFile = findFile(new SearchByName(file));
        if (!findFile.isEmpty()) {
            found = readableProjectFiles.get(findFile.get(0));
        }

        return found;
    }

    private class SearchByName implements ISearchCriteria {

        private final File toFound;

        public SearchByName(final File toFound) {
            this.toFound = toFound;
        }

        @Override
        public boolean matchCriteria(final RobotFileOutput robotFile) {
            boolean result = false;
            if (robotFile != null && robotFile.getProcessedFile() != null) {
                result = robotFile.getProcessedFile().getAbsolutePath().equals(toFound.getAbsolutePath());
            }

            return result;
        }
    }

    protected List<Integer> findFile(final ISearchCriteria criteria) {
        final List<Integer> foundFiles = new ArrayList<>();
        final int size = readableProjectFiles.size();
        for (int i = 0; i < size; i++) {
            final RobotFileOutput robotFile = readableProjectFiles.get(i);
            if (criteria.matchCriteria(robotFile)) {
                foundFiles.add(i);
                break;
            }
        }

        return foundFiles;
    }

    protected interface ISearchCriteria {

        boolean matchCriteria(final RobotFileOutput robotFile);
    }
}
