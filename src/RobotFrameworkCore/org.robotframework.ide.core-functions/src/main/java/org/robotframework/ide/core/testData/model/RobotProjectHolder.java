/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.importer.VariablesFileImportReference;
import org.robotframework.ide.core.testData.robotImported.ARobotInternalVariable;
import org.robotframework.ide.core.testData.robotImported.DictionaryRobotInternalVariable;
import org.robotframework.ide.core.testData.robotImported.ListRobotInternalVariable;
import org.robotframework.ide.core.testData.robotImported.ScalarRobotInternalVariable;

import com.google.common.annotations.VisibleForTesting;


public class RobotProjectHolder {

    private final RobotRuntimeEnvironment robotRuntime;
    private final List<RobotFileOutput> readableProjectFiles = new LinkedList<>();
    private final List<ARobotInternalVariable<?>> globalVariables = new LinkedList<>();


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


    @VisibleForTesting
    protected void initGlobalVariables() {
        final Map<String, Object> variables = robotRuntime == null ? new HashMap<String, Object>()
                : robotRuntime.getGlobalVariables();
        globalVariables.addAll(map(variables));
    }


    private List<ARobotInternalVariable<?>> map(final Map<String, Object> varsRead) {
        final List<ARobotInternalVariable<?>> variables = new LinkedList<>();
        for (final String varName : varsRead.keySet()) {
            final Object varValue = varsRead.get(varName);

            if (varValue instanceof List) {
                final List<?> value = (List<?>) varValue;
                variables.add(new ListRobotInternalVariable(varName, value));
            } else if (varValue instanceof Map) {
                final Map<String, Object> value = convert((Map<?, ?>) varValue);
                variables.add(new DictionaryRobotInternalVariable(varName, value));
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


    public void removeModelFile(final RobotFileOutput robotOutput) {
        readableProjectFiles.remove(robotOutput);
    }


    public void addImportedResources(
            final List<ResourceImportReference> referenced) {
        for (final ResourceImportReference ref : referenced) {
            addImportedResource(ref);
        }
    }


    public void addImportedResource(final ResourceImportReference referenced) {
        readableProjectFiles.add(referenced.getReference());
    }


    public boolean shouldBeLoaded(final RobotFileOutput robotOutput) {
        return (robotOutput != null && shouldBeLoaded(robotOutput
                .getProcessedFile()));
    }


    public boolean shouldBeLoaded(final File file) {
        final RobotFileOutput foundFile = findFileByName(file);
        return (foundFile == null)
                || (file.lastModified() != foundFile
                        .getLastModificationEpochTime());
    }


    public List<RobotFileOutput> findFilesWithImportedVariableFile(
            final File variableFile) {
        final List<RobotFileOutput> found = new LinkedList<>();
        final List<Integer> foundFiles = findFile(new SearchByVariablesImport(
                variableFile));
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
            final List<VariablesFileImportReference> varImports = robotFile
                    .getVariablesImportReferences();
            for (final VariablesFileImportReference variablesFileImportReference : varImports) {
                if (variablesFileImportReference.getVariablesFile()
                        .getAbsolutePath().equals(toFound.getAbsolutePath())) {
                    matchResult = true;
                    break;
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
            return (robotFile.getProcessedFile().getAbsolutePath()
                    .equals(toFound.getAbsolutePath()));
        }
    }


    protected List<Integer> findFile(final ISearchCriteria criteria) {
        final List<Integer> foundFiles = new LinkedList<>();
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
