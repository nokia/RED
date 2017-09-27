/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Sets.newHashSet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    public static StackFrameVariables newNonLocalVariables(final Map<Variable, VariableTypedValue> variables) {
        final LinkedHashMap<String, StackFrameVariable> vars = new LinkedHashMap<>();

        for (final Entry<Variable, VariableTypedValue> entry : variables.entrySet()) {
            final String varName = entry.getKey().getName();
            final VariableScope scope = entry.getKey().getScope();
            final String varType = entry.getValue().getType();
            final Object val = entry.getValue().getValue();

            vars.put(varName, new StackFrameVariable(scope, isAutomatic(varName), varName, varType, val));
        }
        return new StackFrameVariables(vars);
    }

    public static StackFrameVariables newLocalVariables(final StackFrameVariables parentVars,
            final boolean preserveLocals) {
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

        for (final Variable removedVariable : delta.removedVariables) {
            variables.remove(removedVariable.getName());
        }
        // nothing to do for unchanged variables
        for (final Variable changedVariable : delta.changedVariables) {
            final StackFrameVariable var = variables.get(changedVariable.getName());

            final String name = changedVariable.getName();
            final VariableScope scope = changedVariable.getScope();
            final boolean isAutomatic = var.isAutomatic();
            final String type = vars.get(changedVariable).getType();
            final Object value = vars.get(changedVariable).getValue();

            variables.put(name, new StackFrameVariable(scope, isAutomatic, name, type, value));
        }
        for (final Variable addedVariable : delta.addedVariables) {
            final String name = addedVariable.getName();
            final VariableScope scope = addedVariable.getScope();
            final boolean isAutomatic = isAutomatic(name);
            final String type = vars.get(addedVariable).getType();
            final Object value = vars.get(addedVariable).getValue();

            variables.put(name, new StackFrameVariable(scope, isAutomatic, name, type, value));
        }
        return delta;
    }

    private StackVariablesDelta computeDelta(final Map<Variable, VariableTypedValue> vars) {
        final StackVariablesDelta delta = new StackVariablesDelta();

        for (final Variable incomingVariable : vars.keySet()) {
            if (!variables.containsKey(incomingVariable.getName())) {
                delta.addedVariables.add(incomingVariable);
            } else {
                // has same scope, type and value
                if (Objects.equal(incomingVariable.getScope(), variables.get(incomingVariable.getName()).getScope()) &&
                        Objects.equal(vars.get(incomingVariable).getType(), variables.get(incomingVariable.getName()).getType()) &&
                        Objects.equal(vars.get(incomingVariable).getValue(), variables.get(incomingVariable.getName()).getValue())) {
                    delta.unchangedVariables.add(incomingVariable);
                } else {
                    delta.changedVariables.add(incomingVariable);
                }
            }
        }
        for (final String existingVar : variables.keySet()) {
            if (!vars.containsKey(new Variable(existingVar))) {
                delta.removedVariables.add(new Variable(existingVar, variables.get(existingVar).getScope()));
            }
        }
        return delta;
    }

    private static boolean isAutomatic(final String varName) {
        return GLOBAL_AUTOMATIC_VARIABLES.contains(varName.toLowerCase())
                || SUITE_AUTOMATIC_VARIABLES.contains(varName.toLowerCase())
                || TEST_AUTOMATIC_VARIABLES.contains(varName.toLowerCase())
                || LOCAL_AUTOMATIC_VARIABLES.contains(varName.toLowerCase());
    }

    public static class StackVariablesDelta {

        private final Set<Variable> changedVariables = new HashSet<>();

        private final Set<Variable> unchangedVariables = new HashSet<>();

        private final Set<Variable> addedVariables = new LinkedHashSet<>(); // order has to be preserved

        private final Set<Variable> removedVariables = new HashSet<>();

        public boolean isChanged(final String variableName) {
            return changedVariables.contains(new Variable(variableName));
        }

        public boolean isUnchanged(final String variableName) {
            return unchangedVariables.contains(new Variable(variableName));
        }

        public boolean isAdded(final String variableName) {
            return addedVariables.contains(new Variable(variableName));
        }

        public boolean isRemoved(final String variableName) {
            return removedVariables.contains(new Variable(variableName));
        }
    }
}
