package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;

public class RobotDebugValueManager {

    public RobotDebugValue createRobotDebugValue(Object value, RobotDebugVariable variable, RobotDebugTarget target) {
        if (value instanceof List<?>) {
            variable.setValueModificationEnabled(false);
            return createNewList((List<Object>) value, variable, target);
        } else if (value instanceof Map<?, ?>) {
            variable.setValueModificationEnabled(false);
            return createNewDictionary((Map<Object, Object>) value, variable, target);
        } else {
            return createNewTextValue(value.toString(), target);
        }
    }

    private RobotDebugValue createNewList(List<Object> list, RobotDebugVariable variable, RobotDebugTarget target) {
        String rootValue = createListRootValue(list);
        IVariable[] nestedVariables = new IVariable[list.size()];
        for (int i = 0; i < list.size(); i++) {
            nestedVariables[i] = new RobotDebugVariable(target, "[" + i + "]", list.get(i), variable);
        }
        return new RobotDebugValue(target, rootValue, nestedVariables);
    }

    private RobotDebugValue createNewDictionary(Map<Object, Object> map, RobotDebugVariable variable,
            RobotDebugTarget target) {
        String rootValue = createDictionaryRootValue(map);
        IVariable[] nestedVariables = new IVariable[map.size()];
        Set<Object> keySet = map.keySet();
        int i = 0;
        for (Object key : keySet) {
            nestedVariables[i] = new RobotDebugVariable(target, key.toString(), map.get(key), variable);
            i++;
        }
        return new RobotDebugValue(target, rootValue, nestedVariables);
    }

    private RobotDebugValue createNewTextValue(String text, RobotDebugTarget target) {
        return new RobotDebugValue(target, text, new IVariable[0]);
    }

    private String createListRootValue(List<?> value) {
        return "List[" + value.size() + "]";
    }

    private String createDictionaryRootValue(Map<?, ?> value) {
        return "Dictionary[" + value.size() + "]";
    }
    
    public String extractValueDetail(IVariable[] variables) throws DebugException {
        StringBuilder detailBuilder = new StringBuilder();
        extractNestedVariablesDetails(variables, detailBuilder);
        return detailBuilder.toString();
    }
    
    public void extractNestedVariablesDetails(IVariable[] variables, StringBuilder detailBuilder) throws DebugException {
        detailBuilder.append("[");
        for (int i = 0; i < variables.length; i++) {
            if(variables[i].getValue().hasVariables()) {
                extractNestedVariablesDetails(variables[i].getValue().getVariables(), detailBuilder);
            } else {
                detailBuilder.append(extractValue(variables[i]));
                if(i < variables.length-1) {
                    detailBuilder.append(", ");
                }
            }
        }
        detailBuilder.append("]");
    }
    
    private String extractValue(IVariable variable) throws DebugException {
        String variableName = variable.getName();
        if(isDictionaryElement(variableName)) {
            return variableName + "=" + variable.getValue().getValueString();
        }
        return variable.getValue().getValueString();
    }
    
    private boolean isDictionaryElement(String variableName) {
        return (variableName.indexOf("[") < 0 && variableName.indexOf("]") < 0);
    }
}
