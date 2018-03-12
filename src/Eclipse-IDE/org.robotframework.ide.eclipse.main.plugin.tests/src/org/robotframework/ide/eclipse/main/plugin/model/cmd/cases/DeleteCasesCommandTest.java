/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.children;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class DeleteCasesCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void nothingHappens_whenThereAreNoCasesToRemove() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotCase> casesToRemove = newArrayList();

        final DeleteCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteCasesCommand(casesToRemove));
        command.execute();
        assertThat(section.getChildren().size()).isEqualTo(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren().size()).isEqualTo(3);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void singleCaseIsProperlyRemoved() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotCase> casesToRemove = newArrayList(section.getChildren().get(1));

        final DeleteCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteCasesCommand(casesToRemove));
        command.execute();

        assertThat(section.getChildren()).hasSize(2);
        assertThat(section.getChildren().get(0)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 1"))
                .has(children());
        assertThat(section.getChildren().get(1)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 3"))
                .has(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren()).hasSize(3);
        assertThat(section.getChildren().get(1)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 2"))
                .has(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_CASE_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_CASE_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, casesToRemove)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void multipleCasesAreProperlyRemoved() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotCase> casesToRemove = newArrayList(section.getChildren().get(0), section.getChildren().get(2));

        final DeleteCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteCasesCommand(casesToRemove));
        command.execute();

        assertThat(section.getChildren()).hasSize(1);
        assertThat(section.getChildren().get(0)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 2"))
                .has(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren()).hasSize(3);
        assertThat(section.getChildren().get(0)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 1"))
                .has(children());
        assertThat(section.getChildren().get(1)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 2"))
                .has(children());
        assertThat(section.getChildren().get(2)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 3"))
                .has(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_CASE_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_CASE_ADDED), eq(ImmutableMap.of(IEventBroker.DATA, section,
                RobotModelEvents.ADDITIONAL_DATA, newArrayList(section.getChildren().get(0)))));
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_CASE_ADDED), eq(ImmutableMap.of(IEventBroker.DATA, section,
                RobotModelEvents.ADDITIONAL_DATA, newArrayList(section.getChildren().get(2)))));
        verifyNoMoreInteractions(eventBroker);
    }

    private static RobotCasesSection createTestCasesSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("case 2")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  Log  10")
                .appendLine("case 3")
                .appendLine("  Log  10")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section;
    }
}
