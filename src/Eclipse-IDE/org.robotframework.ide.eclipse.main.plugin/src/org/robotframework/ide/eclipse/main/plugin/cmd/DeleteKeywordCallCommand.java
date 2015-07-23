package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteKeywordCallCommand extends EditorCommand {

    private final List<RobotKeywordCall> callsToDelete;

    public DeleteKeywordCallCommand(final List<RobotKeywordCall> callsToDelete) {
        this.callsToDelete = callsToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (callsToDelete.isEmpty()) {
            return;
        }
        final RobotElement parent = callsToDelete.get(0).getParent();

        parent.getChildren().removeAll(callsToDelete);

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, parent);
    }
}
