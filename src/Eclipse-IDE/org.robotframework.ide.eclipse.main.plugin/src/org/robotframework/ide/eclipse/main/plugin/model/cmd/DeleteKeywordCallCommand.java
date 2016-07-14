/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.KeywordTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteKeywordCallCommand extends EditorCommand {

    private final List<? extends RobotKeywordCall> callsToDelete;

    public DeleteKeywordCallCommand(final List<? extends RobotKeywordCall> callsToDelete) {
        this.callsToDelete = callsToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (callsToDelete.isEmpty()) {
            return;
        }
        IRobotCodeHoldingElement parent = callsToDelete.get(0).getParent();

        parent.getChildren().removeAll(callsToDelete);

        removeModelElements(parent);

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, parent);
    }

    private void removeModelElements(IRobotCodeHoldingElement parent) {
        final Object linkedElement = parent.getLinkedElement();
        if (linkedElement != null && linkedElement instanceof UserKeyword) {
            final UserKeyword userKeyword = (UserKeyword) linkedElement;
            for (RobotKeywordCall robotKeywordCall : callsToDelete) {
                final AModelElement<?> modelElement = robotKeywordCall.getLinkedElement();
                if (modelElement.getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
                    userKeyword.removeExecutableRow((RobotExecutableRow<UserKeyword>) modelElement);
                } else {
                    new KeywordTableModelUpdater().remove(userKeyword, modelElement);
                }
            }
        }
    }
}
