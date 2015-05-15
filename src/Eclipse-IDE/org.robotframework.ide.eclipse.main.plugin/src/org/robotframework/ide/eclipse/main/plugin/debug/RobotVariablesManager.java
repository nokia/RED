package org.robotframework.ide.eclipse.main.plugin.debug;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;

/**
 * @author mmarzec
 */
public class RobotVariablesManager {

    public static final String GLOBAL_VARIABLE_NAME = "Global Variables";

    private RobotDebugTarget target;

    private List<RobotVariablesContext> previousVariables;

    private Map<String, String> globalVariables;

    public RobotVariablesManager(RobotDebugTarget target) {
        this.target = target;
        previousVariables = new ArrayList<>();
    }

    public IVariable[] extractRobotDebugVariables(int stackTraceId, Map<String, String> newVariables) {
        Map<String, IVariable> previousVariablesMap = null;
        RobotVariablesContext currentVariablesContext = null;
        for (RobotVariablesContext variablesContext : previousVariables) {
            if (variablesContext.getStackTraceId() == stackTraceId) {
                previousVariablesMap = variablesContext.getVariablesMap();
                currentVariablesContext = variablesContext;
                break;
            }
        }

        Map<String, IVariable> currentVariablesMap = new LinkedHashMap<>();
        if (previousVariablesMap == null) {
            currentVariablesMap.put(GLOBAL_VARIABLE_NAME, createGlobalVariable(createNestedGlobalVariables()));
            for (String newVarName : newVariables.keySet()) {
                if (!globalVariables.containsKey(newVarName)) {
                    currentVariablesMap.put(newVarName,
                            new RobotDebugVariable(target, newVarName, newVariables.get(newVarName)));
                }
            }
        } else {
            Map<String, IVariable> nonGlobalVariablesMap = new LinkedHashMap<>();
            IVariable[] nestedGlobalVariables = new IVariable[globalVariables.size()];
            int nestedIndex = 0;

            for (String newVarName : newVariables.keySet()) {
                if (!globalVariables.containsKey(newVarName)) {
                    RobotDebugVariable variable = new RobotDebugVariable(target, newVarName,
                            newVariables.get(newVarName));
                    if (previousVariablesMap.containsKey(newVarName)) {
                        try {
                            String variableOldValue = ((RobotDebugVariable) previousVariablesMap.get(newVarName)).getValue()
                                    .getValueString();
                            String variableNewValue = newVariables.get(newVarName);
                            if (!variableOldValue.equals(variableNewValue)) {
                                variable.setHasValueChanged(true);
                            }
                        } catch (DebugException e) {
                            e.printStackTrace();
                        }
                    }
                    nonGlobalVariablesMap.put(newVarName, variable);
                } else {
                    nestedGlobalVariables[nestedIndex] = new RobotDebugVariable(target, newVarName,
                            newVariables.get(newVarName));
                    nestedIndex++;
                }
            }

            RobotDebugVariable globalVariable = createGlobalVariable(nestedGlobalVariables);
            currentVariablesMap.put(GLOBAL_VARIABLE_NAME, globalVariable);

            currentVariablesMap.putAll(nonGlobalVariablesMap);
        }

        if (currentVariablesContext != null) {
            currentVariablesContext.setVariablesMap(currentVariablesMap);
        } else {
            previousVariables.add(new RobotVariablesContext(stackTraceId, currentVariablesMap));
        }

        return currentVariablesMap.values().toArray(new IVariable[currentVariablesMap.size()]);
    }

    public Map<String, String> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(Map<String, String> globalVariables) {
        this.globalVariables = globalVariables;
    }

    private RobotDebugVariable createGlobalVariable(IVariable[] nestedVariables) {
        RobotDebugVariable variable = new RobotDebugVariable(target, GLOBAL_VARIABLE_NAME, "");
        RobotDebugValue value = new RobotDebugValue(target, "");
        value.setNestedVariables(nestedVariables);
        ;
        variable.setNewRobotDebugValue(value);

        return variable;
    }

    private IVariable[] createNestedGlobalVariables() {
        IVariable[] nested = new IVariable[globalVariables.size()];
        Set<String> set = globalVariables.keySet();
        int i = 0;
        for (String key : set) {
            nested[i] = new RobotDebugVariable(target, key, globalVariables.get(key));
            i++;
        }
        return nested;
    }
}
