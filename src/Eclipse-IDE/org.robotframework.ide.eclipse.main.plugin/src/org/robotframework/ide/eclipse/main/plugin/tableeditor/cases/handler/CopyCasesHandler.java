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
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CopyCasesHandler.E4CopyCasesHandlerr;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.CasesTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordCallsTransfer;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyCasesHandler extends DIParameterizedHandler<E4CopyCasesHandlerr> {

    public CopyCasesHandler() {
        super(E4CopyCasesHandlerr.class);
    }

    public static class E4CopyCasesHandlerr {

        @Execute
        public Object copyKeywords(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final Clipboard clipboard) {

            final List<RobotCase> cases = Selections.getElements(selection, RobotCase.class);
            final List<RobotKeywordCall> calls = Selections.getElements(selection, RobotKeywordCall.class);
            if (!cases.isEmpty()) {
                clipboard.setContents(
                        new RobotCase[][] { cases.toArray(new RobotCase[cases.size()]) },
                        new Transfer[] { CasesTransfer.getInstance() });
            } else if (!calls.isEmpty()) {
                clipboard.setContents(new RobotKeywordCall[][] { calls.toArray(new RobotKeywordCall[calls.size()]) },
                        new Transfer[] { KeywordCallsTransfer.getInstance() });
            }
            return null;
        }
    }
}
