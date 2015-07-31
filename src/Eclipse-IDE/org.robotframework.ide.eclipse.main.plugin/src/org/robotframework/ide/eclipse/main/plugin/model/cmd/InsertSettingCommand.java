package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertSettingCommand extends EditorCommand {

    private final IRobotCodeHoldingElement parent;
    private final int index;
    private final List<RobotKeywordCall> settingsToInsert;

    public InsertSettingCommand(final IRobotCodeHoldingElement parent, final RobotKeywordCall[] settingsToInsert) {
        this(parent, -1, settingsToInsert);
    }

    public InsertSettingCommand(final IRobotCodeHoldingElement parent, final int index,
            final RobotKeywordCall[] settingsToInsert) {
        this.parent = parent;
        this.index = index;
        this.settingsToInsert = Arrays.asList(settingsToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {
        for (final RobotKeywordCall call : settingsToInsert) {
            call.setParent(parent);
        }
        if (index == -1) {
            parent.getChildren().addAll(settingsToInsert);
        } else {
            parent.getChildren().addAll(index, settingsToInsert);
        }

        eventBroker.post(RobotModelEvents.ROBOT_SETTING_ADDED, parent);
    }
}
