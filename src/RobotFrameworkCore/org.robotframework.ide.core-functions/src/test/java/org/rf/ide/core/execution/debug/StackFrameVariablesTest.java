/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrameVariables.StackVariablesDelta;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

public class StackFrameVariablesTest {

    @Test
    public void globalVariablesAreProperlyConstructed() {
        final Map<Variable, VariableTypedValue> globalVars = new HashMap<>();
        globalVars.put(new Variable("${var1}", VariableScope.GLOBAL), new VariableTypedValue("int", 1));
        globalVars.put(new Variable("${var2}", VariableScope.GLOBAL), new VariableTypedValue("string", "xyz"));
        globalVars.put(new Variable("${true}", VariableScope.GLOBAL), new VariableTypedValue("bool", true));

        final Map<String, StackFrameVariable> variables = StackFrameVariables.newNonLocalVariables(globalVars)
                .getVariables();
        assertThat(variables).containsOnlyKeys("${var1}", "${var2}", "${true}");
        assertThat(variables.get("${var1}").isAutomatic()).isFalse();
        assertThat(variables.get("${var2}").isAutomatic()).isFalse();
        assertThat(variables.get("${true}").isAutomatic()).isTrue();
    }

    @Test
    public void suiteVariablesAreProperlyConstructed() {
        final Map<Variable, VariableTypedValue> suiteVars = new HashMap<>();
        suiteVars.put(new Variable("${var}", VariableScope.GLOBAL), new VariableTypedValue("int", 1));
        suiteVars.put(new Variable("${true}", VariableScope.GLOBAL), new VariableTypedValue("bool", true));
        suiteVars.put(new Variable("${suite_var}", VariableScope.TEST_SUITE), new VariableTypedValue("string", "abc"));
        suiteVars.put(new Variable("${suite_name}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 42));

        final Map<String, StackFrameVariable> variables = StackFrameVariables.newNonLocalVariables(suiteVars)
                .getVariables();
        assertThat(variables).containsOnlyKeys("${var}", "${true}", "${suite_var}", "${suite_name}");
        assertThat(variables.get("${var}").isAutomatic()).isFalse();
        assertThat(variables.get("${true}").isAutomatic()).isTrue();
        assertThat(variables.get("${suite_var}").isAutomatic()).isFalse();
        assertThat(variables.get("${suite_name}").isAutomatic()).isTrue();
    }

    @Test
    public void testVariablesAreProperlyConstructed() {
        final Map<Variable, VariableTypedValue> testVars = new HashMap<>();
        testVars.put(new Variable("${var}", VariableScope.GLOBAL), new VariableTypedValue("int", 1));
        testVars.put(new Variable("${true}", VariableScope.GLOBAL), new VariableTypedValue("bool", true));
        testVars.put(new Variable("${suite_var}", VariableScope.TEST_SUITE), new VariableTypedValue("string", "abc"));
        testVars.put(new Variable("${suite_name}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 42));
        testVars.put(new Variable("${test_var}", VariableScope.TEST_CASE), new VariableTypedValue("unicode", "xyz"));
        testVars.put(new Variable("${test_name}", VariableScope.TEST_CASE), new VariableTypedValue("double", 4.2));

        final Map<String, StackFrameVariable> variables = StackFrameVariables.newNonLocalVariables(testVars)
                .getVariables();
        assertThat(variables).containsOnlyKeys("${var}", "${true}", "${suite_var}", "${suite_name}",
                "${test_var}", "${test_name}");
        assertThat(variables.get("${var}").isAutomatic()).isFalse();
        assertThat(variables.get("${true}").isAutomatic()).isTrue();
        assertThat(variables.get("${suite_var}").isAutomatic()).isFalse();
        assertThat(variables.get("${suite_name}").isAutomatic()).isTrue();
        assertThat(variables.get("${test_var}").isAutomatic()).isFalse();
        assertThat(variables.get("${test_name}").isAutomatic()).isTrue();
    }

    @Test
    public void localVariablesAreProperlyConstructed_whenPreservingLocals() {
        final Map<Variable, VariableTypedValue> testVars = new HashMap<>();
        testVars.put(new Variable("${var}", VariableScope.GLOBAL), new VariableTypedValue("int", 1));
        testVars.put(new Variable("${true}", VariableScope.GLOBAL), new VariableTypedValue("bool", true));
        testVars.put(new Variable("${suite_var}", VariableScope.TEST_SUITE), new VariableTypedValue("string", "abc"));
        testVars.put(new Variable("${suite_name}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 42));
        testVars.put(new Variable("${test_var}", VariableScope.TEST_CASE), new VariableTypedValue("unicode", "xyz"));
        testVars.put(new Variable("${test_name}", VariableScope.TEST_CASE), new VariableTypedValue("double", 4.2));
        testVars.put(new Variable("${local_var}", VariableScope.LOCAL), new VariableTypedValue("int", 55));
        testVars.put(new Variable("${keyword_status}", VariableScope.LOCAL), new VariableTypedValue("bool", false));

        final StackFrameVariables parentVars = StackFrameVariables.newNonLocalVariables(testVars);

        final Map<String, StackFrameVariable> variables = StackFrameVariables.newLocalVariables(parentVars, true)
                .getVariables();
        assertThat(variables).containsOnlyKeys("${var}", "${true}", "${suite_var}", "${suite_name}",
                "${test_var}", "${test_name}", "${local_var}", "${keyword_status}");
        assertThat(variables.get("${var}").isAutomatic()).isFalse();
        assertThat(variables.get("${true}").isAutomatic()).isTrue();
        assertThat(variables.get("${suite_var}").isAutomatic()).isFalse();
        assertThat(variables.get("${suite_name}").isAutomatic()).isTrue();
        assertThat(variables.get("${test_var}").isAutomatic()).isFalse();
        assertThat(variables.get("${test_name}").isAutomatic()).isTrue();
        assertThat(variables.get("${local_var}").isAutomatic()).isFalse();
        assertThat(variables.get("${keyword_status}").isAutomatic()).isTrue();
    }

    @Test
    public void localVariablesAreProperlyConstructed_whenOmittingLocals() {
        final Map<Variable, VariableTypedValue> testVars = new HashMap<>();
        testVars.put(new Variable("${var}", VariableScope.GLOBAL), new VariableTypedValue("int", 1));
        testVars.put(new Variable("${true}", VariableScope.GLOBAL), new VariableTypedValue("bool", true));
        testVars.put(new Variable("${suite_var}", VariableScope.TEST_SUITE), new VariableTypedValue("string", "abc"));
        testVars.put(new Variable("${suite_name}", VariableScope.TEST_SUITE), new VariableTypedValue("int", 42));
        testVars.put(new Variable("${test_var}", VariableScope.TEST_CASE), new VariableTypedValue("unicode", "xyz"));
        testVars.put(new Variable("${test_name}", VariableScope.TEST_CASE), new VariableTypedValue("double", 4.2));
        testVars.put(new Variable("${local_var}", VariableScope.LOCAL), new VariableTypedValue("int", 55));
        testVars.put(new Variable("${keyword_status}", VariableScope.LOCAL), new VariableTypedValue("bool", false));

        final StackFrameVariables parentVars = StackFrameVariables.newNonLocalVariables(testVars);

        final Map<String, StackFrameVariable> variables = StackFrameVariables.newLocalVariables(parentVars, false)
                .getVariables();
        assertThat(variables).containsOnlyKeys("${var}", "${true}", "${suite_var}", "${suite_name}", "${test_var}",
                "${test_name}");
        assertThat(variables.get("${var}").isAutomatic()).isFalse();
        assertThat(variables.get("${true}").isAutomatic()).isTrue();
        assertThat(variables.get("${suite_var}").isAutomatic()).isFalse();
        assertThat(variables.get("${suite_name}").isAutomatic()).isTrue();
        assertThat(variables.get("${test_var}").isAutomatic()).isFalse();
        assertThat(variables.get("${test_name}").isAutomatic()).isTrue();
    }

    @Test
    public void iteratorIsReturned_withAllTheVariables() {
        final StackFrameVariable v1 = new StackFrameVariable(VariableScope.GLOBAL, true, "var1", "int", 5);
        final StackFrameVariable v2 = new StackFrameVariable(VariableScope.TEST_SUITE, false, "var2", "int", 6);
        final StackFrameVariable v3 = new StackFrameVariable(VariableScope.TEST_CASE, false, "var3", "int", 7);
        final StackFrameVariable v4 = new StackFrameVariable(VariableScope.LOCAL, false, "var4", "int", 8);

        final Map<String, StackFrameVariable> vars = new HashMap<>();
        vars.put("var1", v1);
        vars.put("var2", v2);
        vars.put("var3", v3);
        vars.put("var4", v4);
        final StackFrameVariables variables = new StackFrameVariables(vars);

        final Iterator<StackFrameVariable> iterator = variables.iterator();
        final List<StackFrameVariable> iteratorContent = newArrayList(iterator);

        assertThat(iteratorContent).containsOnly(v1, v2, v3, v4);
    }

    @Test
    public void updatingVariablesTest() {
        final Map<Variable, VariableTypedValue> toUpdate = new HashMap<>();
        toUpdate.put(new Variable("var1", VariableScope.GLOBAL), new VariableTypedValue("int", 5));
        toUpdate.put(new Variable("var2", VariableScope.TEST_SUITE), new VariableTypedValue("int", 1729));
        toUpdate.put(new Variable("var3", VariableScope.TEST_SUITE), new VariableTypedValue("int", 7));
        toUpdate.put(new Variable("var4", VariableScope.TEST_SUITE), new VariableTypedValue("double", 8));
        toUpdate.put(new Variable("var6", VariableScope.LOCAL), new VariableTypedValue("int", 9));

        final Map<String, StackFrameVariable> vars = new HashMap<>();
        vars.put("var1", new StackFrameVariable(VariableScope.GLOBAL, true, "var1", "int", 5));
        vars.put("var2", new StackFrameVariable(VariableScope.TEST_SUITE, false, "var2", "int", 6));
        vars.put("var3", new StackFrameVariable(VariableScope.LOCAL, false, "var2", "int", 7));
        vars.put("var4", new StackFrameVariable(VariableScope.TEST_SUITE, false, "var2", "int", 8));
        vars.put("var5", new StackFrameVariable(VariableScope.TEST_CASE, false, "var3", "int", 9));

        final StackFrameVariables variables = new StackFrameVariables(vars);
        final StackVariablesDelta delta = variables.update(toUpdate);

        assertThat(delta.isUnchanged("var1")).isTrue();
        assertThat(delta.isChanged("var1")).isFalse();

        assertThat(delta.isChanged("var2")).isTrue();
        assertThat(delta.isUnchanged("var2")).isFalse();

        assertThat(delta.isChanged("var3")).isTrue();
        assertThat(delta.isUnchanged("var3")).isFalse();

        assertThat(delta.isChanged("var4")).isTrue();
        assertThat(delta.isUnchanged("var4")).isFalse();

        assertThat(delta.isRemoved("var5")).isTrue();
        assertThat(delta.isAdded("var5")).isFalse();

        assertThat(delta.isAdded("var6")).isTrue();
        assertThat(delta.isRemoved("var6")).isFalse();

        assertThat(variables.getVariables()).containsOnlyKeys("var1", "var2", "var3", "var4", "var6");
        assertThat(variables.getVariables().values().stream().map(StackFrameVariable::getValue)).containsOnly(5, 1729,
                7, 8, 9);
    }
}
