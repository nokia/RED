/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noChildren;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinitionConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTaskConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class CreateFreshHolderCommandTest {


    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void whenCommandIsUsedWithoutIndex_newCaseIsProperlyAddedAtTheEnd() {
        final RobotCasesSection section = createTestCasesSection();

        final CreateFreshHolderCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshHolderCommand(section));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(4);

        final RobotCase addedCase = section.getChildren().get(3);
        assertThat(addedCase.getName()).isEqualTo("case 4");
        assertThat(addedCase.getChildren()).isEmpty();
        assertThat(addedCase).has(RobotCaseConditions.properlySetParent());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren().size()).isEqualTo(3);

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedCase)));
        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void whenCommandIsUsedWithIndex_newCaseIsProperlyAddedAtSpecifiedPlace() {
        final RobotCasesSection section = createTestCasesSection();

        final CreateFreshHolderCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshHolderCommand(section, 1));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(4);

        final RobotCase addedCase = section.getChildren().get(1);
        assertThat(addedCase.getName()).isEqualTo("case 4");
        assertThat(addedCase.getChildren()).isEmpty();
        assertThat(addedCase).has(RobotCaseConditions.properlySetParent());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren().size()).isEqualTo(3);

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedCase)));
        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void whenCommandIsUsedWithoutIndex_newKeywordIsProperlyAddedAtTheEnd() {
        final RobotKeywordsSection section = createKeywordsSection();

        final CreateFreshHolderCommand command = new CreateFreshHolderCommand(section);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("Keyword 1", "Keyword 2",
                "Keyword 3", "Keyword 4");

        final RobotKeywordDefinition addedKeyword = section.getChildren().get(3);
        assertThat(addedKeyword).has(RobotKeywordDefinitionConditions.properlySetParent())
                .has(name("Keyword 4"))
                .has(noChildren());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("Keyword 1", "Keyword 2",
                "Keyword 3");

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedKeyword)));
        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void whenCommandIsUsedWithIndex_newKeywordIsProperlyAddedAtSpecifiedPlace() {
        final RobotKeywordsSection section = createKeywordsSection();

        final CreateFreshHolderCommand command = new CreateFreshHolderCommand(section, 1);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("Keyword 1", "Keyword 4",
                "Keyword 2", "Keyword 3");

        final RobotKeywordDefinition addedKeyword = section.getChildren().get(1);
        assertThat(addedKeyword).has(RobotKeywordDefinitionConditions.properlySetParent())
                .has(name("Keyword 4"))
                .has(noChildren());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("Keyword 1", "Keyword 2",
                "Keyword 3");

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedKeyword)));
        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void whenCommandIsUsedWithoutIndex_newTaskIsProperlyAddedAtTheEnd() {
        final RobotTasksSection section = createTasksSection();

        final CreateFreshHolderCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshHolderCommand(section));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(4);

        final RobotTask addedTask = section.getChildren().get(3);
        assertThat(addedTask.getName()).isEqualTo("task 4");
        assertThat(addedTask.getChildren()).isEmpty();
        assertThat(addedTask).has(RobotTaskConditions.properlySetParent());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren().size()).isEqualTo(3);

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedTask)));
        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void whenCommandIsUsedWithIndex_newTaskIsProperlyAddedAtSpecifiedPlace() {
        final RobotTasksSection section = createTasksSection();

        final CreateFreshHolderCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshHolderCommand(section, 1));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(4);

        final RobotTask addedTask = section.getChildren().get(1);
        assertThat(addedTask.getName()).isEqualTo("task 4");
        assertThat(addedTask.getChildren()).isEmpty();
        assertThat(addedTask).has(RobotTaskConditions.properlySetParent());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren().size()).isEqualTo(3);

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedTask)));
        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    private static RobotCasesSection createTestCasesSection() {
        return new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  Log  10")
                .appendLine("case 2")
                .appendLine("  Log  10")
                .appendLine("case 3")
                .appendLine("  Log  10")
                .build()
                .findSection(RobotCasesSection.class)
                .get();
    }

    private static RobotTasksSection createTasksSection() {
        return new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task 1")
                .appendLine("  Log  10")
                .appendLine("task 2")
                .appendLine("  Log  10")
                .appendLine("task 3")
                .appendLine("  Log  10")
                .build()
                .findSection(RobotTasksSection.class)
                .get();
    }

    private static RobotKeywordsSection createKeywordsSection() {
        return new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("Keyword 1")
                .appendLine("  Log  10")
                .appendLine("Keyword 2")
                .appendLine("  Log  10")
                .appendLine("Keyword 3")
                .appendLine("  Log  10")
                .build()
                .findSection(RobotKeywordsSection.class)
                .get();
    }
}
