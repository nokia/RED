/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.util.LinkedHashMap;
import java.util.Map;

public class RobotTestCaseExecutionContext {

    private String longname;

    private Map<RobotKeywordExecutionContext, ExecutionStatus> keywordMap;

    public RobotTestCaseExecutionContext(String longname) {
        this.longname = longname;
        keywordMap = new LinkedHashMap<>();
    }

    public String getLongname() {
        return longname;
    }

    public void setLongname(String longname) {
        this.longname = longname;
    }

    public Map<RobotKeywordExecutionContext, ExecutionStatus> getKeywordMap() {
        return keywordMap;
    }

    public void setKeywordMap(Map<RobotKeywordExecutionContext, ExecutionStatus> keywordMap) {
        this.keywordMap = keywordMap;
    }

}
