package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertKeywordCallArgumentCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final int index;
    private final String argument;

    public InsertKeywordCallArgumentCommand(final RobotKeywordCall keywordCall, final int index,
            final String argument) {
        this.keywordCall = keywordCall;
        this.index = index;
        this.argument = argument;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<String> arguments = keywordCall.getArguments();

        for (int i = arguments.size(); i < index; i++) {
            arguments.add("");
        }
        arguments.add(index, argument);
        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCall);
    }
}
