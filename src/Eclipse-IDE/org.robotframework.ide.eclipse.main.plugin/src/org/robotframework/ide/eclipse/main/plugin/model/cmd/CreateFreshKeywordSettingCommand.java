package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


public class CreateFreshKeywordSettingCommand extends EditorCommand {

    private final RobotKeywordDefinition definition;

    private final String settingName;

    private final List<String> args;

    public CreateFreshKeywordSettingCommand(final RobotKeywordDefinition definition, final String settingName,
            final List<String> args) {
        this.definition = definition;
        this.settingName = settingName;
        this.args = args;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        definition.createDefinitionSetting(settingName, args, "");

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, definition);
    }
}
