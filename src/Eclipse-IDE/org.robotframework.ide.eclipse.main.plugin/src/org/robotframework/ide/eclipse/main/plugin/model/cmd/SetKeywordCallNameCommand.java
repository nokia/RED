/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallNameCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final String name;

    public SetKeywordCallNameCommand(final RobotKeywordCall keywordCall, final String name) {
        this.keywordCall = keywordCall;
        this.name = name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (keywordCall.getName().equals(name)) {
            return;
        }
        keywordCall.setName(name);

        final RobotExecutableRow<?> exeRow = (RobotExecutableRow<?>) keywordCall.getLinkedElement();
        final RobotToken actionToken = new RobotToken();
        actionToken.setText(name);
        exeRow.setAction(actionToken);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, keywordCall);
    }
}
