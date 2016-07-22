/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.SettingTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
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
            arguments.add("\\");
            changed = true;
        }
        if (!arguments.get(index).equals(value)) {
            arguments.remove(index);
            if (value != null) {
                arguments.add(index, value);
            } 
            changed = true;
        }
        if (changed) {
            updateModelElement();
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCall);
        }
    }

    protected void updateModelElement() {
        final AModelElement<?> linkedElement = keywordCall.getLinkedElement();
        final ModelType modelType = linkedElement.getModelType();
        if (modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW || modelType == ModelType.TEST_CASE_EXECUTABLE_ROW) {
            if(value != null) {
                ((RobotExecutableRow<?>) linkedElement).setArgument(index, value);
            } else if (index < ((RobotExecutableRow<?>) linkedElement).getArguments().size()) {
                ((RobotExecutableRow<?>) linkedElement).removeElementToken(index);
            }
        } else {
            new SettingTableModelUpdater().update(linkedElement, index, value);
        }
    }

    public int getIndex() {
        return index;
    }

    protected RobotKeywordCall getKeywordCall() {
        return keywordCall;
    }

    protected String getValue() {
        return value;
    }
}
