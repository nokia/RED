package org.robotframework.ide.core.testData.text.section;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.model.ModelType;


public class TestCaseTableElementsPriorities implements
        IModelElementsPriority {

    private static final Map<ModelType, Integer> PRIORITIES = new LinkedHashMap<>();
    static {
        int startPrio = 0;
        PRIORITIES.put(ModelType.TEST_CASE_TABLE_HEADER, ++startPrio);
        PRIORITIES.put(ModelType.TEST_CASE, ++startPrio);
        PRIORITIES.put(ModelType.TEST_CASE_DOCUMENTATION, ++startPrio);
        PRIORITIES.put(ModelType.TEST_CASE_SETUP, ++startPrio);
        PRIORITIES.put(ModelType.TEST_CASE_TEMPLATE, ++startPrio);
        PRIORITIES.put(ModelType.TEST_CASE_TIMEOUT, ++startPrio);
        PRIORITIES.put(ModelType.TEST_CASE_EXECUTABLE_ROW, ++startPrio);
        PRIORITIES.put(ModelType.TEST_CASE_TEARDOWN, ++startPrio);
    }


    @Override
    public Map<ModelType, Integer> getElementPriorities() {
        return PRIORITIES;
    }
}
