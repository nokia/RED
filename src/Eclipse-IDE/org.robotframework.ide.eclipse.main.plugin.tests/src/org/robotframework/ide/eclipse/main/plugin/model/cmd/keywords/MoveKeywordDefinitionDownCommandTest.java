/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelFunctions.toNames;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinitionConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveKeywordDefinitionDownCommandTest {

    @Test
    public void nothingHappens_whenTryingToMoveKeywordWhichIsAlreadyTheLastOne() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition keywordToMove = section.getChildren().get(section.getChildren().size() - 1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordDefinitionDownCommand command = new MoveKeywordDefinitionDownCommand(keywordToMove);
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
    public void keywordIsProperlyMovedDown_whenTryingToMoveNonLastKeyword() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition keywordToMove = section.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordDefinitionDownCommand command = new MoveKeywordDefinitionDownCommand(keywordToMove);
        command.setEventBroker(eventBroker);
        command.execute();

        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 3", "kw 2");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent());

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_MOVED, section);
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
