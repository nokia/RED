package org.robotframework.ide.core.testData.text.section;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.model.ModelType;


public class VariablesTableElementsPriorities implements
        IModelElementsPriority {

    private static final Map<ModelType, Integer> PRIORITIES = new LinkedHashMap<>();
    static {
        int startPrio = 0;
        PRIORITIES.put(ModelType.VARIABLES_TABLE_HEADER, ++startPrio);
        startPrio++;
        PRIORITIES.put(ModelType.SCALAR_VARIABLE_DECLARATION_IN_TABLE,
                startPrio);
        PRIORITIES.put(ModelType.LIST_VARIABLE_DECLARATION_IN_TABLE, startPrio);
        PRIORITIES.put(ModelType.DICTIONARY_VARIABLE_DECLARATION_IN_TABLE,
                startPrio);
        PRIORITIES.put(ModelType.UNKNOWN_VARIABLE_DECLARATION_IN_TABLE,
                startPrio);
    }


    @Override
    public Map<ModelType, Integer> getElementPriorities() {
        return PRIORITIES;
    }
}
