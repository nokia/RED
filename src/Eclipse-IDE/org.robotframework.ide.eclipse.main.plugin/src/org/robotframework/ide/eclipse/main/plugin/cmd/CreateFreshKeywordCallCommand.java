package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.ArrayList;

import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshKeywordCallCommand extends EditorCommand {

    private final RobotCase testCase;
    private final int index;

    public CreateFreshKeywordCallCommand(final RobotCase testCase) {
        this(testCase, -1);
    }

    public CreateFreshKeywordCallCommand(final RobotCase testCase, final int index) {
        this.testCase = testCase;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotKeywordCall keywordCall = new RobotKeywordCall(testCase, "", new ArrayList<String>(), "");
        if (index == -1) {
            testCase.getChildren().add(keywordCall);
        } else {
            testCase.getChildren().add(index, keywordCall);
        }

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, testCase);
    }
}
