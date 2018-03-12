/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable.RobotDebugVariableVisitor;

import com.google.common.collect.ImmutableMap;

public class RobotDebugValueTest {

    @Test
    public void properValueIsCreated_dependingOnValueType() {
        final RobotDebugValue val1 = RobotDebugValue.createFromValue(mock(RobotDebugVariable.class), "int", null);
        assertThat(val1).isInstanceOf(RobotDebugValueOfScalar.class);
        final RobotDebugValue val2 = RobotDebugValue.createFromValue(mock(RobotDebugVariable.class), "string", "foo");
        assertThat(val2).isInstanceOf(RobotDebugValueOfScalar.class);
        final RobotDebugValue val3 = RobotDebugValue.createFromValue(mock(RobotDebugVariable.class), "list",
                newArrayList(new VariableTypedValue("string", "a"), new VariableTypedValue("int", 1)));
        assertThat(val3).isInstanceOf(RobotDebugValueOfList.class);
        final RobotDebugValue val4 = RobotDebugValue.createFromValue(mock(RobotDebugVariable.class), "dict",
                ImmutableMap.of("x", new VariableTypedValue("int", 1), "y", new VariableTypedValue("string", "z")));
        assertThat(val4).isInstanceOf(RobotDebugValueOfDictionary.class);
    }

    @Test
    public void valuePropertiesCheck() {
        final RobotDebugValue value = valueToTest(mock(RobotDebugTarget.class), "type", "val");

        assertThat(value.getReferenceTypeName()).isEqualTo("type");
        assertThat(value.isTuple()).isFalse();
        assertThat(value.getValueString()).isEqualTo("val");
        assertThat(value.isAllocated()).isTrue();
        assertThat(value.hasVariables()).isFalse();
        assertThat(value.getVariables()).isEmpty();
    }

    @Test
    public void valuePropertiesCheckOfTuple() {
        final RobotDebugValue value = valueToTest(mock(RobotDebugTarget.class), "tuple", "val");

        assertThat(value.getReferenceTypeName()).isEqualTo("tuple");
        assertThat(value.isTuple()).isTrue();
        assertThat(value.getValueString()).isEqualTo("val");
        assertThat(value.isAllocated()).isTrue();
        assertThat(value.hasVariables()).isFalse();
        assertThat(value.getVariables()).isEmpty();
    }

    @Test
    public void visitorDoesNotVisitAnything() {
        final RobotDebugValue value = valueToTest(mock(RobotDebugTarget.class), "type", "val");

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        value.visitAllVariables(visitor);

        verifyZeroInteractions(visitor);
    }

    @Test
    public void noVariablesIsProvided_whenSearchingForVariableWithGivenName() {
        final RobotDebugValue value = valueToTest(mock(RobotDebugTarget.class), "type", "val");

        assertThat(value.getVariable("x")).isNull();
    }

    private static RobotDebugValue valueToTest(final RobotDebugTarget target, final String type, final String value) {
        return new RobotDebugValue(target, type, value) {

            @Override
            public String getDetailedValue() {
                return "";
            }
        };
    }

}
