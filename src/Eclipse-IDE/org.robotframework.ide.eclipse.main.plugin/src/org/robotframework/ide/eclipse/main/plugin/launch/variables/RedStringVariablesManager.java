/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.variables;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class RedStringVariablesManager {

    private final IStringVariableManager manager;

    private final RedPreferences preferences;

    public RedStringVariablesManager() {
        this.manager = VariablesPlugin.getDefault().getStringVariableManager();
        this.preferences = RedPlugin.getDefault().getPreferences();
    }

    public String substitute(final String expression) throws CoreException {
        return manager.performStringSubstitution(expression);
    }

    public String substituteUsingQuickValuesSet(final String expression) throws CoreException {
        final Map<String, String> activeVars = getActiveOverriddenVars();
        final Map<String, String> activeVarsOldValues = getCurrentValues(activeVars.keySet());
        try {
            setValues(activeVars);
            return substitute(expression);
        } finally {
            setValues(activeVarsOldValues);
        }
    }

    private Map<String, String> getActiveOverriddenVars() {
        final Optional<String> active = preferences.getActiveVariablesSet();
        if (active.isPresent()) {
            return preferences.getOverriddenVariablesSets()
                    .get(active.get())
                    .stream()
                    .collect(toMap(v -> v.getName(), v -> v.getValue()));
        } else {
            return new HashMap<>();
        }
    }

    private Map<String, String> getCurrentValues(final Set<String> activeVars) {
        return activeVars.stream().collect(toMap(var -> var, var -> manager.getValueVariable(var).getValue()));
    }

    private void setValues(final Map<String, String> activeVars) {
        for (final Entry<String, String> var : activeVars.entrySet()) {
            manager.getValueVariable(var.getKey()).setValue(var.getValue());
        }
    }

    public Map<String, String> getOverridableVariables() {
        return overridableVariablesStream(manager.getValueVariables())
                .collect(toMap(IValueVariable::getName, IValueVariable::getValue, (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                }, LinkedHashMap::new));
    }

    private static Stream<IValueVariable> overridableVariablesStream(final IValueVariable[] variables) {
        return Stream.of(variables).filter(var -> !var.isContributed()).filter(var -> !var.isReadOnly());
    }
}
