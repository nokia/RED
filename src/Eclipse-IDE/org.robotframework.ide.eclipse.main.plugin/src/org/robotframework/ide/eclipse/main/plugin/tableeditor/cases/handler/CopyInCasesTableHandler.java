/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CopyInCasesTableHandler.E4CopyInCasesTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.E4CopyInCodeHoldersTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyInCasesTableHandler extends DIParameterizedHandler<E4CopyInCasesTableHandler> {

    public CopyInCasesTableHandler() {
        super(E4CopyInCasesTableHandler.class);
    }

    public static class E4CopyInCasesTableHandler extends E4CopyInCodeHoldersTableHandler {

        @Execute
        public boolean copyContent(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {
            return copyContent(editor.getSelectionLayerAccessor(), selection, clipboard);
        }

        @Override
        protected Class<? extends RobotCodeHoldingElement<?>> getCodeHolderClass() {
            return RobotCase.class;
        }
    }
}
