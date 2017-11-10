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

public class SetScalarValueCommandTest {

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToSetValueToNonScalarVariable_1() {
        final RobotVariable variable = createVariablesForTest().get(2);

        new SetScalarValueCommand(variable, "42").execute();
    }

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToSetValueToNonScalarVariable_2() {
        final RobotVariable variable = createVariablesForTest().get(3);

        new SetScalarValueCommand(variable, "42").execute();
    }

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToSetValueToNonScalarVariable_3() {
        final RobotVariable variable = createVariablesForTest().get(4);

        new SetScalarValueCommand(variable, "42").execute();
    }

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToSetValueToNonScalarVariable_4() {
        final RobotVariable variable = createVariablesForTest().get(5);

        new SetScalarValueCommand(variable, "42").execute();
    }

    @Test
    public void valueIsChangedAndEventIsPosted_whenEmptyScalarGetsValue() {
        final RobotVariable variable = createVariablesForTest().get(0);
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final SetScalarValueCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetScalarValueCommand(variable, "100"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("100");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void valueIsChangedAndEventIsPosted_whenNoneEmptyScalarChangesValue() {
        final RobotVariable variable = createVariablesForTest().get(1);
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final SetScalarValueCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetScalarValueCommand(variable, "100"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("100");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void valueIsChangedToEmptyAndEventIsPosted_whenNullIsGiven() {
        final RobotVariable variable = createVariablesForTest().get(1);
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final SetScalarValueCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetScalarValueCommand(variable, null));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    private static List<RobotVariable> createVariablesForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar1}")
                .appendLine("${scalar2}  0")
                .appendLine("${scalar_as_list}  0  1  2")
                .appendLine("@{list}  1  2  3 ")
                .appendLine("&{dictionary}  a=1  b=2  c=3")
                .appendLine("invalid}  0")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren();
    }

}
