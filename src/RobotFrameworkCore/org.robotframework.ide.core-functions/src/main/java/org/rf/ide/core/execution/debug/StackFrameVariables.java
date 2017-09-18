/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Sets.newHashSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

public class StackFrameVariables implements Iterable<StackFrameVariable> {

    // hardcoded variable names; has to be checked after release of new RF

    private static final Set<String> GLOBAL_AUTOMATIC_VARIABLES = newHashSet("${/}", "${:}", "${\\n}", "${curdir}",
            "${tempdir}", "${execdir}", "${none}", "${null}", "${true}", "${false}", "${space}", "${empty}", "@{empty}",
            "&{empty}", "${prev_test_name}", "${prev_test_message}", "${prev_test_status}", "${suite_name}",
            "${suite_source}", "${log_level}", "${output_file}", "${log_file}", "${report_file}", "${debug_file}",
            "${output_dir}");

    private static final Set<String> SUITE_AUTOMATIC_VARIABLES = newHashSet("${suite_name}", "${suite_documentation}",
            "${suite_source}", "&{suite_metadata}", "${suite_status}", "${suite_message}");

    private static final Set<String> TEST_AUTOMATIC_VARIABLES = newHashSet("${test_name}", "@{test_tags}",
            "${test_documentation}", "${test_status}", "${test_message}");

    private static final Set<String> LOCAL_AUTOMATIC_VARIABLES = newHashSet("${keyword_status}", "${keyword_message}");

    private final Map<String, StackFrameVariable> variables;

    static StackFrameVariables newGlobalVariables(final Map<Variable, VariableTypedValue> variables) {
        // all globals are automatic variables at the very beginning of execution (no user globals exist yet)
        return newVariables(variables);
    }

    static StackFrameVariables newSuiteVariables(final Map<Variable, VariableTypedValue> variables) {
        return newVariables(variables);
    }

    static StackFrameVariables newTestVariables(final Map<Variable, VariableTypedValue> variables) {
        return newVariables(variables);
    }

    private static StackFrameVariables newVariables(final Map<Variable, VariableTypedValue> variables) {
        final LinkedHashMap<String, StackFrameVariable> vars = new LinkedHashMap<>();

        for (final Entry<Variable, VariableTypedValue> entry : variables.entrySet()) {
            final String varName = entry.getKey().getName();
            final VariableScope scope = entry.getKey().getScope();
            final String varType = entry.getValue().getType();
            final Object val = entry.getValue().getValue();

            vars.put(varName, new StackFrameVariable(scope, isAutomatic(scope, varName), varName, varType, val));
        }
        return new StackFrameVariables(vars);
    }

    static StackFrameVariables newLocalVariables(final StackFrameVariables parentVars, final boolean preserveLocals) {
        final LinkedHashMap<String, StackFrameVariable> variables = new LinkedHashMap<>();
        for (final StackFrameVariable variable : parentVars) {
            if (preserveLocals || variable.getScope() != VariableScope.LOCAL) {
                final StackFrameVariable copiedVariable = variable.copy();
                variables.put(copiedVariable.getName(), copiedVariable);
            }
        }
        return new StackFrameVariables(variables);
    }

    @VisibleForTesting
    StackFrameVariables(final Map<String, StackFrameVariable> variables) {
        this.variables = variables;
    }

    @Override
    public Iterator<StackFrameVariable> iterator() {
        return variables.values().iterator();
    }

    public Map<String, StackFrameVariable> getVariables() {
        return variables;
    }

    StackVariablesDelta update(final Map<Variable, VariableTypedValue> vars) {
        final StackVariablesDelta delta = computeDelta(vars);

        for (final Variable removedVariable : delta.removedVariables.keySet()) {
            delta.removedVariables.put(removedVariable, variables.get(removedVariable.getName()));
            variables.remove(removedVariable.getName());
        }
        for (final Variable unchangedVariable : delta.unchangedVariables.keySet()) {
            delta.unchangedVariables.put(unchangedVariable, variables.get(unchangedVariable.getName()));
        }
        for (final Variable changedVariable : delta.changedVariables.keySet()) {
            final StackFrameVariable var = variables.get(changedVariable.getName());
            delta.changedVariables.put(changedVariable, var);
            var.setScope(changedVariable.getScope());
            var.setType(vars.get(changedVariable).getType());
            var.setValue(vars.get(changedVariable).getValue());
        }
        for (final Variable addedVariable : delta.addedVariables.keySet()) {
            final String type = vars.get(addedVariable).getType();
            final Object value = vars.get(addedVariable).getValue();
            final VariableScope scope = addedVariable.getScope();
            variables.put(addedVariable.getName(), new StackFrameVariable(scope,
                    isAutomatic(scope, addedVariable.getName()), addedVariable.getName(), type, value));
            delta.addedVariables.put(addedVariable, variables.get(addedVariable.getName()));
        }
        return delta;
    }

    private StackVariablesDelta computeDelta(final Map<Variable, VariableTypedValue> vars) {
        final StackVariablesDelta delta = new StackVariablesDelta();

        for (final Variable incomingVariable : vars.keySet()) {
            if (!variables.containsKey(incomingVariable.getName())) {
                delta.addedVariables.put(incomingVariable, null);
            } else {
                // has same scope, type and value
                if (Objects.equal(incomingVariable.getScope(), variables.get(incomingVariable.getName()).getScope()) &&
                        Objects.equal(vars.get(incomingVariable).getType(), variables.get(incomingVariable.getName()).getType()) &&
                        Objects.equal(vars.get(incomingVariable).getValue(), variables.get(incomingVariable.getName()).getValue())) {
                    delta.unchangedVariables.put(incomingVariable, null);
                } else {
                    delta.changedVariables.put(incomingVariable, null);
                }
            }
        }
        for (final String existingVar : variables.keySet()) {
            if (!vars.containsKey(new Variable(existingVar))) {
                delta.removedVariables.put(new Variable(existingVar, variables.get(existingVar).getScope()), null);
            }
        }
        return delta;
    }

    private static boolean isAutomatic(final VariableScope scope, final String varName) {
        if (scope == VariableScope.GLOBAL) {
            return GLOBAL_AUTOMATIC_VARIABLES.contains(varName.toLowerCase());
        } else if (scope == VariableScope.TEST_SUITE) {
            return SUITE_AUTOMATIC_VARIABLES.contains(varName.toLowerCase());
        } else if (scope == VariableScope.TEST_CASE) {
            return TEST_AUTOMATIC_VARIABLES.contains(varName.toLowerCase());
        } else if (scope == VariableScope.LOCAL) {
            return LOCAL_AUTOMATIC_VARIABLES.contains(varName.toLowerCase());
        }
        return false;
    }

    public static class StackVariablesDelta {

        private final Map<Variable, StackFrameVariable> changedVariables = new HashMap<>();

        private final Map<Variable, StackFrameVariable> unchangedVariables = new HashMap<>();

        private final Map<Variable, StackFrameVariable> addedVariables = new LinkedHashMap<>(); // order has to be preserved

        private final Map<Variable, StackFrameVariable> removedVariables = new HashMap<>();

        public boolean isChanged(final String variableName) {
            return changedVariables.containsKey(new Variable(variableName));
        }

        public boolean isUnchanged(final String variableName) {
            return unchangedVariables.containsKey(new Variable(variableName));
        }

        public boolean isAdded(final String variableName) {
            return addedVariables.containsKey(new Variable(variableName));
        }

        public boolean isRemoved(final String variableName) {
            return removedVariables.containsKey(new Variable(variableName));
        }
    }
}
