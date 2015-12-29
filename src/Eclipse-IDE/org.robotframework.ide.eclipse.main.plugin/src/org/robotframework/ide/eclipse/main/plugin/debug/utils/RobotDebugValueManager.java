/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;

/**
 * @author mmarzec
 *
 */
public class RobotDebugValueManager {
    
    private static final String DICT_VARIABLE_VALUE_NAME = "Dictionary";
    
    private static final String LIST_VARIABLE_VALUE_NAME = "List";

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
        return LIST_VARIABLE_VALUE_NAME + "[" + value.size() + "]";
    }

    private String createDictionaryRootValue(final Map<?, ?> value) {
        return DICT_VARIABLE_VALUE_NAME + "[" + value.size() + "]";
    }
    
    public static String extractValueDetail(final IValue value) {
        String detail = "";
        try {
            if (value.hasVariables()) {
                final StringBuilder detailBuilder = new StringBuilder();
                extractNestedVariablesDetails(value.getVariables(), detailBuilder, isDictionaryValue(value));
                detail = detailBuilder.toString();
            } else {
                detail = value.getValueString();
            }
        } catch (final DebugException e) {
            e.printStackTrace();
        }
        return detail;
    }

    private static void extractNestedVariablesDetails(final IVariable[] variables, final StringBuilder detailBuilder,
            final boolean isDictionary) throws DebugException {
        
        detailBuilder.append(getDetailBeginCharacter(isDictionary));
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].getValue().hasVariables()) {
                if (isDictionaryVariable(variables[i].getName())) {
                    detailBuilder.append(variables[i].getName() + "=");
                }
                extractNestedVariablesDetails(variables[i].getValue().getVariables(), detailBuilder,
                        isDictionaryValue(variables[i].getValue()));
            } else {
                detailBuilder.append(getTextValue(variables[i]));
            }
            if (i < variables.length - 1) {
                detailBuilder.append(", ");
            }
        }
        detailBuilder.append(getDetailEndCharacter(isDictionary));
    }
    
    private static String getTextValue(final IVariable variable) throws DebugException {
        final String variableName = variable.getName();
        if(isDictionaryVariable(variableName)) {
            return variableName + "=" + variable.getValue().getValueString();
        }
        return variable.getValue().getValueString();
    }
    
    private static boolean isDictionaryVariable(final String variableName) {
        return (variableName.indexOf("[") < 0 && variableName.indexOf("]") < 0);
    }

    private static boolean isDictionaryValue(final IValue value) throws DebugException {
        final String valueString = value.getValueString();
        return valueString != null ? valueString.contains(DICT_VARIABLE_VALUE_NAME) : false;
    }

    private static String getDetailBeginCharacter(final boolean isDictionary) {
        return isDictionary ? "{" : "[";
    }

    private static String getDetailEndCharacter(final boolean isDictionary) {
        return isDictionary ? "}" : "]";
    }
}
