/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noChildren;
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

public class CreateFreshKeywordDefinitionCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void whenCommandIsUsedWithoutIndex_newKeywordIsProperlyAddedAtTheEnd() {
        final RobotKeywordsSection section = createKeywordsSection();

        final CreateFreshKeywordDefinitionCommand command = new CreateFreshKeywordDefinitionCommand(section);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("Keyword 1", "Keyword 2", "Keyword 3",
                "Keyword 4");

        final RobotKeywordDefinition addedKeyword = section.getChildren().get(3);
        assertThat(addedKeyword).has(RobotKeywordDefinitionConditions.properlySetParent())
                .has(name("Keyword 4"))
                .has(noChildren());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("Keyword 1", "Keyword 2", "Keyword 3");

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedKeyword)));
        verify(eventBroker).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void whenCommandIsUsedWithIndex_newKeywordIsProperlyAddedAtSpecifiedPlace() {
        final RobotKeywordsSection section = createKeywordsSection();

        final CreateFreshKeywordDefinitionCommand command = new CreateFreshKeywordDefinitionCommand(section, 1);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("Keyword 1", "Keyword 4", "Keyword 2",
                "Keyword 3");

        final RobotKeywordDefinition addedKeyword = section.getChildren().get(1);
        assertThat(addedKeyword).has(RobotKeywordDefinitionConditions.properlySetParent())
                .has(name("Keyword 4"))
                .has(noChildren());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("Keyword 1", "Keyword 2", "Keyword 3");

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, addedKeyword)));
        verify(eventBroker).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    private static RobotKeywordsSection createKeywordsSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("Keyword 1")
                .appendLine("  Log  10")
                .appendLine("Keyword 2")
                .appendLine("  Log  10")
                .appendLine("Keyword 3")
                .appendLine("  Log  10")
                .build();
        return model.findSection(RobotKeywordsSection.class).get();
    }
}
