package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateSettingKeywordCall extends EditorCommand {

    private final RobotSuiteSettingsSection settingsSection;
    private final String keywordName;
    private final String[] args;

    public CreateSettingKeywordCall(final RobotSuiteSettingsSection settingsSection, final String keywordName,
            final String... args) {
        this.settingsSection = settingsSection;
        this.keywordName = keywordName;
        this.args = args;
    }

    @Override
    public void execute() throws CommandExecutionException {
        settingsSection.createSetting(keywordName, "", args);

        eventBroker.post(RobotModelEvents.ROBOT_SETTING_ADDED, settingsSection);
    }
}
