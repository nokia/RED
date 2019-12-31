/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.model.table.setting.VariablesImport;

public class VariablesFileImportReference {

    public static final long FILE_NOT_EXIST_EPOCH = 0;

    private final VariablesImport importDeclaration;

    private long lastModificationEpoch = FILE_NOT_EXIST_EPOCH;

    private File variablesFile;

    private List<AVariableImported<?>> variables = new ArrayList<>();

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

    public void map(final Map<String, Object> varsRead) {
        varsRead.forEach((varName, varValue) -> {
            if (varValue instanceof List) {
                final List<?> value = (List<?>) varValue;
                variables.add(new ListVariableImported(varName, value));

            } else if (varValue instanceof Map) {
                final Map<String, Object> value = new LinkedHashMap<>();
                ((Map<?, ?>) varValue).forEach((k, v) -> value.put("" + k, v));
                variables.add(new DictionaryVariableImported(varName, value));

            } else if (varValue != null && varValue.getClass().isArray()) {
                final List<Object> value = new ArrayList<>();
                value.addAll(Arrays.asList((Object[]) varValue));
                variables.add(new ListVariableImported(varName, value));

            } else {
                variables.add(new ScalarVariableImported(varName, "" + varValue));
            }
        });
    }

    public VariablesFileImportReference copy(final VariablesImport importDeclaration) {
        final VariablesFileImportReference newVarImportRef = new VariablesFileImportReference(importDeclaration);
        newVarImportRef.setVariablesFile(variablesFile.getAbsoluteFile());
        newVarImportRef.variables = variables;

        return newVarImportRef;
    }
}
