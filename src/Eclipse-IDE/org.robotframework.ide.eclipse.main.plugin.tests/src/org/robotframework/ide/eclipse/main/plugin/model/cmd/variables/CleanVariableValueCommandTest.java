/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class CleanVariableValueCommandTest {

    @Test
    public void thereIsNoValueForScalar_whenClearCommandIsExecuted() {
        final RobotVariable variable = createVariables().get(0);
        testValueClearing(variable, "");
    }

    @Test
    public void thereIsNoValueForScalarAsList_whenClearCommandIsExecuted() {
        final RobotVariable variable = createVariables().get(1);
        testValueClearing(variable, "");
    }

    @Test
    public void thereIsNoValueForList_whenClearCommandIsExecuted() {
        final RobotVariable variable = createVariables().get(2);
        testValueClearing(variable, "[]");
    }

    @Test
    public void thereIsNoValueForDict_whenClearCommandIsExecuted() {
        final RobotVariable variable = createVariables().get(3);
        testValueClearing(variable, "{}");
    }

    @Test
    public void thereIsNoValueForInvalid_whenClearCommandIsExecuted() {
        final RobotVariable variable = createVariables().get(4);
        testValueClearing(variable, "[]");
    }

    private void testValueClearing(final RobotVariable variable, final String expectedValueAfterClear) {

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CleanVariableValueCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CleanVariableValueCommand(variable));
        command.execute();

        assertThat(variable.getValue()).isEqualTo(expectedValueAfterClear);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    private static List<RobotVariable> createVariables() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0")
                .appendLine("${scalar_as_list}  0  1  2")
                .appendLine("@{list}  1  2  3")
                .appendLine("&{dict}  a=1  b=2  c=3  d=4")
                .appendLine("invalid}  1  2  3")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren();
    }
}
