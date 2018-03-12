/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable.RobotDebugVariableVisitor;

public class RobotDebugValueOfScalarTest {

    @Test
    public void scalarPropertiesCheck() {
        final RobotDebugValueOfScalar scalarValue = RobotDebugValueOfScalar.create(mock(RobotDebugVariable.class),
                "int", "10");

        assertThat(scalarValue.getReferenceTypeName()).isEqualTo("int");
        assertThat(scalarValue.getValueString()).isEqualTo("10");
        assertThat(scalarValue.getDetailedValue()).isEqualTo("10");
        assertThat(scalarValue.isAllocated()).isTrue();
        assertThat(scalarValue.hasVariables()).isFalse();
        assertThat(scalarValue.getVariables()).isEmpty();
    }

    @Test
    public void visitorDoesNotVisitAnything() {
        final RobotDebugValueOfScalar scalarValue = RobotDebugValueOfScalar.create(mock(RobotDebugVariable.class),
                "int", "10");

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        scalarValue.visitAllVariables(visitor);

        verifyZeroInteractions(visitor);
    }

    @Test
    public void noVariablesIsProvided_whenSearchingForVariableWithGivenName() {
        final RobotDebugValueOfScalar scalarValue = RobotDebugValueOfScalar.create(mock(RobotDebugVariable.class),
                "int", "10");

        final RobotDebugVariable var = scalarValue.getVariable("a");
        assertThat(var).isNull();
    }

}
