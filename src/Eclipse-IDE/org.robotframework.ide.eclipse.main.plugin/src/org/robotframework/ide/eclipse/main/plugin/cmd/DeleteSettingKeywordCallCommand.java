package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteSettingKeywordCallCommand extends EditorCommand {

    private final List<RobotSetting> settingsToRemove;

    public DeleteSettingKeywordCallCommand(final List<RobotSetting> settingsToRemove) {
        this.settingsToRemove = settingsToRemove;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (settingsToRemove.isEmpty()) {
            return;
        }
        final RobotSuiteFileSection settingsSection = (RobotSuiteFileSection) settingsToRemove.get(0).getParent();
        settingsSection.getChildren().removeAll(settingsToRemove);

        eventBroker.send(RobotModelEvents.ROBOT_SETTING_REMOVED, settingsSection);
    }
}
