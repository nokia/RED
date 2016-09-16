/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelFunctions.toNames;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveVariableUpCommandTest {

    @Test
    public void nothingHappens_whenFirstVariableIsTriedToBeMovedUp() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariablesSection varSection = createVariables();
        final RobotVariable variableToMove = varSection.getChildren().get(0);

        final MoveVariableUpCommand command = new MoveVariableUpCommand(variableToMove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(varSection.getChildren(), toNames())).containsExactly("scalar", "scalar_as_list", "list",
                "dict", "invalid}");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(varSection.getChildren(), toNames())).containsExactly("scalar", "scalar_as_list", "list",
                "dict", "invalid}");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void lastVariableIsMovedUpProperly() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariablesSection varSection = createVariables();
        final RobotVariable variableToMove = varSection.getChildren().get(varSection.getChildren().size() - 1);

        final MoveVariableUpCommand command = new MoveVariableUpCommand(variableToMove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(varSection.getChildren(), toNames())).containsExactly("scalar", "scalar_as_list", "list",
                "invalid}", "dict");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(varSection.getChildren(), toNames())).containsExactly("scalar", "scalar_as_list", "list",
                "dict", "invalid}");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_VARIABLE_MOVED, varSection);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void someInnerVariableIsMovedUpProperly() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariablesSection varSection = createVariables();
        final RobotVariable variableToMove = varSection.getChildren().get(2);

        final MoveVariableUpCommand command = new MoveVariableUpCommand(variableToMove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(varSection.getChildren(), toNames())).containsExactly("scalar", "list", "scalar_as_list",
                "dict", "invalid}");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(varSection.getChildren(), toNames())).containsExactly("scalar", "scalar_as_list", "list",
                "dict", "invalid}");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_VARIABLE_MOVED, varSection);
        verifyNoMoreInteractions(eventBroker);
    }

    private static RobotVariablesSection createVariables() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0")
                .appendLine("${scalar_as_list}  0  1  2")
                .appendLine("@{list}  1  2  3")
                .appendLine("&{dict}  a=1  b=2  c=3  d=4")
                .appendLine("invalid}  1  2  3")
                .build();
        return model.findSection(RobotVariablesSection.class).get();
    }

}
