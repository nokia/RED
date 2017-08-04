/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;

class VariableMappingsResolver {

    static Map<String, String> resolve(final List<VariableMapping> variableMappings) {
        final Map<String, String> variables = new HashMap<>();
        for (final VariableMapping mapping : variableMappings) {
            variables.put(VariableNamesSupport.extractUnifiedVariableName(mapping.getName()), mapping.getValue());
        }
        return variables;
    }
}
