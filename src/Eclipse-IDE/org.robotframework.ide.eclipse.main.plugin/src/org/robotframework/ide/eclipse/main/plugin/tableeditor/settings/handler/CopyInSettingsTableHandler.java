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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CopyInSettingsTableHandler.E4CopyInSettingsTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CopySettingsHandler.E4CopySettingsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyInSettingsTableHandler extends DIParameterizedHandler<E4CopyInSettingsTableHandler> {

    public CopyInSettingsTableHandler() {
        super(E4CopyInSettingsTableHandler.class);
    }

    public static class E4CopyInSettingsTableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object copy(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final Clipboard clipboard) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();

            if (selectionLayerAccessor.onlyFullRowsAreSelected()) {
                final E4CopySettingsHandler copyHandler = new E4CopySettingsHandler();
                copyHandler.copySettings(selection, clipboard);
            } else {

            }

            return null;
        }
    }
}
