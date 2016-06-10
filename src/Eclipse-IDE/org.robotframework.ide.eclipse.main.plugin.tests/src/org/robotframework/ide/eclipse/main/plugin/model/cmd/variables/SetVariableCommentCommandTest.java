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
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class SetVariableCommentCommandTest {

    @Test
    public void commentIsProperlyCleared_whenEmptyIsGiven() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariable("# comment");

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableCommentCommand(variable, ""))
                .execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_COMMENT_CHANGE, variable);
        assertThat(variable.getComment()).isEmpty();
        assertThat(variable.getLinkedElement().getComment()).isEmpty();
    }

    @Test
    public void commentIsProperlyUpdated_whenEmptyIsGiven() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariable("# comment");

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableCommentCommand(variable, "# comment1 | comment2"))
                .execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_COMMENT_CHANGE, variable);
        assertThat(variable.getComment()).isEqualTo("# comment1 | comment2");
        assertThat(variable.getLinkedElement().getComment()).hasSize(2);
        assertThat(variable.getLinkedElement().getComment().get(0).getText()).isEqualTo("# comment1");
        assertThat(variable.getLinkedElement().getComment().get(1).getText()).isEqualTo("comment2");
    }

    private static RobotVariable createVariable(final String comment) {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${variable}  0  " + comment)
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren().get(0);
    }
}
