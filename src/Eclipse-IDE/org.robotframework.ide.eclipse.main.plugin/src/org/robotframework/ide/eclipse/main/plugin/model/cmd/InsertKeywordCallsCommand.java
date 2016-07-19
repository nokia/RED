/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertKeywordCallsCommand extends EditorCommand {

    private final IRobotCodeHoldingElement parent;
    private final int modelTableIndex;
    private final int codeHoldingElementIndex;
    private final List<RobotKeywordCall> callsToInsert;

    public InsertKeywordCallsCommand(final IRobotCodeHoldingElement parent, final RobotKeywordCall[] callsToInsert) {
        this(parent, -1, -1, callsToInsert);
    }

    public InsertKeywordCallsCommand(final IRobotCodeHoldingElement parent, final int modelTableIndex, final int codeHoldingElementIndex,
            final RobotKeywordCall[] callsToInsert) {
        this.parent = parent;
        this.modelTableIndex = modelTableIndex;
        this.codeHoldingElementIndex = codeHoldingElementIndex;
        this.callsToInsert = Arrays.asList(callsToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {

        final RobotCodeHoldingElement parentElement = (RobotCodeHoldingElement) parent;
        int shift = 0;
        for (final RobotKeywordCall call : callsToInsert) {
            if(call.getLinkedElement() != null) {
                parentElement.insertKeywordCall(modelTableIndex < 0 ? -1 : modelTableIndex + shift,
                        codeHoldingElementIndex < 0 ? -1 : codeHoldingElementIndex + shift, call);
            }
            shift++;
        }
 
        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }
}
