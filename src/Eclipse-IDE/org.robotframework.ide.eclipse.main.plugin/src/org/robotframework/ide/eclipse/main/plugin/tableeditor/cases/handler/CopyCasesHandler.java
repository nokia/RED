/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CopyCasesHandler.E4CopyCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.E4CopyCodeHoldersHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyCasesHandler extends DIParameterizedHandler<E4CopyCasesHandler> {

    public CopyCasesHandler() {
        super(E4CopyCasesHandler.class);
    }

    public static class E4CopyCasesHandler extends E4CopyCodeHoldersHandler {

        @Execute
        public boolean copyCases(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {
            return copyCodeHolders(selection, clipboard);
        }

        @Override
        protected Class<? extends RobotCodeHoldingElement<?>> getCodeHolderClass() {
            return RobotCase.class;
        }
    }
}
