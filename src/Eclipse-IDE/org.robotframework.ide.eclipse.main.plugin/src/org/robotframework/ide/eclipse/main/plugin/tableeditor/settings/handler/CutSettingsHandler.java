/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CopySettingsHandler.E4CopySettingsHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CutSettingsHandler.E4CutSettingsHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteSettingsHandler.E4DeleteSettingsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutSettingsHandler extends DIParameterizedHandler<E4CutSettingsHandler> {

    public CutSettingsHandler() {
        super(E4CutSettingsHandler.class);
    }

    public static class E4CutSettingsHandler {

        @Execute
        public void cutKeywords(final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final boolean copiedToClipboard = new E4CopySettingsHandler().copySettings(selection, clipboard);
            if (copiedToClipboard) {
                new E4DeleteSettingsHandler().deleteSettings(commandsStack, selection);
            }
        }
    }
}
