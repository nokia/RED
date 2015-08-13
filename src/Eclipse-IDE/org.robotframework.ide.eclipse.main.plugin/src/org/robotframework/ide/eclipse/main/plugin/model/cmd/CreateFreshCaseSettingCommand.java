package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


public class CreateFreshCaseSettingCommand extends EditorCommand {

    private final RobotCase testCase;

    private final String settingName;

    private final List<String> args;

    private final int index;

    public CreateFreshCaseSettingCommand(final RobotCase testCase, final int index,
            final String settingName,
            final List<String> args) {
        this.testCase = testCase;
        this.index = index;
        this.settingName = settingName;
        this.args = args;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        testCase.createDefinitionSetting(index, settingName, args, "");

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, testCase);
    }
}
