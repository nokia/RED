package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetVariableCommentCommand extends EditorCommand {

    private final RobotVariable variable;
    private final String newComment;

    public SetVariableCommentCommand(final RobotVariable variable, final String newComment) {
        this.variable = variable;
        this.newComment = newComment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (variable.getComment().equals(newComment)) {
            return;
        }
        variable.setComment(newComment);
        
        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_COMMENT_CHANGE, variable);
    }
}
