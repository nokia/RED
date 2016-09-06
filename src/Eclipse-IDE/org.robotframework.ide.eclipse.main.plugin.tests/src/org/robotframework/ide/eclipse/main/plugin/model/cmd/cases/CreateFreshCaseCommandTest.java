/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.collect.ImmutableMap;

public class CreateFreshCaseCommandTest {

    @Test
    public void whenCommandIsUsedWithoutIndex_newCaseIsProperlyAddedAtTheEnd() {
        final RobotCasesSection section = createTestCasesSection();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateFreshCaseCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshCaseCommand(section));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(4);

        final RobotCase addedCase = section.getChildren().get(3);
        assertThat(addedCase.getName()).isEqualTo("case 4");
        assertThat(addedCase.getChildren()).isEmpty();
        assertThat(addedCase).has(RobotCaseConditions.properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_CASE_ADDED), eq(ImmutableMap
                .<String, Object> of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedCase)));
    }

    @Test
    public void whenCommandIsUsedWithIndex_newCaseIsProperlyAddedAtSpecifiedPlace() {
        final RobotCasesSection section = createTestCasesSection();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateFreshCaseCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshCaseCommand(section, 1));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(4);

        final RobotCase addedCase = section.getChildren().get(1);
        assertThat(addedCase.getName()).isEqualTo("case 4");
        assertThat(addedCase.getChildren()).isEmpty();
        assertThat(addedCase).has(RobotCaseConditions.properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_CASE_ADDED), eq(ImmutableMap
                .<String, Object> of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedCase)));
    }

    private static RobotCasesSection createTestCasesSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  Log  10")
                .appendLine("case 2")
                .appendLine("  Log  10")
                .appendLine("case 3")
                .appendLine("  Log  10")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section;
    }
}
