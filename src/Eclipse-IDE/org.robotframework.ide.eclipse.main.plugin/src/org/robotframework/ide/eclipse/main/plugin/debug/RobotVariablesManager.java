package org.robotframework.ide.eclipse.main.plugin.debug;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;

/**
 * @author mmarzec
 */
public class RobotVariablesManager {

    private RobotDebugTarget target;

    private Map<String, IVariable> oldVariablesMap;

    public RobotVariablesManager(RobotDebugTarget target) {
        this.target = target;
    }

    public IVariable[] extractRobotDebugVariables(Map<String, String> newVariables) {

        Map<String, IVariable> currentVariablesMap = new LinkedHashMap<>();
        if (oldVariablesMap == null) {
            for (String newVarName : newVariables.keySet()) {
                currentVariablesMap.put(newVarName,
                        new RobotDebugVariable(target, newVarName, newVariables.get(newVarName)));
            }
        } else {
            for (String newVarName : newVariables.keySet()) {
                RobotDebugVariable variable = new RobotDebugVariable(target, newVarName, newVariables.get(newVarName));
                if (oldVariablesMap.containsKey(newVarName)) {
                    try {
                        String variableOldValue = ((RobotDebugVariable) oldVariablesMap.get(newVarName)).getValue()
                                .getValueString();
                        String variableNewValue = newVariables.get(newVarName);
                        if (!variableOldValue.equals(variableNewValue)) {
                            variable.setHasValueChanged(true);
                        }
                    } catch (DebugException e) {
                        e.printStackTrace();
                    }
                }
                currentVariablesMap.put(newVarName, variable);
            }
        }

        oldVariablesMap = currentVariablesMap;
        return currentVariablesMap.values().toArray(new IVariable[currentVariablesMap.size()]);
    }
}
