package org.robotframework.ide.eclipse.main.plugin.debug;

import java.util.Map;

import org.eclipse.debug.core.model.IVariable;

/**
 * @author mmarzec
 *
 */
public class RobotVariablesContext {

    private Map<String, IVariable> variablesMap;

    private int stackTraceId;

    public RobotVariablesContext(int stackTraceId, Map<String, IVariable> variablesMap) {
        this.variablesMap = variablesMap;
        this.stackTraceId = stackTraceId;
    }

    public Map<String, IVariable> getVariablesMap() {
        return variablesMap;
    }

    public void setVariablesMap(Map<String, IVariable> variablesMap) {
        this.variablesMap = variablesMap;
    }

    public int getStackTraceId() {
        return stackTraceId;
    }

    public void setStackTraceId(int stackTraceId) {
        this.stackTraceId = stackTraceId;
    }

}
