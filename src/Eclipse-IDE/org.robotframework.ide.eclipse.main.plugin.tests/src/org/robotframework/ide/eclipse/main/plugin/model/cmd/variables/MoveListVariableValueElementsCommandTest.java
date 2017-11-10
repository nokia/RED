/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveDirection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand.CommandExecutionException;

public class MoveListVariableValueElementsCommandTest {

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToMoveElementsInScalar() {
        final RobotVariable variable = createVariables().get(0);

        final MoveListVariableValueElementsCommand command = new MoveListVariableValueElementsCommand(variable,
                newArrayList(new RobotToken()), MoveDirection.UP);
        command.execute();
    }

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToMoveElementsInDictionary() {
        final RobotVariable variable = createVariables().get(3);

        final MoveListVariableValueElementsCommand command = new MoveListVariableValueElementsCommand(variable,
                newArrayList(new RobotToken()), MoveDirection.UP);
        command.execute();
    }

    @Test
    public void elementsAreMovedUpInScalarAsListAndEventBrokerSendsEvent() {
        final RobotVariable variable = createVariables().get(1);

        final List<RobotToken> elementsToMove = newArrayList(
                ((ScalarVariable) variable.getLinkedElement()).getValues().subList(1, 3));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveListVariableValueElementsCommand(variable, elementsToMove, MoveDirection.UP));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[1, 2, 0, 3]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementsAreMovedDownInScalarAsListAndEventBrokerSendsEvent() {
        final RobotVariable variable = createVariables().get(1);

        final List<RobotToken> elementsToMove = newArrayList(
                ((ScalarVariable) variable.getLinkedElement()).getValues().subList(1, 3));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveListVariableValueElementsCommand(variable, elementsToMove, MoveDirection.DOWN));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[0, 3, 1, 2]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementsAreMovedUpInListAndEventBrokerSendsEvent() {
        final RobotVariable variable = createVariables().get(2);

        final List<RobotToken> elementsToRemove = newArrayList(
                ((ListVariable) variable.getLinkedElement()).getItems().subList(1, 3));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveListVariableValueElementsCommand(variable, elementsToRemove, MoveDirection.UP));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[2, 3, 1, 4]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementsAreMovedDownInListAndEventBrokerSendsEvent() {
        final RobotVariable variable = createVariables().get(2);

        final List<RobotToken> elementsToRemove = newArrayList(
                ((ListVariable) variable.getLinkedElement()).getItems().subList(1, 3));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(
                        new MoveListVariableValueElementsCommand(variable, elementsToRemove, MoveDirection.DOWN));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[1, 4, 2, 3]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementsAreMovedUpInInvalidAndEventBrokerSendsEvent() {
        final RobotVariable variable = createVariables().get(4);

        final List<RobotToken> elementsToRemove = newArrayList(
                ((UnknownVariable) variable.getLinkedElement()).getItems().subList(1, 3));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveListVariableValueElementsCommand(variable, elementsToRemove, MoveDirection.UP));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[2, 3, 1, 5]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementsAreMovedDownInInvalidAndEventBrokerSendsEvent() {
        final RobotVariable variable = createVariables().get(4);

        final List<RobotToken> elementsToRemove = newArrayList(
                ((UnknownVariable) variable.getLinkedElement()).getItems().subList(1, 3));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(
                        new MoveListVariableValueElementsCommand(variable, elementsToRemove, MoveDirection.DOWN));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[1, 5, 2, 3]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    private static List<RobotVariable> createVariables() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0")
                .appendLine("${scalar_as_list}  0  1  2  3")
                .appendLine("@{list}  1  2  3  4")
                .appendLine("&{dict}  a=1  b=2")
                .appendLine("invalid}  1  2  3  5")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren();
    }
}
