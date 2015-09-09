/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.util.LinkedHashMap;
import java.util.Map;

public class RobotSuiteExecutionContext {

    private String longname;

    private Map<RobotTestCaseExecutionContext, ExecutionStatus> testCaseMap;

    public RobotSuiteExecutionContext(String longname) {
        this.longname = longname;
        testCaseMap = new LinkedHashMap<>();
    }

    public String getLongname() {
        return longname;
    }

    public void setLongname(String longname) {
        this.longname = longname;
    }

    public Map<RobotTestCaseExecutionContext, ExecutionStatus> getTestCaseMap() {
        return testCaseMap;
    }

    public void setTestCaseMap(Map<RobotTestCaseExecutionContext, ExecutionStatus> testCaseMap) {
        this.testCaseMap = testCaseMap;
    }

}
