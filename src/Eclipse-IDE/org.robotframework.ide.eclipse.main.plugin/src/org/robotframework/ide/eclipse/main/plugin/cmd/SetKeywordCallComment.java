package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

class SetKeywordCallComment extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final String comment;
    private final String topic;

    SetKeywordCallComment(final RobotKeywordCall keywordCall, final String comment, final String topic) {
        this.keywordCall = keywordCall;
        this.comment = comment;
        this.topic = topic;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (keywordCall.getComment().equals(comment)) {
            return;
        }
        keywordCall.setComment(comment);

        // it has to be send, not posted
        // otherwise it is not possible to traverse between cells, because the cell
        // is traversed and then main thread has to handle incoming posted event which
        // closes currently active cell editor
        eventBroker.send(topic, keywordCall);
    }
}
