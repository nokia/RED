package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetCaseCommentCommand extends EditorCommand {

    private final RobotCase testCase;
    private final String newComment;

    public SetCaseCommentCommand(final RobotCase testCase, final String comment) {
        this.testCase = testCase;
        this.newComment = comment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (testCase.getComment().equals(newComment)) {
            return;
        }
        testCase.setComment(newComment);

        // it has to be send, not posted
        // otherwise it is not possible to traverse between cells, because the
        // cell
        // is traversed and then main thread has to handle incoming posted event
        // which
        // closes currently active cell editor
        eventBroker.send(RobotModelEvents.ROBOT_CASE_COMMENT_CHANGE, testCase);
    }
}
