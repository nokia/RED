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
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class SetDictVariableValueElementCommandTest {

    @Test
    public void nothingChangesAndEventBrokerDoesNotPost_whenOldValueIsTheSameAsNew() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariable();
        final DictionaryKeyValuePair oldElement = ((DictionaryVariable) variable.getLinkedElement()).getItems().get(0);

        final SetDictVariableValueElementCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetDictVariableValueElementCommand(variable, oldElement, "a=1"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("{a = 1, b = 2}");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void elementValueChangesAndEventBrokerPostIt_whenOnlyKeyChanges() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariable();
        final DictionaryKeyValuePair oldElement = ((DictionaryVariable) variable.getLinkedElement()).getItems().get(0);

        final SetDictVariableValueElementCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetDictVariableValueElementCommand(variable, oldElement, "new_key=1"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("{new_key = 1, b = 2}");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Test
    public void elementValueChangesAndEventBrokerPostIt_whenOnlyValueChanges() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariable();
        final DictionaryKeyValuePair oldElement = ((DictionaryVariable) variable.getLinkedElement()).getItems().get(0);

        final SetDictVariableValueElementCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetDictVariableValueElementCommand(variable, oldElement, "a=new_value"));
        command.execute();

        assertThat(variable.getValue()).isEqualTo("{a = new_value, b = 2}");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    private static RobotVariable createVariable() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("&{dict}  a=1  b=2")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren().get(0);
    }
}
