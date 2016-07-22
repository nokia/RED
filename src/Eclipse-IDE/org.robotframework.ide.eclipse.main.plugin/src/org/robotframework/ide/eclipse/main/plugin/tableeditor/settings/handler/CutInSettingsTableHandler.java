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
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CopyInSettingsTableHandler.E4CopyInSettingsTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CutInSettingsTableHandler.E4CutInSettingsTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteInSettingsTableHandler.E4DeleteInSettingsTableHandler;
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
        public void cut(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final boolean copiedToClipboard = new E4CopyInSettingsTableHandler().copy(editor, selection, clipboard);
            if (copiedToClipboard) {
                new E4DeleteInSettingsTableHandler().delete(selection, editor, commandsStack);
            }
        }
    }
}
