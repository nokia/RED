package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


public class MoveKeywordCallUpCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    public MoveKeywordCallUpCommand(final RobotKeywordCall keywordCall) {
        this.keywordCall = keywordCall;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotElement keywordDef = keywordCall.getParent();
        final int index = keywordDef.getChildren().indexOf(keywordCall);
        if (index == 0) {
            // lets try to move to keyword definition up from here
            final int indexOfDef = keywordDef.getParent().getChildren().indexOf(keywordDef);
            if (indexOfDef == 0) {
                return;
            }
            final RobotElement targetDef = keywordDef.getParent().getChildren().get(indexOfDef - 1);

            keywordDef.getChildren().remove(index);
            targetDef.getChildren().add(keywordCall);
            keywordCall.setParent(targetDef);

            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, targetDef);
            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, keywordDef);

            return;
        }
        Collections.swap(keywordDef.getChildren(), index, index - 1);

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, keywordDef);
    }
}
