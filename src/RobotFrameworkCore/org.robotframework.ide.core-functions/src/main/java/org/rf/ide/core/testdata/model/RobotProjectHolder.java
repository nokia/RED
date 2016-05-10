/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.imported.ARobotInternalVariable;
import org.rf.ide.core.testdata.imported.DictionaryRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ListRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ScalarRobotInternalVariable;
import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;

import com.google.common.annotations.VisibleForTesting;

public class RobotProjectHolder {

    private final RobotRuntimeEnvironment robotRuntime;

    private final List<RobotFileOutput> readableProjectFiles = new ArrayList<>();

    private final List<ARobotInternalVariable<?>> globalVariables = new ArrayList<>();

    private Map<String, String> variableMappings = new HashMap<>();

    public RobotProjectHolder(final RobotRuntimeEnvironment robotRuntime) {
        this.robotRuntime = robotRuntime;
        initGlobalVariables();
    }

    /**
     * Design for test.
     */
    @VisibleForTesting
    public RobotProjectHolder() {
        this.robotRuntime = null;
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

    public void setVariableMappings(final Map<String, String> variableMappings) {
        this.variableMappings = variableMappings;
    }

    @VisibleForTesting
    protected void initGlobalVariables() {
        final Map<String, Object> variables = robotRuntime == null ? new LinkedHashMap<String, Object>()
                : robotRuntime.getGlobalVariables();
        globalVariables.addAll(map(variables));
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

    public List<RobotFileOutput> findFilesWithImportedVariableFile(final File variableFile) {
        final List<RobotFileOutput> found = new ArrayList<>();
        final List<Integer> foundFiles = findFile(new SearchByVariablesImport(variableFile));
        for (final Integer fileId : foundFiles) {
            found.add(readableProjectFiles.get(fileId));
        }

        return found;
    }

    private class SearchByVariablesImport implements ISearchCriteria {

        private final File toFound;

        public SearchByVariablesImport(final File toFound) {
            this.toFound = toFound;
        }

        @Override
        public boolean matchCriteria(final RobotFileOutput robotFile) {
            boolean matchResult = false;
            if (robotFile != null) {
                final List<VariablesFileImportReference> varImports = robotFile.getVariablesImportReferences();
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
