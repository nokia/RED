package org.robotframework.ide.core.testData.importer;

import java.util.Map;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;


public class DictionaryVariableImported extends
        AVariableImported<Map<String, ?>> {

    public DictionaryVariableImported(String name) {
        super(name, VariableType.DICTIONARY);
    }
}
