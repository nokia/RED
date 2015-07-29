package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetSettingKeywordCallCommand extends EditorCommand {

    private final RobotSetting setting;
    private final List<String> args;

    public SetSettingKeywordCallCommand(RobotSetting setting, final List<String> args) {
        this.setting = setting;
        this.args = args;
    }


    @Override
    public void execute() throws CommandExecutionException {
        setting.setArgs(args);
        final RobotSettingsSection settingsSection = (RobotSettingsSection) setting.getParent();
        eventBroker.send(RobotModelEvents.ROBOT_SETTING_CHANGED, settingsSection);
    }
}
