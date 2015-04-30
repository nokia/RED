package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

abstract class SetKeywordCallArgument extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final int index;
    private final String value;
    private final String topic;

    SetKeywordCallArgument(final RobotKeywordCall keywordCall, final int index, final String value, final String topic) {
        this.keywordCall = keywordCall;
        this.index = index;
        this.value = value;
        this.topic = topic;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<String> arguments = keywordCall.getArguments();
        boolean changed = false;

        int i = arguments.size();
        while (i <= index) {
            arguments.add("");
            changed = true;
            i++;
        }
        if (!arguments.get(index).equals(value)) {
            arguments.remove(index);
            arguments.add(index, value);
            changed = true;
        }
        if (changed) {
            eventBroker.post(topic, keywordCall);
        }
    }
}
