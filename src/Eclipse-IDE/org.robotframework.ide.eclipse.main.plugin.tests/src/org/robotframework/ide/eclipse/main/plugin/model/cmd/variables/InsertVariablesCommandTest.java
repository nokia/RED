/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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

public class InsertVariablesCommandTest {

    @Test
    public void variablesAreAddedAtTheEndOfSectionProperly() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RobotVariablesSection variablesSection = createVariables();

        final RobotVariable[] variablesToInsert = createVariablesToInsert();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertVariablesCommand(variablesSection, variablesToInsert))
                .execute();

        assertThat(variablesSection.getChildren()).hasSize(7);

        final RobotVariable firstAdded = variablesSection.getChildren().get(5);
        final RobotVariable secondAdded = variablesSection.getChildren().get(6);

        assertThat(firstAdded.getType()).isEqualTo(VariableType.SCALAR);
        assertThat(firstAdded.getName()).isEqualTo("a");
        assertThat(firstAdded.getValue()).isEqualTo("0");
        assertThat(firstAdded.getComment()).isEqualTo("# comment1");

        assertThat(secondAdded.getType()).isEqualTo(VariableType.LIST);
        assertThat(secondAdded.getName()).isEqualTo("b");
        assertThat(secondAdded.getValue()).isEqualTo("[1, 2, 3]");
        assertThat(secondAdded.getComment()).isEqualTo("# comment2");

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
    }

    @Test
    public void variablesAreAddedAtGivenIndex() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RobotVariablesSection variablesSection = createVariables();

        final RobotVariable[] variablesToInsert = createVariablesToInsert();

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertVariablesCommand(variablesSection, 3, variablesToInsert))
                .execute();

        assertThat(variablesSection.getChildren()).hasSize(7);

        final RobotVariable firstAdded = variablesSection.getChildren().get(3);
        final RobotVariable secondAdded = variablesSection.getChildren().get(4);

        assertThat(firstAdded.getType()).isEqualTo(VariableType.SCALAR);
        assertThat(firstAdded.getName()).isEqualTo("a");
        assertThat(firstAdded.getValue()).isEqualTo("0");
        assertThat(firstAdded.getComment()).isEqualTo("# comment1");

        assertThat(secondAdded.getType()).isEqualTo(VariableType.LIST);
        assertThat(secondAdded.getName()).isEqualTo("b");
        assertThat(secondAdded.getValue()).isEqualTo("[1, 2, 3]");
        assertThat(secondAdded.getComment()).isEqualTo("# comment2");

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

    private static RobotVariable[] createVariablesToInsert() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${a}  0  # comment1")
                .appendLine("@{b}  1  2  3  # comment2")
                .build();
        return model.findSection(RobotVariablesSection.class).get().getChildren().toArray(new RobotVariable[0]);
    }
}
