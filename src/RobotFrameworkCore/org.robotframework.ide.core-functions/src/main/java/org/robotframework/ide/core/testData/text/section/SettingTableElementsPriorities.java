package org.robotframework.ide.core.testData.text.section;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.model.ModelType;


public class SettingTableElementsPriorities implements
        IModelElementsPriority {

    private static final Map<ModelType, Integer> PRIORITIES = new LinkedHashMap<>();
    static {
        int startPriority = 0;
        PRIORITIES.put(ModelType.SETTINGS_TABLE_HEADER, ++startPriority);
        PRIORITIES.put(ModelType.SUITE_DOCUMENTATION, ++startPriority);
        PRIORITIES.put(ModelType.SUITE_SETUP, ++startPriority);
        PRIORITIES.put(ModelType.SUITE_TEARDOWN, ++startPriority);
        PRIORITIES.put(ModelType.SUITE_TEST_SETUP, ++startPriority);
        PRIORITIES.put(ModelType.SUITE_TEST_TEARDOWN, ++startPriority);
        PRIORITIES.put(ModelType.FORCE_TAGS_SETTING, ++startPriority);
        PRIORITIES.put(ModelType.DEFAULT_TAGS_SETTING, ++startPriority);
        PRIORITIES.put(ModelType.SUITE_TEST_TEMPLATE, ++startPriority);
        PRIORITIES.put(ModelType.SUITE_TEST_TIMEOUT, ++startPriority);
        PRIORITIES.put(ModelType.METADATA_SETTING, ++startPriority);
        startPriority++;
        PRIORITIES.put(ModelType.LIBRARY_IMPORT_SETTING, startPriority);
        PRIORITIES.put(ModelType.VARIABLES_IMPORT_SETTING, startPriority);
        PRIORITIES.put(ModelType.RESOURCE_IMPORT_SETTING, startPriority);
    }


    @Override
    public Map<ModelType, Integer> getElementPriorities() {
        return PRIORITIES;
    }
}
