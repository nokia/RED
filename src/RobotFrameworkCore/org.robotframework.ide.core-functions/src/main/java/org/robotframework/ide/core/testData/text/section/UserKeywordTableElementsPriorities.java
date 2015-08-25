package org.robotframework.ide.core.testData.text.section;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.model.ModelType;


public class UserKeywordTableElementsPriorities implements
        IModelElementsPriority {

    private static final Map<ModelType, Integer> PRIORITIES = new LinkedHashMap<>();
    static {
        int startPrio = 0;
        PRIORITIES.put(ModelType.KEYWORDS_TABLE_HEADER, ++startPrio);
        PRIORITIES.put(ModelType.USER_KEYWORD, ++startPrio);
        PRIORITIES.put(ModelType.USER_KEYWORD_ARGUMENTS, ++startPrio);
        PRIORITIES.put(ModelType.USER_KEYWORD_DOCUMENTATION, ++startPrio);
        PRIORITIES.put(ModelType.USER_KEYWORD_TIMEOUT, ++startPrio);
        PRIORITIES.put(ModelType.USER_KEYWORD_EXECUTABLE_ROW, ++startPrio);
        PRIORITIES.put(ModelType.USER_KEYWORD_TEARDOWN, ++startPrio);
        PRIORITIES.put(ModelType.USER_KEYWORD_RETURN, ++startPrio);
    }


    @Override
    public Map<ModelType, Integer> getElementPriorities() {
        return PRIORITIES;
    }
}
