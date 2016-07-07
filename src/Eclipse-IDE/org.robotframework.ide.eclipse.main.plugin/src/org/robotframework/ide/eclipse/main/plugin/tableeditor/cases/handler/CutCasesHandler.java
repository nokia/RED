/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteCasesCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CutCasesHandler.E4CutCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutCasesHandler extends DIParameterizedHandler<E4CutCasesHandler> {

    public CutCasesHandler() {
        super(E4CutCasesHandler.class);
    }

    public static class E4CutCasesHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public void cutKeywords(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {
            final List<RobotCase> cases = Selections.getElements(selection, RobotCase.class);
            final List<RobotKeywordCall> calls = Selections.getElements(selection, RobotKeywordCall.class);

            if (!cases.isEmpty()) {
                final Object data = cases.toArray(new RobotCase[0]);
                clipboard.insertContent(data);
                commandsStack.execute(new DeleteCasesCommand(cases));

            } else if (!calls.isEmpty()) {
                final Object data = calls.toArray(new RobotKeywordCall[0]);
                clipboard.insertContent(data);
                commandsStack.execute(new DeleteKeywordCallCommand(calls));
            }
        }
    }
}
