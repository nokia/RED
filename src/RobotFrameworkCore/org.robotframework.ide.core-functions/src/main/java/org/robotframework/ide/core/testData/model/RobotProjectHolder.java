/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


    public RobotRuntimeEnvironment getRobotRuntime() {
        return robotRuntime;
    }


    public List<ARobotInternalVariable<?>> getGlobalVariables() {

        return globalVariables;
    }


    @VisibleForTesting
    protected void initGlobalVariables() {
        Map<?, ?> variables = robotRuntime.getGlobalVariables();
        globalVariables.addAll(map(variables));
    }


    @SuppressWarnings("rawtypes")
    private List<ARobotInternalVariable<?>> map(final Map<?, ?> varsRead) {
        List<ARobotInternalVariable<?>> variables = new LinkedList<>();
        Set<?> variablesNames = varsRead.keySet();
        for (Object varName : variablesNames) {
            Object varValue = varsRead.get(varName);
            ARobotInternalVariable<?> var;
            if (varValue instanceof List) {
                ListRobotInternalVariable listVar = new ListRobotInternalVariable(
                        "" + varName);
                listVar.setValue((List) varValue);
                var = listVar;
            } else if (varValue instanceof Map) {
                DictionaryRobotInternalVariable dictVar = new DictionaryRobotInternalVariable(
                        "" + varName);
                dictVar.setValue(convert((Map) varValue));
                var = dictVar;
            } else {
                ScalarRobotInternalVariable scalarVar = new ScalarRobotInternalVariable(
                        "" + varName);
                scalarVar.setValue("" + varValue);
                var = scalarVar;
            }

            variables.add(var);
        }

        return variables;
    }


    private Map<String, Object> convert(@SuppressWarnings("rawtypes") Map m) {
        Map<String, Object> map = new LinkedHashMap<>();
        @SuppressWarnings("rawtypes")
        Set keySet = m.keySet();
        for (Object key : keySet) {
            map.put("" + key, m.get(key));
        }

        return map;
    }


    public void addModelFile(final RobotFileOutput robotOutput) {
        if (robotOutput != null) {
            File processedFile = robotOutput.getProcessedFile();
            if (processedFile != null) {
                RobotFileOutput file = findFileByName(processedFile);
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
        for (ResourceImportReference ref : referenced) {
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
        RobotFileOutput foundFile = findFileByName(file);
        return (foundFile == null)
                || (file.lastModified() != foundFile
                        .getLastModificationEpochTime());
    }


    public List<RobotFileOutput> findFilesWithImportedVariableFile(
            final File variableFile) {
        List<RobotFileOutput> found = new LinkedList<>();
        List<Integer> foundFiles = findFile(new SearchByVariablesImport(
                variableFile));
        for (Integer fileId : foundFiles) {
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
        public boolean matchCriteria(RobotFileOutput robotFile) {
            boolean matchResult = false;
            List<VariablesFileImportReference> varImports = robotFile
                    .getVariablesImportReferences();
            for (VariablesFileImportReference variablesFileImportReference : varImports) {
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
        List<Integer> findFile = findFile(new SearchByName(file));
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
        public boolean matchCriteria(RobotFileOutput robotFile) {
            return (robotFile.getProcessedFile().getAbsolutePath()
                    .equals(toFound.getAbsolutePath()));
        }
    }


    protected List<Integer> findFile(final ISearchCriteria criteria) {
        List<Integer> foundFiles = new LinkedList<>();
        int size = readableProjectFiles.size();
        for (int i = 0; i < size; i++) {
            RobotFileOutput robotFile = readableProjectFiles.get(i);
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
