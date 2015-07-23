package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordDefinitionNameCommand extends EditorCommand {

    private final RobotKeywordDefinition definition;
    private final String name;

    public SetKeywordDefinitionNameCommand(final RobotKeywordDefinition definition, final String name) {
        this.definition = definition;
        this.name = name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (definition.getName().equals(name)) {
            return;
        }
        definition.setName(name);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_NAME_CHANGE, definition);
    }
}
