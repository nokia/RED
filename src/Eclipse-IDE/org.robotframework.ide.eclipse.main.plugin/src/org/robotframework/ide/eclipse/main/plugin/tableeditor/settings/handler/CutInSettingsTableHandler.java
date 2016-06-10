/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CutSettingsHandler.E4CutSettingsHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CutInSettingsTableHandler.E4CutInSettingsTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutInSettingsTableHandler extends DIParameterizedHandler<E4CutInSettingsTableHandler> {

    public CutInSettingsTableHandler() {
        super(E4CutInSettingsTableHandler.class);
    }

    public static class E4CutInSettingsTableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object cut(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final Clipboard clipboard) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();

            if (selectionLayerAccessor.onlyFullRowsAreSelected()) {
                final E4CutSettingsHandler cutHandler = new E4CutSettingsHandler();
                cutHandler.cutKeywords(commandsStack, selection, clipboard);
            } else {

            }

            return null;
        }
    }
}
