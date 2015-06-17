package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;

public class RobotDebugValueManager {

    public RobotDebugValue createRobotDebugValue(final Object value, final RobotDebugVariable variable, final RobotDebugTarget target) {
        if (value instanceof List<?>) {
            variable.setValueModificationEnabled(false);
            return createNewList((List<?>) value, variable, target);
        } else if (value instanceof Map<?, ?>) {
            variable.setValueModificationEnabled(false);
            return createNewDictionary((Map<?, ?>) value, variable, target);
        } else {
            return createNewTextValue(value.toString(), target);
        }
    }

    private RobotDebugValue createNewList(final List<?> list, final RobotDebugVariable variable, final RobotDebugTarget target) {
        final String rootValue = createListRootValue(list);
        final IVariable[] nestedVariables = new IVariable[list.size()];
        for (int i = 0; i < list.size(); i++) {
            nestedVariables[i] = new RobotDebugVariable(target, "[" + i + "]", list.get(i), variable);
        }
        return new RobotDebugValue(target, rootValue, nestedVariables);
    }

    private RobotDebugValue createNewDictionary(final Map<?, ?> map, final RobotDebugVariable variable,
            final RobotDebugTarget target) {
        final String rootValue = createDictionaryRootValue(map);
        final IVariable[] nestedVariables = new IVariable[map.size()];
        int i = 0;
        for (final Object key : map.keySet()) {
            nestedVariables[i] = new RobotDebugVariable(target, key.toString(), map.get(key), variable);
            i++;
        }
        return new RobotDebugValue(target, rootValue, nestedVariables);
    }

    private RobotDebugValue createNewTextValue(final String text, final RobotDebugTarget target) {
        return new RobotDebugValue(target, text, new IVariable[0]);
    }

    private String createListRootValue(final List<?> value) {
        return "List[" + value.size() + "]";
    }

    private String createDictionaryRootValue(final Map<?, ?> value) {
        return "Dictionary[" + value.size() + "]";
    }
    
    public String extractValueDetail(final IVariable[] variables) throws DebugException {
        final StringBuilder detailBuilder = new StringBuilder();
        extractNestedVariablesDetails(variables, detailBuilder);
        return detailBuilder.toString();
    }
    
    public void extractNestedVariablesDetails(final IVariable[] variables, final StringBuilder detailBuilder) throws DebugException {
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
    
    private String extractValue(final IVariable variable) throws DebugException {
        final String variableName = variable.getName();
        if(isDictionaryElement(variableName)) {
            return variableName + "=" + variable.getValue().getValueString();
        }
        return variable.getValue().getValueString();
    }
    
    private boolean isDictionaryElement(final String variableName) {
        return (variableName.indexOf("[") < 0 && variableName.indexOf("]") < 0);
    }
}
