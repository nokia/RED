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

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class CreateFreshVariableCommandTest {

    @Test
    public void newVariableIsAddedAtTheEndOfSectionProperly() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RobotVariablesSection variablesSection = createVariables();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshVariableCommand(variablesSection, VariableType.SCALAR))
                .execute();

        assertThat(variablesSection.getChildren()).hasSize(6);
        final RobotVariable addedParameter = variablesSection.getChildren().get(5);
        assertThat(addedParameter.getType()).isEqualTo(VariableType.SCALAR);
        assertThat(addedParameter.getName()).isEqualTo("var");
        assertThat(addedParameter.getValue()).isEmpty();
        assertThat(addedParameter.getComment()).isEmpty();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
    }

    @Test
    public void consecutivelyCreatedVariablesHaveCounterSuffix() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RobotVariablesSection variablesSection = createVariables();

        for (int i = 0; i < 3; i++) {
            ContextInjector.prepareContext()
                    .inWhich(eventBroker)
                    .isInjectedInto(new CreateFreshVariableCommand(variablesSection, VariableType.SCALAR))
                    .execute();
        }

        assertThat(variablesSection.getChildren()).hasSize(8);
        assertThat(variablesSection.getChildren().get(5).getName()).isEqualTo("var");
        assertThat(variablesSection.getChildren().get(6).getName()).isEqualTo("var1");
        assertThat(variablesSection.getChildren().get(7).getName()).isEqualTo("var2");

        verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
    }

    @Test
    public void newVariableIsAddedAtGivenIndexWithGivenTypeProperly() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RobotVariablesSection variablesSection = createVariables();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshVariableCommand(variablesSection, 2, VariableType.LIST))
                .execute();

        assertThat(variablesSection.getChildren()).hasSize(6);
        final RobotVariable addedParameter = variablesSection.getChildren().get(2);
        assertThat(addedParameter.getType()).isEqualTo(VariableType.LIST);
        assertThat(addedParameter.getName()).isEqualTo("var");
        assertThat(addedParameter.getValue()).isEqualTo("[]");
        assertThat(addedParameter.getComment()).isEmpty();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
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
