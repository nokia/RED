/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallArgumentCommandTest {

    @Test
    public void testUndoRedoOnFirstArgumentInKeywordCall() {
        RobotKeywordCall call = createKeywordCall();
        SetKeywordCallArgumentCommand command = new SetKeywordCallArgumentCommand(call, 0, null);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

        verifyArguments(call, 2, 0, "2");

        EditorCommand undoCommand = command.getUndoCommand();
        undoCommand.execute();
        verifyArguments(call, 3, 0, "1");

        EditorCommand redoCommand = undoCommand.getUndoCommand();
        redoCommand.execute();
        verifyArguments(call, 2, 0, "2");

        verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
    }

    @Test
    public void testUndoRedoOnFirstArgumentInKeywordBasedSetting() {
        RobotKeywordCall call = createKeywordBasedSetting();
        SetKeywordCallArgumentCommand command = new SetKeywordCallArgumentCommand(call, 0, null);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

        verifyArguments(call, 3, 0, "");

        EditorCommand undoCommand = command.getUndoCommand();
        undoCommand.execute();
        verifyArguments(call, 3, 0, "keyword");

        EditorCommand redoCommand = undoCommand.getUndoCommand();
        redoCommand.execute();
        verifyArguments(call, 3, 0, "\\");

        verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
    }

    @Test
    public void testUndoRedoOnSecondArgumentInKeywordBasedSetting() {
        RobotKeywordCall call = createKeywordBasedSetting();
        SetKeywordCallArgumentCommand command = new SetKeywordCallArgumentCommand(call, 1, null);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

        verifyArguments(call, 2, 1, "2");

        EditorCommand undoCommand = command.getUndoCommand();
        undoCommand.execute();
        verifyArguments(call, 3, 1, "1");

        EditorCommand redoCommand = undoCommand.getUndoCommand();
        redoCommand.execute();
        verifyArguments(call, 2, 1, "2");

        verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
    }
    
    @Test
    public void testUndoRedoOnFirstArgumentInNotKeywordBasedSetting() {
        RobotKeywordCall call = createNotKeywordBasedSetting();
        SetKeywordCallArgumentCommand command = new SetKeywordCallArgumentCommand(call, 0, null);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

        verifyArguments(call, 2, 0, "T2");

        EditorCommand undoCommand = command.getUndoCommand();
        undoCommand.execute();
        verifyArguments(call, 3, 0, "T1");

        EditorCommand redoCommand = undoCommand.getUndoCommand();
        redoCommand.execute();
        verifyArguments(call, 2, 0, "T2");

        verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
    }
    
    @Test
    public void testUndoRedoOnAllArgumentsOneByOne() {
        RobotKeywordCall call = createKeywordCall();
        SetKeywordCallArgumentCommand command1 = new SetKeywordCallArgumentCommand(call, 0, null);
        SetKeywordCallArgumentCommand command2 = new SetKeywordCallArgumentCommand(call, 0, null);
        SetKeywordCallArgumentCommand command3 = new SetKeywordCallArgumentCommand(call, 0, null);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command1).execute();
        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command2).execute();
        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command3).execute();
        
        assertTrue(call.getArguments().isEmpty());
        
        EditorCommand undoCommand1 = command3.getUndoCommand();
        undoCommand1.execute();
        verifyArguments(call, 1, 0, "3");
        
        EditorCommand undoCommand2 = command2.getUndoCommand();
        undoCommand2.execute();
        verifyArguments(call, 2, 0, "2");
        
        EditorCommand undoCommand3 = command1.getUndoCommand();
        undoCommand3.execute();
        verifyArguments(call, 3, 0, "1");
        verifyArguments(call, 3, 1, "2");
        verifyArguments(call, 3, 2, "3");
        
        EditorCommand redoCommand1 = undoCommand3.getUndoCommand();
        redoCommand1.execute();
        verifyArguments(call, 2, 0, "2");
        
        EditorCommand redoCommand2 = undoCommand2.getUndoCommand();
        redoCommand2.execute();
        verifyArguments(call, 1, 0, "3");
        
        EditorCommand redoCommand3 = undoCommand1.getUndoCommand();
        redoCommand3.execute();
        assertTrue(call.getArguments().isEmpty());
    }

    private void verifyArguments(RobotKeywordCall call, int expectedSize, int indexToVerify, String expectedValue) {
        assertTrue(call.getArguments().size() == expectedSize);
        assertThat(call.getArguments().get(indexToVerify)).isEqualTo(expectedValue);
    }

    private static RobotKeywordCall createKeywordCall() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  call  1  2  3  # comment")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0).getChildren().get(0);
    }

    private static RobotKeywordCall createKeywordBasedSetting() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]  keyword  1  2  # comment")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0).getChildren().get(0);
    }
    
    private static RobotKeywordCall createNotKeywordBasedSetting() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Tags]  T1  T2  T3  # comment")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0).getChildren().get(0);
    }

}
