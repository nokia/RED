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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.children;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelFunctions.toNames;

import java.util.List;

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

public class DeleteKeywordDefinitionCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void nothingHappens_whenThereAreNoKeywordsToRemove() {
        final RobotKeywordsSection section = createKeywordsSection();
        final List<RobotKeywordDefinition> keywordsToRemove = newArrayList();

        final DeleteKeywordDefinitionCommand command = new DeleteKeywordDefinitionCommand(keywordsToRemove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void singleKeywordIsProperlyRemoved() {
        final RobotKeywordsSection section = createKeywordsSection();
        final List<RobotKeywordDefinition> keywordsToRemove = newArrayList(section.getChildren().get(1));

        final DeleteKeywordDefinitionCommand command = new DeleteKeywordDefinitionCommand(keywordsToRemove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, keywordsToRemove)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void multipleKeywordsAreProperlyRemoved() {
        final RobotKeywordsSection section = createKeywordsSection();
        final List<RobotKeywordDefinition> keywordsToRemove = newArrayList(section.getChildren().get(0),
                section.getChildren().get(2));

        final DeleteKeywordDefinitionCommand command = new DeleteKeywordDefinitionCommand(keywordsToRemove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 2");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(section.getChildren().get(0)))));
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(section.getChildren().get(2)))));
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
}
