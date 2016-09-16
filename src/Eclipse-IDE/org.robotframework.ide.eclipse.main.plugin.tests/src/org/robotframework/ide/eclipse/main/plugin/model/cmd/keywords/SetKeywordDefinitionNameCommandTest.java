/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordDefinitionNameCommandTest {

    @Test
    public void nothingHappens_whenNewNameIsEqualToOldOne() {
        final RobotKeywordDefinition keyword = createKeyword("kw 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetKeywordDefinitionNameCommand command = new SetKeywordDefinitionNameCommand(keyword, "kw 1");
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(keyword.getName()).isEqualTo("kw 1");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(keyword.getName()).isEqualTo("kw 1");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nameIsProperlyChanged_whenNewNameIsDifferentThanOldOne() {
        final RobotKeywordDefinition keyword = createKeyword("kw 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetKeywordDefinitionNameCommand command = new SetKeywordDefinitionNameCommand(keyword, "new kw");
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(keyword.getName()).isEqualTo("new kw");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(keyword.getName()).isEqualTo("kw 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_NAME_CHANGE, keyword);
    }

    @Test
    public void nameBecomesBackslash_whenTryingToSetNull() {
        final RobotKeywordDefinition keyword = createKeyword("kw 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetKeywordDefinitionNameCommand command = new SetKeywordDefinitionNameCommand(keyword, null);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(keyword.getName()).isEqualTo("\\");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(keyword.getName()).isEqualTo("kw 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_NAME_CHANGE, keyword);
    }

    @Test
    public void nameBecomesBackslash_whenTryingToSetEmptyName() {
        final RobotKeywordDefinition keyword = createKeyword("kw 1");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetKeywordDefinitionNameCommand command = new SetKeywordDefinitionNameCommand(keyword, "");
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(keyword.getName()).isEqualTo("\\");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(keyword.getName()).isEqualTo("kw 1");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_NAME_CHANGE, keyword);
    }

    private static RobotKeywordDefinition createKeyword(final String kwName) {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine(kwName)
                .appendLine("  Log  10")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
    }
}
