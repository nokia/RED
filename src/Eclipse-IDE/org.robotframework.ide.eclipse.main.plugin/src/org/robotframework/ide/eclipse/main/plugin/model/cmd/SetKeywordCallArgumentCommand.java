/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallArgumentCommand extends EditorCommand {

    protected final RobotKeywordCall keywordCall;
    protected final int index;
    protected final String value;

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
        changed |= index < arguments.size() && !arguments.get(index).equals(value);
        arguments.set(index, value == null || value.isEmpty() ? "\\" : value);

        for (int i = arguments.size() - 1; i >= 0; i--) {
            if (!arguments.get(i).equals("\\")) {
                break;
            }
            arguments.set(i, null);
        }
        
        if (changed) {
            keywordCall.resetStored();
            updateModelElement(arguments);
            
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCall);
        }
    }

    protected void updateModelElement(final List<String> arguments) {
        final AModelElement<?> linkedElement = keywordCall.getLinkedElement();
        final ModelType modelType = linkedElement.getModelType();
        if (modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW || modelType == ModelType.TEST_CASE_EXECUTABLE_ROW) {
            if (value != null) {
                for (int i = arguments.size() - 1; i >= 0; i--) {
                    ((RobotExecutableRow<?>) linkedElement).setArgument(i, arguments.get(i));
                }
            } else if (index < ((RobotExecutableRow<?>) linkedElement).getArguments().size()) {
                ((RobotExecutableRow<?>) linkedElement).removeElementToken(index);
            }
        }
    }
}
