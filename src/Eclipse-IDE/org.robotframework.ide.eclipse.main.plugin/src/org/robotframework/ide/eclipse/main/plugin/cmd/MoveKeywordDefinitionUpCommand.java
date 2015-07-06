package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveKeywordDefinitionUpCommand extends EditorCommand {

    private final RobotKeywordDefinition keywordDef;

    public MoveKeywordDefinitionUpCommand(final RobotKeywordDefinition keywordDef) {
        this.keywordDef = keywordDef;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotElement section = keywordDef.getParent();
        final int index = section.getChildren().indexOf(keywordDef);
        if (index == 0) {
            return;
        }
        Collections.swap(section.getChildren(), index, index - 1);

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_MOVED, section);
    }

}
