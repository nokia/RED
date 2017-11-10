/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand.CommandExecutionException;

public class CreateCompoundVariableValueElementCommandTest {

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToCreateElementInsideScalar() {
        final RobotVariable variable = createVariables().get(0);

        final CreateCompoundVariableValueElementCommand command = new CreateCompoundVariableValueElementCommand(
                variable, "");
        command.execute();
    }

    @Test
    public void newElementIsAddedToScalarAsListAndEventBrokerSendsEventAboutIt() {
        final RobotVariable variable = createVariables().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateCompoundVariableValueElementCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateCompoundVariableValueElementCommand(variable, "3"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[0, 1, 2, 3]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void newElementIsAddedToListAndEventBrokerSendsEventAboutIt() {
        final RobotVariable variable = createVariables().get(2);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateCompoundVariableValueElementCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateCompoundVariableValueElementCommand(variable, "4"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[1, 2, 3, 4]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void newElementIsAddedToDictionaryAndEventBrokerSendsEventAboutIt() {
        final RobotVariable variable = createVariables().get(3);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateCompoundVariableValueElementCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateCompoundVariableValueElementCommand(variable, "key=val"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("{a = 1, b = 2, c = 3, d = 4, key = val}");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void newElementIsAddedToInvalidAndEventBrokerSendsEventAboutIt() {
        final RobotVariable variable = createVariables().get(4);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateCompoundVariableValueElementCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateCompoundVariableValueElementCommand(variable, "4"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[1, 2, 3, 4]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
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
