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
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class RemoveVariableCommandTest {

    @Test
    public void nothingHappens_whenThereAreNoVariablesToDelete() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new RemoveVariableCommand(new ArrayList<RobotVariable>()))
                .execute();

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void variablesAreRemovedProperly() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RobotVariablesSection variablesSection = createVariables();

        final List<RobotVariable> varsToRemove = newArrayList(variablesSection.getChildren().get(1),
                variablesSection.getChildren().get(3));
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new RemoveVariableCommand(varsToRemove))
                .execute();

        assertThat(variablesSection.getChildren()).hasSize(3);
        assertThat(variablesSection.findChild("scalar")).isNotNull();
        assertThat(variablesSection.findChild("scalar_as_list")).isNull();
        assertThat(variablesSection.findChild("list")).isNotNull();
        assertThat(variablesSection.findChild("dict")).isNull();
        assertThat(variablesSection.findChild("invalid}")).isNotNull();

        final VariableTable varTable = variablesSection.getLinkedElement();
        assertThat(varTable.getVariables()).hasSize(3);

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_REMOVED, variablesSection);
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
