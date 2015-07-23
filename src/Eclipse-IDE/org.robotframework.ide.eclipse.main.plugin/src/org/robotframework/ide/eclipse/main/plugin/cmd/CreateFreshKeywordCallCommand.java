package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.ArrayList;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshKeywordCallCommand extends EditorCommand {

    private final RobotElement parent;
    private final int index;
    private final boolean notifySync;

    public CreateFreshKeywordCallCommand(final RobotElement parent, final boolean notifySync) {
        this(parent, -1, notifySync);
    }

    public CreateFreshKeywordCallCommand(final RobotElement parent, final int index, final boolean notifySync) {
        this.parent = parent;
        this.index = index;
        this.notifySync = notifySync;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotKeywordCall keywordCall = new RobotKeywordCall(parent, "", new ArrayList<String>(), "");
        if (index == -1) {
            parent.getChildren().add(keywordCall);
        } else {
            parent.getChildren().add(index, keywordCall);
        }

        if (notifySync) {
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
        } else {
            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
        }
    }
}
