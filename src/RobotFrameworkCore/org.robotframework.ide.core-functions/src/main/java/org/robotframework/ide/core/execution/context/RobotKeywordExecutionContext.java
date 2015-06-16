package org.robotframework.ide.core.execution.context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RobotKeywordExecutionContext {

    private String name;

    private String type;

    private List<String> arguments;

    private Map<RobotKeywordExecutionContext, ExecutionStatus> keywordMap;

    public RobotKeywordExecutionContext(String name, String type, List<String> arguments) {
        this.name = name;
        this.type = type;
        this.arguments = arguments;
        keywordMap = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public Map<RobotKeywordExecutionContext, ExecutionStatus> getKeywordMap() {
        return keywordMap;
    }

    public void setKeywordMap(Map<RobotKeywordExecutionContext, ExecutionStatus> keywordMap) {
        this.keywordMap = keywordMap;
    }

}
