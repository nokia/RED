/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class RemoveKeywordCallArgumentCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final int index;

    public RemoveKeywordCallArgumentCommand(final RobotKeywordCall keywordCall, final int index) {
        this.keywordCall = keywordCall;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (index < keywordCall.getArguments().size()) {
            keywordCall.getArguments().remove(index);
        }
        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCall);
    }
}
