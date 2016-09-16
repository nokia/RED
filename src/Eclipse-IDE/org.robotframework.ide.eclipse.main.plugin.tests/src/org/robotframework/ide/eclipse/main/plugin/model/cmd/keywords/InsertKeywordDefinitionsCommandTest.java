/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.children;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinitionConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class InsertKeywordDefinitionsCommandTest {

    @Test
    public void nothingHappens_whenThereAreNoCasesToInsert() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = new RobotKeywordDefinition[0];

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final InsertKeywordDefinitionsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordDefinitionsCommand(section, keywordsToInsert));
        command.execute();
        assertThat(section.getChildren().size()).isEqualTo(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren().size()).isEqualTo(3);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void keywordsAreProperlInsertedAtTheSectionEnd_whenNoIndexIsProvided() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = createKeywordsToInsert();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final InsertKeywordDefinitionsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordDefinitionsCommand(section, keywordsToInsert));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(5);

        assertKeyword(section.getChildren().get(0), "kw 1");
        assertKeyword(section.getChildren().get(1), "kw 2");
        assertKeyword(section.getChildren().get(2), "kw 3");
        assertKeyword(section.getChildren().get(3), "inserted kw 1");
        assertKeyword(section.getChildren().get(4), "inserted kw 2");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren().size()).isEqualTo(3);

        assertKeyword(section.getChildren().get(0), "kw 1");
        assertKeyword(section.getChildren().get(1), "kw 2");
        assertKeyword(section.getChildren().get(2), "kw 3");

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.<String, Object> of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(keywordsToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordsAreProperlInsertedInsideTheSection_whenIndexIsProvided() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = createKeywordsToInsert();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final InsertKeywordDefinitionsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordDefinitionsCommand(section, 1, keywordsToInsert));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(5);

        assertKeyword(section.getChildren().get(0), "kw 1");
        assertKeyword(section.getChildren().get(1), "inserted kw 1");
        assertKeyword(section.getChildren().get(2), "inserted kw 2");
        assertKeyword(section.getChildren().get(3), "kw 2");
        assertKeyword(section.getChildren().get(4), "kw 3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren().size()).isEqualTo(3);

        assertKeyword(section.getChildren().get(0), "kw 1");
        assertKeyword(section.getChildren().get(1), "kw 2");
        assertKeyword(section.getChildren().get(2), "kw 3");

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.<String, Object> of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(keywordsToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void keywordsAreProperlyInsertedAndRenamed_whenThereIsAKeywordWithSameNameAlready() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition[] keywordsToInsert = createKeywordsWithSameNameToInsert();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final InsertKeywordDefinitionsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertKeywordDefinitionsCommand(section, keywordsToInsert));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(4);

        assertKeyword(section.getChildren().get(0), "kw 1");
        assertKeyword(section.getChildren().get(1), "kw 2");
        assertKeyword(section.getChildren().get(2), "kw 3");
        assertKeyword(section.getChildren().get(3), "kw 1 1");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren().size()).isEqualTo(3);

        assertKeyword(section.getChildren().get(0), "kw 1");
        assertKeyword(section.getChildren().get(1), "kw 2");
        assertKeyword(section.getChildren().get(2), "kw 3");

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.<String, Object> of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(keywordsToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    private static void assertKeyword(final RobotKeywordDefinition keyword, final String expectedName) {
        assertThat(keyword).has(RobotKeywordDefinitionConditions.properlySetParent())
                .has(name(expectedName))
                .has(children());
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
