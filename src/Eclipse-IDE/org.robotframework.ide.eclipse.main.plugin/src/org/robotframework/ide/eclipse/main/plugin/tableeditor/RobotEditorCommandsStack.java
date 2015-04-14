package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.Stack;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand.CommandExecutionException;

public class RobotEditorCommandsStack {

    private final IEclipseContext context;

    private final Stack<EditorCommand> _executedCommands = new Stack<>();

    private final Stack<EditorCommand> _toRedoCommands = new Stack<>();

    
    public RobotEditorCommandsStack(final IEclipseContext context) {
        this.context = context;
    }
    
    public void execute(final EditorCommand command) throws CommandExecutionException {
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
            final EditorCommand command = stackToClear.pop();
            ContextInjectionFactory.uninject(command, context);
        }
    }
}
