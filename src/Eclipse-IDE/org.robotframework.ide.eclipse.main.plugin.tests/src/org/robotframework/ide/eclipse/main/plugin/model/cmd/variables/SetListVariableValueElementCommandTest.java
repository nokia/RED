/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class SetListVariableValueElementCommandTest {

    @Test
    public void nothingChangesAndEventBrokerDoesNotPost_whenOldValueIsTheSameAsNew() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariable();
        final RobotToken oldElement = ((ListVariable) variable.getLinkedElement()).getItems().get(0);

        final SetListVariableValueElementCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetListVariableValueElementCommand(variable, oldElement, "1"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[1, 2, 3]");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void elementValueChangesAndEventBrokerPostIt_whenOldValueIsDifferentThanNew() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariable();
        final RobotToken oldElement = ((ListVariable) variable.getLinkedElement()).getItems().get(0);

        final SetListVariableValueElementCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetListVariableValueElementCommand(variable, oldElement, "42"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("[42, 2, 3]");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    private static RobotVariable createVariable() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("@{list}  1  2  3 ")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren().get(0);
    }
}
