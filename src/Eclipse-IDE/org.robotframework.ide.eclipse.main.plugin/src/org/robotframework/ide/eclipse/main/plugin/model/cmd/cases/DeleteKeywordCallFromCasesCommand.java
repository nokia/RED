/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteKeywordCallFromCasesCommand extends EditorCommand {

    private final List<? extends RobotKeywordCall> callsToDelete;

    public DeleteKeywordCallFromCasesCommand(final List<? extends RobotKeywordCall> callsToDelete) {
        this.callsToDelete = callsToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (callsToDelete.isEmpty()) {
            return;
        }

        final Set<RobotCase> casesWhereRemovalWasPerformed = new HashSet<>();
        
        for (final RobotKeywordCall call : callsToDelete) {
            final RobotCase parent = (RobotCase) call.getParent();
            parent.removeChild(call);

            casesWhereRemovalWasPerformed.add(parent);
        }
        for (final RobotCase testCase : casesWhereRemovalWasPerformed) {
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, testCase);
        }
    }
}
