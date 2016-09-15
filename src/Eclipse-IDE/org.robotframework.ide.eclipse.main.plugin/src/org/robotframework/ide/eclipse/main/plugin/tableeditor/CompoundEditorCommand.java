package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.List;

public class CompoundEditorCommand extends EditorCommand {

    private List<EditorCommand> commands;

    private EditorCommand undoCommand;

    public CompoundEditorCommand(final EditorCommand undoCommand, final List<EditorCommand> commands) {
        this.undoCommand = undoCommand;
        this.commands = commands;
    }

    @Override
    public void execute() {
        for (final EditorCommand cmd : commands) {
            cmd.execute();
        }
    }

    @Override
    public EditorCommand getUndoCommand() {
        return undoCommand;
    }

    public List<EditorCommand> getCommands() {
        return commands;
    }

}
