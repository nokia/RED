/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordDefinitionArgumentCommandTest {

    @Test
    public void testUndoRedoOnFirstArgumentInKeywordDefinition() {
        final RobotKeywordDefinition def = createKeywordDef();
        final RobotKeywordCall args = def.getChildren().get(0);
        final SetKeywordDefinitionArgumentCommand command = new SetKeywordDefinitionArgumentCommand(def, 0, null);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

        verifyArguments(def, 2, 0, "2");

        final EditorCommand undoCommand = command.getUndoCommands().get(0);
        undoCommand.execute();
        verifyArguments(def, 3, 0, "1");

        final EditorCommand redoCommand = undoCommand.getUndoCommands().get(0);
        redoCommand.execute();
        verifyArguments(def, 2, 0, "2");

        verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, args);
    }

    private void verifyArguments(final RobotKeywordDefinition def, final int expectedSize, final int indexToVerify,
            final String expectedValue) {
        assertTrue(def.getArgumentsSetting().getArguments().size() == expectedSize);
        assertThat(def.getArgumentsSetting().getArguments().get(indexToVerify)).isEqualTo(expectedValue);
    }

    private static RobotKeywordDefinition createKeywordDef() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  1  2  3")
                .appendLine("  call  1  # comment")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
    }

}
