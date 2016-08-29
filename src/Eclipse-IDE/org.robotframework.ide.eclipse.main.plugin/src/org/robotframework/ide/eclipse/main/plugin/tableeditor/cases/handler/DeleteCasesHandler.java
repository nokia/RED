/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.DeleteCasesCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.DeleteKeywordCallFromCasesCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.DeleteCasesHandler.E4DeleteCasesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteCasesHandler extends DIParameterizedHandler<E4DeleteCasesHandler> {

    public DeleteCasesHandler() {
        super(E4DeleteCasesHandler.class);
    }

    public static class E4DeleteCasesHandler {

        @Execute
        public void deleteCasesAndCalls(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {
            final List<RobotKeywordCall> keywordCalls = Selections.getElements(selection, RobotKeywordCall.class);
            final List<RobotCase> cases = Selections.getElements(selection, RobotCase.class);

            if (!keywordCalls.isEmpty()) {
                commandsStack.execute(new DeleteKeywordCallFromCasesCommand(keywordCalls));
            }
            if (!cases.isEmpty()) {
                commandsStack.execute(new DeleteCasesCommand(cases));
            }
            editor.getSelectionLayerAccessor().clear();
        }
    }
}
