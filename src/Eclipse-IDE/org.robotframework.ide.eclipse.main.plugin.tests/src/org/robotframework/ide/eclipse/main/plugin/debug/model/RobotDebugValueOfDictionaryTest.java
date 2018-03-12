/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable.RobotDebugVariableVisitor;

import com.google.common.collect.ImmutableMap;

public class RobotDebugValueOfDictionaryTest {

    @Test
    public void dictionaryPropertiesCheck() {
        final RobotDebugValueOfDictionary dictValue = RobotDebugValueOfDictionary.create(mock(RobotDebugVariable.class),
                "dict", ImmutableMap.of("a", new VariableTypedValue("int", 1), "b", new VariableTypedValue("int", 2)));

        assertThat(dictValue.getReferenceTypeName()).isEqualTo("dict");
        assertThat(dictValue.getValueString()).isEqualTo("dict[2]");
        assertThat(dictValue.getDetailedValue()).isEqualTo("{a=1, b=2}");
        assertThat(dictValue.isAllocated()).isTrue();
        assertThat(dictValue.hasVariables()).isTrue();
        assertThat(dictValue.getVariables()).hasSize(2);
    }

    @Test
    public void visitorDoesNotVisitAnything() {
        final RobotDebugValueOfDictionary dictValue = RobotDebugValueOfDictionary.create(mock(RobotDebugVariable.class),
                "dict", ImmutableMap.of("a", new VariableTypedValue("int", 1), "b", new VariableTypedValue("int", 2)));

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        dictValue.visitAllVariables(visitor);

        verify(visitor, times(2)).visit(any(RobotDebugVariable.class));
    }

    @Test
    public void noVariablesIsProvided_whenSearchingForVariableWithGivenName() {
        final RobotDebugValueOfDictionary dictValue = RobotDebugValueOfDictionary.create(mock(RobotDebugVariable.class),
                "dict", ImmutableMap.of("a", new VariableTypedValue("int", 1), "b", new VariableTypedValue("int", 2)));

        final RobotDebugVariable var = dictValue.getVariable("b");
        assertThat(var.getValue().getValueString()).isEqualTo("2");
        assertThat(dictValue.getVariable("x")).isNull();
    }
}
