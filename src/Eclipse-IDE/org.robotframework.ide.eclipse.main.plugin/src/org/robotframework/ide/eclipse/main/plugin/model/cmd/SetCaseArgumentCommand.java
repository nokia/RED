package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetCaseArgumentCommand extends EditorCommand {

    private final RobotCase testCase;
    private final int index;
    private final String value;

    public SetCaseArgumentCommand(final RobotCase testCase, final int index, final String value) {
        this.testCase = testCase;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<String> arguments = testCase.getArguments();
        boolean changed = false;

        for (int i = arguments.size(); i <= index; i++) {
            arguments.add("");
            changed = true;
        }
        if (!arguments.get(index).equals(value)) {
            arguments.remove(index);
            arguments.add(index, value);
            changed = true;
        }
        if (changed) {
            // it has to be send, not posted
            // otherwise it is not possible to traverse between cells, because the cell
            // is traversed and then main thread has to handle incoming posted event which
            // closes currently active cell editor
            eventBroker.send(RobotModelEvents.ROBOT_CASE_ARGUMENT_CHANGE, testCase);
        }
    }
}
