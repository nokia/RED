package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateSettingKeywordCallCommand extends EditorCommand {

    private final RobotSuiteSettingsSection settingsSection;
    private final String keywordName;
    private final List<String> args;
    private final String comment;

    public CreateSettingKeywordCallCommand(final RobotSuiteSettingsSection settingsSection, final String keywordName,
            final List<String> args) {
        this(settingsSection, keywordName, args, "");
    }

    public CreateSettingKeywordCallCommand(final RobotSuiteSettingsSection settingsSection, final String keywordName,
            final List<String> args, final String comment) {
        this.settingsSection = settingsSection;
        this.keywordName = keywordName;
        this.args = args;
        this.comment = comment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        settingsSection.createSetting(keywordName, comment, args.toArray(new String[0]));

        eventBroker.send(RobotModelEvents.ROBOT_SETTING_ADDED, settingsSection);
    }
}
