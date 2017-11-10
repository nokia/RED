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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand.CommandExecutionException;

public class RemoveListVariableValueElementsCommandTest {

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToRemoveElementsFromScalar() {
        final RobotVariable variable = createVariables().get(0);

        final RemoveListVariableValueElementsCommand command = new RemoveListVariableValueElementsCommand(variable,
                newArrayList(new RobotToken()));
        command.execute();
    }

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToRemoveElementsFromDictionary() {
        final RobotVariable variable = createVariables().get(3);

        final RemoveListVariableValueElementsCommand command = new RemoveListVariableValueElementsCommand(variable,
                newArrayList(new RobotToken()));
        command.execute();
    }

    @Test
    public void elementIsRemovedFromScalarAsListAndEventBrokerSendsEvent_1() {
        final RobotVariable variable = createVariables().get(1);

        final Collection<RobotToken> elementsToRemove = newArrayList(
                ((ScalarVariable) variable.getLinkedElement()).getValues().subList(0, 1));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RemoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new RemoveListVariableValueElementsCommand(variable, elementsToRemove));
        command.execute();

        assertThat(variable.getType()).isEqualTo(VariableType.SCALAR_AS_LIST);
        assertThat(variable.getValue()).isEqualTo("[1, 2]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementIsRemovedFromScalarAsListAndEventBrokerSendsEvent_2() {
        final RobotVariable variable = createVariables().get(1);

        final Collection<RobotToken> elementsToRemove = newArrayList(
                ((ScalarVariable) variable.getLinkedElement()).getValues().subList(0, 2));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RemoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new RemoveListVariableValueElementsCommand(variable, elementsToRemove));
        command.execute();

        assertThat(variable.getType()).isEqualTo(VariableType.SCALAR);
        assertThat(variable.getValue()).isEqualTo("2");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementIsRemovedFromListAndEventBrokerSendsEvent() {
        final RobotVariable variable = createVariables().get(2);

        final Collection<RobotToken> elementsToRemove = newArrayList(
                ((ListVariable) variable.getLinkedElement()).getItems().subList(1, 2));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RemoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new RemoveListVariableValueElementsCommand(variable, elementsToRemove));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[1, 3]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementIsRemovedFromInvalidAndEventBrokerSendsEvent() {
        final RobotVariable variable = createVariables().get(4);

        final Collection<RobotToken> elementsToRemove = newArrayList(
                ((UnknownVariable) variable.getLinkedElement()).getItems().subList(1, 3));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RemoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new RemoveListVariableValueElementsCommand(variable, elementsToRemove));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[1]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementIsRemovedFromListAndReturnsToPreviousState() {
        final RobotVariable variable = createVariables().get(2);

        final Collection<RobotToken> elementsToRemove = newArrayList(
                ((ListVariable) variable.getLinkedElement()).getItems().subList(0, 2));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RemoveListVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new RemoveListVariableValueElementsCommand(variable, elementsToRemove));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[3]");

        List<EditorCommand> undoCommands = command.getUndoCommands();
        for (final EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }

        assertThat(variable.getValue()).isEqualTo("[1, 2, 3]");

        final List<EditorCommand> redoCommands = new ArrayList<>();
        for (final EditorCommand undoCommand : undoCommands) {
            redoCommands.addAll(0, undoCommand.getUndoCommands());
        }
        for (final EditorCommand redoCommand : redoCommands) {
            redoCommand.execute();
        }

        assertThat(variable.getValue()).isEqualTo("[3]");

        undoCommands = new ArrayList<>();
        for (final EditorCommand redoCommand : redoCommands) {
            undoCommands.addAll(0, redoCommand.getUndoCommands());
        }
        for (final EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }

        assertThat(variable.getValue()).isEqualTo("[1, 2, 3]");
    }

    private static List<RobotVariable> createVariables() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0")
                .appendLine("${scalar_as_list}  0  1  2")
                .appendLine("@{list}  1  2  3")
                .appendLine("&{dict}  a=1  b=2")
                .appendLine("invalid}  1  2  3")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren();
    }
}
