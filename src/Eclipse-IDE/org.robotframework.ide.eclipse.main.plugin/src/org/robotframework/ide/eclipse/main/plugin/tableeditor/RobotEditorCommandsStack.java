/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.Stack;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand.CommandExecutionException;

public class RobotEditorCommandsStack {

    private final Stack<EditorCommand> _executedCommands = new Stack<>();

    private final Stack<EditorCommand> _toRedoCommands = new Stack<>();

    public void execute(final EditorCommand command) throws CommandExecutionException {
        final IEclipseContext context = ((IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class))
                .getActiveLeaf();
        ContextInjectionFactory.inject(command, context);
        command.execute();
        
        _executedCommands.push(command);
        clear(_toRedoCommands);
    }
    
    public boolean isUndoPossible() {
        return !_executedCommands.isEmpty();
    }
    
    public void undo() {
        if (isUndoPossible()) {
            final EditorCommand commandToUndo = _executedCommands.pop();
            commandToUndo.execute();
            _toRedoCommands.push(commandToUndo);
        }
    }
    
    public boolean isRedoPossible() {
        return !_toRedoCommands.isEmpty();
    }

    public void redo() {
        if (isRedoPossible()) {
            final EditorCommand commandToRedo = _toRedoCommands.pop();
            commandToRedo.execute();
            _executedCommands.push(commandToRedo);
        }
    }

    public void clear() {
        clear(_toRedoCommands);
        clear(_executedCommands);
    }

    private void clear(final Stack<EditorCommand> stackToClear) {
        while (!stackToClear.isEmpty()) {
            final IEclipseContext context = ((IEclipseContext) PlatformUI.getWorkbench()
                    .getService(IEclipseContext.class)).getActiveLeaf();
            final EditorCommand command = stackToClear.pop();
            ContextInjectionFactory.uninject(command, context);
        }
    }
}
