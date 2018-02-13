/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand.CommandExecutionException;

public class RobotEditorCommandsStack {

    private static final int COMMANDS_STACK_MAX_SIZE = 100;

    private final Deque<EditorCommand> _executedCommands = new ArrayDeque<>();

    private final Deque<EditorCommand> _toRedoCommands = new ArrayDeque<>();

    public void execute(final EditorCommand command) throws CommandExecutionException {
        final IEclipseContext context = PlatformUI.getWorkbench().getService(IEclipseContext.class).getActiveLeaf();
        ContextInjectionFactory.inject(command, context);
        command.execute();

        if (_executedCommands.size() > COMMANDS_STACK_MAX_SIZE) {
            _executedCommands.removeLast();
        }

        _executedCommands.push(command);
        clear(_toRedoCommands);
    }

    public boolean isUndoPossible() {
        return !_executedCommands.isEmpty();
    }

    public void undo() {
        if (isUndoPossible()) {
            final EditorCommand commandToUndo = _executedCommands.pop();
            executeUndoCommands(commandToUndo.getUndoCommands(), _toRedoCommands);
            findAndExecuteUndoCommandsWithTheSameParent(commandToUndo, _executedCommands, _toRedoCommands);
        }
    }

    public boolean isRedoPossible() {
        return !_toRedoCommands.isEmpty();
    }

    public void redo() {
        if (isRedoPossible()) {
            final EditorCommand commandToRedo = _toRedoCommands.pop();
            executeUndoCommands(commandToRedo.getUndoCommands(), _executedCommands);
            findAndExecuteUndoCommandsWithTheSameParent(commandToRedo, _toRedoCommands, _executedCommands);
        }
    }

    public void clear() {
        clear(_toRedoCommands);
        clear(_executedCommands);
    }

    private void clear(final Deque<EditorCommand> stackToClear) {
        while (!stackToClear.isEmpty()) {
            final IEclipseContext context = PlatformUI.getWorkbench().getService(IEclipseContext.class).getActiveLeaf();
            final EditorCommand command = stackToClear.pop();
            ContextInjectionFactory.uninject(command, context);
        }
    }

    private void executeUndoCommands(final List<EditorCommand> commands,
            final Deque<EditorCommand> commandsDestinationStack) {
        for (final EditorCommand command : commands) {
            command.execute();
            commandsDestinationStack.push(command);
        }
    }

    private void findAndExecuteUndoCommandsWithTheSameParent(final EditorCommand executedCommand,
            final Deque<EditorCommand> commandsSourceStack, final Deque<EditorCommand> commandsDestinationStack) {
        boolean hasCommandsWithTheSameParent = true;
        while (hasCommandsWithTheSameParent) {
            final EditorCommand nextCommand = commandsSourceStack.peek();
            if (nextCommand != null && nextCommand.getParent() != null && executedCommand.getParent() != null
                    && nextCommand.getParent() == executedCommand.getParent()) {
                commandsSourceStack.pop();
                executeUndoCommands(nextCommand.getUndoCommands(), commandsDestinationStack);
            } else {
                hasCommandsWithTheSameParent = false;
            }
        }
    }
}
