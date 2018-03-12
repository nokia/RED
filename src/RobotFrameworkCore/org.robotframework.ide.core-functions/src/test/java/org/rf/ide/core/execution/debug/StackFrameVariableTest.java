/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

import com.google.common.collect.ImmutableMap;

public class StackFrameVariableTest {

    @Test
    public void gettersAndSettersTests() {
        final StackFrameVariable var = new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10);

        assertThat(var.getName()).isEqualTo("var");
        assertThat(var.isAutomatic()).isTrue();

        assertThat(var.getScope()).isEqualTo(VariableScope.GLOBAL);
        var.setScope(VariableScope.TEST_SUITE);
        assertThat(var.getScope()).isEqualTo(VariableScope.TEST_SUITE);

        assertThat(var.getType()).isEqualTo("int");
        var.setType("string");
        assertThat(var.getType()).isEqualTo("string");

        assertThat(var.getValue()).isEqualTo(10);
        var.setValue(20);
        assertThat(var.getValue()).isEqualTo(20);
    }

    @Test
    public void cloningTest() {
        final Map<Object, Object> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", newArrayList("x", "y"));

        final StackFrameVariable scalar = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10);
        final StackFrameVariable list = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                newArrayList(1, 2, 3));
        final StackFrameVariable dict = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "dict", map);

        final StackFrameVariable scalarCopy = scalar.copy();
        assertThat(scalarCopy.getName()).isEqualTo("var");
        assertThat(scalarCopy.isAutomatic()).isFalse();
        assertThat(scalarCopy.getScope()).isEqualTo(VariableScope.GLOBAL);
        assertThat(scalarCopy.getType()).isEqualTo("int");
        assertThat(scalarCopy.getValue()).isEqualTo(10);

        final StackFrameVariable listCopy = list.copy();
        assertThat(listCopy.getName()).isEqualTo("var");
        assertThat(listCopy.isAutomatic()).isFalse();
        assertThat(listCopy.getScope()).isEqualTo(VariableScope.GLOBAL);
        assertThat(listCopy.getType()).isEqualTo("list");
        assertThat(listCopy.getValue()).isEqualTo(newArrayList(1, 2, 3));
        assertThat(listCopy.getValue()).isNotSameAs(list.getValue());

        final StackFrameVariable dictCopy = dict.copy();
        assertThat(dictCopy.getName()).isEqualTo("var");
        assertThat(dictCopy.isAutomatic()).isFalse();
        assertThat(dictCopy.getScope()).isEqualTo(VariableScope.GLOBAL);
        assertThat(dictCopy.getType()).isEqualTo("dict");
        assertThat(dictCopy.getValue()).isEqualTo(ImmutableMap.of("a", 1, "b", newArrayList("x", "y")));
        assertThat(dictCopy.getValue()).isNotSameAs(list.getValue());
        assertThat(((Map<?, ?>) dictCopy.getValue()).get("b")).isNotSameAs(((Map<?, ?>) dict.getValue()).get("b"));
    }

    @Test
    public void equalsTests() {
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10))
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10));

        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10))
                .isNotEqualTo(new StackFrameVariable(VariableScope.TEST_CASE, true, "var", "int", 10));
        assertThat(new StackFrameVariable(VariableScope.TEST_CASE, true, "var", "int", 10))
                .isNotEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10));
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10))
                .isNotEqualTo(new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10));
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10))
                .isNotEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10));
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10))
                .isNotEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var1", "int", 10));
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var1", "int", 10))
                .isNotEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10));
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10))
                .isNotEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "string", 10));
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "string", 10))
                .isNotEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10));
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10))
                .isNotEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 20));
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 20))
                .isNotEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10));
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10)).isNotEqualTo(new Object());
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10)).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10).hashCode())
                .isEqualTo(new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10).hashCode());
    }
}
