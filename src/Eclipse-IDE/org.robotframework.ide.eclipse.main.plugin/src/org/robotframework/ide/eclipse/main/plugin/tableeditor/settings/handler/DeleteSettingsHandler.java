/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteSettingsHandler.E4DeleteSettingsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteSettingsHandler extends DIParameterizedHandler<E4DeleteSettingsHandler> {

    public DeleteSettingsHandler() {
        super(E4DeleteSettingsHandler.class);
    }

    public static class E4DeleteSettingsHandler {

        @Execute
        public void deleteSettings(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {
            
            final List<RobotSetting> settings = Selections.getElements(selection, RobotSetting.class);
            commandsStack.execute(new DeleteSettingKeywordCallCommand(settings));

            // needed when setting is cut/paste and selection remains on the same position,
            // pasting is performed on old, not existing setting
            editor.getSelectionLayerAccessor().getSelectionLayer().clear();
        }
    }
}
