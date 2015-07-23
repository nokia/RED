package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
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
