package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotCollectionElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveValueUpCommand extends EditorCommand {

    RobotCollectionElement selectedElement;
    
    public MoveValueUpCommand(final RobotCollectionElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    @Override
    public void execute() throws CommandExecutionException {
        //selectedElement.getFormDialog().moveElementUp(selectedElement);

    }
}
