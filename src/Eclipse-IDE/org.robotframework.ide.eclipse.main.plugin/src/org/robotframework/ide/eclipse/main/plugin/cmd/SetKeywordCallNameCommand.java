package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallNameCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final String name;

    public SetKeywordCallNameCommand(final RobotKeywordCall keywordCall, final String name) {
        this.keywordCall = keywordCall;
        this.name = name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (keywordCall.getName().equals(name)) {
            return;
        }
        keywordCall.setName(name);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, keywordCall);
    }
}
