package org.robotframework.ide.core.execution.context;

import java.util.LinkedHashMap;
import java.util.Map;

public class RobotKeywordExecutionContext {

    private String name;

    private Map<RobotKeywordExecutionContext, ExecutionStatus> keywordMap;

    public RobotKeywordExecutionContext(String name) {
        this.name = name;
        keywordMap = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<RobotKeywordExecutionContext, ExecutionStatus> getKeywordMap() {
        return keywordMap;
    }

    public void setKeywordMap(Map<RobotKeywordExecutionContext, ExecutionStatus> keywordMap) {
        this.keywordMap = keywordMap;
    }

}
