package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.ArrayList;

import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshKeywordDefinitionCommand extends EditorCommand {

    private static final String DEFAULT_NAME = "Keyword";
    private final RobotKeywordsSection keywordsSection;
    private final int index;
    private final boolean notifySync;

    public CreateFreshKeywordDefinitionCommand(final RobotKeywordsSection keywordsSection,
            final boolean notifySynchronously) {
        this(keywordsSection, -1, notifySynchronously);
    }

    public CreateFreshKeywordDefinitionCommand(final RobotKeywordsSection keywordsSection, final int index) {
        this(keywordsSection, index, false);
    }

    private CreateFreshKeywordDefinitionCommand(final RobotKeywordsSection keywordsSection, final int index,
            final boolean notifySynchronously) {
        this.keywordsSection = keywordsSection;
        this.index = index;
        this.notifySync = notifySynchronously;
    }

    @Override
    public void execute() throws CommandExecutionException {

        final RobotKeywordDefinition definition = new RobotKeywordDefinition(keywordsSection, DEFAULT_NAME,
                new ArrayList<String>(), "");
        if (index == -1) {
            keywordsSection.getChildren().add(definition);
        } else {
            keywordsSection.getChildren().add(index, definition);
        }

        if (notifySync) {
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED, keywordsSection);
        } else {
            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED, keywordsSection);
        }
    }
}
