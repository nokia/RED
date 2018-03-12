/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable.RobotDebugVariableVisitor;

public class RobotDebugValueOfListTest {

    @Test
    public void listPropertiesCheck() {
        final RobotDebugValueOfList listValue = RobotDebugValueOfList.create(mock(RobotDebugVariable.class), "list",
                newArrayList(new VariableTypedValue("int", 1),
                        new VariableTypedValue("int", 2),
                        new VariableTypedValue("int", 3)));

        assertThat(listValue.getReferenceTypeName()).isEqualTo("list");
        assertThat(listValue.getValueString()).isEqualTo("list[3]");
        assertThat(listValue.getDetailedValue()).isEqualTo("[1, 2, 3]");
        assertThat(listValue.isAllocated()).isTrue();
        assertThat(listValue.hasVariables()).isTrue();
        assertThat(listValue.getVariables()).hasSize(3);
    }

    @Test
    public void visitorDoesNotVisitAnything() {
        final RobotDebugValueOfList listValue = RobotDebugValueOfList.create(mock(RobotDebugVariable.class), "list",
                newArrayList(new VariableTypedValue("int", 1),
                        new VariableTypedValue("int", 2),
                        new VariableTypedValue("int", 3)));

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        listValue.visitAllVariables(visitor);

        verify(visitor, times(3)).visit(any(RobotDebugVariable.class));
    }

    @Test
    public void noVariablesIsProvided_whenSearchingForVariableWithGivenName() {
        final RobotDebugValueOfList listValue = RobotDebugValueOfList.create(mock(RobotDebugVariable.class), "list",
                newArrayList(new VariableTypedValue("int", 1),
                        new VariableTypedValue("int", 2),
                        new VariableTypedValue("int", 3)));

        final RobotDebugVariable var = listValue.getVariable("[1]");
        assertThat(var.getValue().getValueString()).isEqualTo("2");
        assertThat(listValue.getVariable("x")).isNull();
    }
}
