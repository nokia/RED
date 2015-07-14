package org.robotframework.ide.core.testData.model.table.variables;

import org.robotframework.ide.core.testData.model.common.Text;


public class DictionaryVariable extends AVariable<Text> {

    private final DictionaryName dictionaryName;


    public DictionaryVariable(final DictionaryName dictionaryName) {
        super(VariableType.DICTIONARY);
        this.dictionaryName = dictionaryName;
    }
}
