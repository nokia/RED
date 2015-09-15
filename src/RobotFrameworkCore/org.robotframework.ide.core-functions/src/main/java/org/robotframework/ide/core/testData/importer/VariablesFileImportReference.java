/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.importer;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;


public class VariablesFileImportReference {

    public static final long FILE_NOT_EXIST_EPOCH = 0;
    private final VariablesImport importDeclaration;
    private long lastModificationEpoch = FILE_NOT_EXIST_EPOCH;
    private File variablesFile;
    private List<AVariableImported<?>> variables = new LinkedList<>();


    public VariablesFileImportReference(final VariablesImport importDeclaration) {
        this.importDeclaration = importDeclaration;
    }


    public VariablesImport getImportDeclaration() {
        return importDeclaration;
    }


    public void setVariablesFile(final File variablesFile) {
        this.variablesFile = variablesFile;
        this.lastModificationEpoch = variablesFile.lastModified();
    }


    public File getVariablesFile() {
        return variablesFile;
    }


    public List<AVariableImported<?>> getVariables() {
        return Collections.unmodifiableList(variables);
    }


    public void setLastModificationEpochTime(final long lastModificationEpoch) {
        this.lastModificationEpoch = lastModificationEpoch;
    }


    public long getLastModificationEpochTime() {
        return lastModificationEpoch;
    }


    public void map(final Map<?, ?> varsRead) {
        final Set<?> variablesNames = varsRead.keySet();
        for (final Object varName : variablesNames) {
            final Object varValue = varsRead.get(varName);
            AVariableImported<?> var;
            if (varValue instanceof List) {
                final ListVariableImported listVar = new ListVariableImported(""
                        + varName);
                listVar.setValue((List<?>) varValue);
                var = listVar;
            } else if (varValue instanceof Map) {
                final DictionaryVariableImported dictVar = new DictionaryVariableImported(
                        "" + varName);
                dictVar.setValue(convert((Map<?, ?>) varValue));
                var = dictVar;
            } else {
                final ScalarVariableImported scalarVar = new ScalarVariableImported(
                        "" + varName);
                scalarVar.setValue("" + varValue);
                var = scalarVar;
            }

            variables.add(var);
        }
    }

    private Map<String, Object> convert(final Map<?, ?> m) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (final Object key : m.keySet()) {
            map.put((String) key, m.get(key));
        }
        return map;
    }

    public VariablesFileImportReference copy(
            final VariablesImport importDeclaration) {
        final VariablesFileImportReference newVarImportRef = new VariablesFileImportReference(
                importDeclaration);
        newVarImportRef.setVariablesFile(variablesFile.getAbsoluteFile());
        newVarImportRef.variables = variables;

        return newVarImportRef;
    }
}
