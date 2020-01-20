/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.children;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinitionConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTaskConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class InsertHoldersCommandTest {

    private IEventBroker eventBroker;

    @BeforeEach
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void nothingHappens_whenThereAreNoCasesToInsert() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = new RobotCase[0];

        final InsertHoldersCommand command = new InsertHoldersCommand(section, casesToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).hasSize(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).hasSize(3);

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void caseAreProperlyInsertedAtTheSectionEnd_whenNoIndexIsProvided() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = createCasesToInsert();

        final InsertHoldersCommand command = new InsertHoldersCommand(section, casesToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 1", "case 2", "case 3", "inserted case 1", "inserted case 2");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(casesToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void caseAreProperlyInsertedInsideTheSection_whenIndexIsProvided() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = createCasesToInsert();

        final InsertHoldersCommand command = new InsertHoldersCommand(section, 1, casesToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 1", "inserted case 1", "inserted case 2", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(casesToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void casesAreProperlyInsertedAndRenamed_whenThereIsACaseWithSameNameAlready() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = createCasesWithSameNameToInsert();

        final InsertHoldersCommand command = new InsertHoldersCommand(section, casesToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 1", "case 2", "case 3", "case 4");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(casesToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenThereAreNoKeywordsToInsert() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = new RobotKeywordDefinition[0];

        final InsertHoldersCommand command = new InsertHoldersCommand(section, keywordsToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).hasSize(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).hasSize(3);

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void keywordsAreProperlyInsertedAtTheSectionEnd_whenNoIndexIsProvided() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = createKeywordsToInsert();

        final InsertHoldersCommand command = new InsertHoldersCommand(section, keywordsToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("kw 1", "kw 2", "kw 3", "inserted kw 1", "inserted kw 2");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(keywordsToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordsAreProperlyInsertedInsideTheSection_whenIndexIsProvided() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = createKeywordsToInsert();

        final InsertHoldersCommand command = new InsertHoldersCommand(section, 1,
                keywordsToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("kw 1", "inserted kw 1", "inserted kw 2", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(keywordsToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordsAreProperlyInsertedAndRenamed_whenThereIsAKeywordWithSameNameAlready() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = createKeywordsWithSameNameToInsert();

        final InsertHoldersCommand command = new InsertHoldersCommand(section, keywordsToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("kw 1", "kw 2", "kw 3", "kw 4");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(keywordsToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenThereAreNoTasksToInsert() {
        final RobotTasksSection section = createTasksSection();
        final RobotTask[] tasksToInsert = new RobotTask[0];

        final InsertHoldersCommand command = new InsertHoldersCommand(section, tasksToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).hasSize(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).hasSize(3);

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void tasksAreProperlyInsertedAtTheSectionEnd_whenNoIndexIsProvided() {
        final RobotTasksSection section = createTasksSection();
        final RobotTask[] tasksToInsert = createTasksToInsert();

        final InsertHoldersCommand command = new InsertHoldersCommand(section, tasksToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("task 1", "task 2", "task 3", "inserted task 1", "inserted task 2");
        assertThat(section.getChildren()).have(RobotTaskConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("task 1", "task 2", "task 3");
        assertThat(section.getChildren()).have(RobotTaskConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(tasksToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void tasksAreProperlyInsertedInsideTheSection_whenIndexIsProvided() {
        final RobotTasksSection section = createTasksSection();
        final RobotTask[] tasksToInsert = createTasksToInsert();

        final InsertHoldersCommand command = new InsertHoldersCommand(section, 1, tasksToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("task 1", "inserted task 1", "inserted task 2", "task 2", "task 3");
        assertThat(section.getChildren()).have(RobotTaskConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("task 1", "task 2", "task 3");
        assertThat(section.getChildren()).have(RobotTaskConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(tasksToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void tasksAreProperlyInsertedAndRenamed_whenThereIsATaskWithSameNameAlready() {
        final RobotTasksSection section = createTasksSection();
        final RobotTask[] tasksToInsert = createTasksWithSameNameToInsert();

        final InsertHoldersCommand command = new InsertHoldersCommand(section, tasksToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("task 1", "task 2", "task 3", "task 4");
        assertThat(section.getChildren()).have(RobotTaskConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("task 1", "task 2", "task 3");
        assertThat(section.getChildren()).have(RobotTaskConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap
                .of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(tasksToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    private static RobotCasesSection createTestCasesSection() {
        return new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("case 2")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  Log  10")
                .appendLine("case 3")
                .appendLine("  Log  10")
                .build().findSection(RobotCasesSection.class).get();
    }

    private static RobotCase[] createCasesToInsert() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("inserted case 1")
                .appendLine("  Log  10")
                .appendLine("inserted case 2")
                .appendLine("  Log  20")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren().toArray(new RobotCase[0]);
    }

    private static RobotCase[] createCasesWithSameNameToInsert() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  Log  10")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren().toArray(new RobotCase[0]);
    }

    private static RobotTasksSection createTasksSection() {
        return new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("task 2")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  Log  10")
                .appendLine("task 3")
                .appendLine("  Log  10")
                .build()
                .findSection(RobotTasksSection.class)
                .get();
    }

    private static RobotTask[] createTasksToInsert() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("inserted task 1")
                .appendLine("  Log  10")
                .appendLine("inserted task 2")
                .appendLine("  Log  20")
                .build();
        return model.findSection(RobotTasksSection.class).get().getChildren().toArray(new RobotTask[0]);
    }

    private static RobotTask[] createTasksWithSameNameToInsert() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task 1")
                .appendLine("  Log  10")
                .build();
        return model.findSection(RobotTasksSection.class).get().getChildren().toArray(new RobotTask[0]);
    }

    private static RobotKeywordsSection createKeywordsSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("kw 2")
                .appendLine("  [Teardown]  Log  xxx")
                .appendLine("  Log  10")
                .appendLine("kw 3")
                .appendLine("  Log  10")
                .build();
        return model.findSection(RobotKeywordsSection.class).get();
    }

    private static RobotKeywordDefinition[] createKeywordsToInsert() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("inserted kw 1")
                .appendLine("  Log  10")
                .appendLine("inserted kw 2")
                .appendLine("  Log  20")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren().toArray(new RobotKeywordDefinition[0]);
    }

    private static RobotKeywordDefinition[] createKeywordsWithSameNameToInsert() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  Log  10")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren().toArray(new RobotKeywordDefinition[0]);
    }
}
