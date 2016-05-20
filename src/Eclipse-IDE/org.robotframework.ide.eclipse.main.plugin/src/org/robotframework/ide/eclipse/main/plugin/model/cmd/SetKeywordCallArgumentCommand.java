/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.SettingTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallArgumentCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final int index;
    private final String value;

    public SetKeywordCallArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value) {
        this.keywordCall = keywordCall;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<String> arguments = keywordCall.getArguments();
        boolean changed = false;

        for (int i = arguments.size(); i <= index; i++) {
            arguments.add("");
            changed = true;
        }
        if (!arguments.get(index).equals(value)) {
            arguments.remove(index);
            arguments.add(index, value);
            changed = true;
        }
        if (changed) {
            new SettingTableModelUpdater().update(keywordCall.getLinkedElement(), index, value);
            // it has to be send, not posted
            // otherwise it is not possible to traverse between cells, because the cell
            // is traversed and then main thread has to handle incoming posted event which
            // closes currently active cell editor
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCall);
        }
    }
}
