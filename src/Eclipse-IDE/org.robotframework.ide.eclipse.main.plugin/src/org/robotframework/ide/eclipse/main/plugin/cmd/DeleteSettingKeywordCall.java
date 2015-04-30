package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteSettingKeywordCall extends EditorCommand {

    private final List<RobotSetting> settingsToRemove;

    public DeleteSettingKeywordCall(final List<RobotSetting> settingsToRemove) {
        this.settingsToRemove = settingsToRemove;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (settingsToRemove.isEmpty()) {
            return;
        }
        final RobotSuiteFileSection settingsSection = (RobotSuiteFileSection) settingsToRemove.get(0).getParent();
        settingsSection.getChildren().removeAll(settingsToRemove);

        eventBroker.post(RobotModelEvents.ROBOT_SETTING_REMOVED, settingsSection);
    }
}
