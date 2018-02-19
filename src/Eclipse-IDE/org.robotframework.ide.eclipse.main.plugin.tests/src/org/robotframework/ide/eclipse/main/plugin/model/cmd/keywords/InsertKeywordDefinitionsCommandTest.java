/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.children;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelFunctions.toNames;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinitionConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class InsertKeywordDefinitionsCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void nothingHappens_whenThereAreNoCasesToInsert() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = new RobotKeywordDefinition[0];

        final InsertKeywordDefinitionsCommand command = new InsertKeywordDefinitionsCommand(section, keywordsToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).hasSize(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).hasSize(3);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void keywordsAreProperlyInsertedAtTheSectionEnd_whenNoIndexIsProvided() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = createKeywordsToInsert();

        final InsertKeywordDefinitionsCommand command = new InsertKeywordDefinitionsCommand(section, keywordsToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3", "inserted kw 1",
                "inserted kw 2");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(keywordsToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordsAreProperlyInsertedInsideTheSection_whenIndexIsProvided() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = createKeywordsToInsert();

        final InsertKeywordDefinitionsCommand command = new InsertKeywordDefinitionsCommand(section, 1, keywordsToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "inserted kw 1",
                "inserted kw 2", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(keywordsToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordsAreProperlyInsertedAndRenamed_whenThereIsAKeywordWithSameNameAlready() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = createKeywordsWithSameNameToInsert();

        final InsertKeywordDefinitionsCommand command = new InsertKeywordDefinitionsCommand(section, keywordsToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3", "kw 4");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(keywordsToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
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
