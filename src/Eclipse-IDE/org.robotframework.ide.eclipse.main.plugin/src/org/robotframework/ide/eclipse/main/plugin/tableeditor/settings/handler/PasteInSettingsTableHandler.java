/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.contexts.IContextService;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.GeneralSettingsFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.PasteInSettingsTableHandler.E4PasteInSettingsTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.PasteSettingsHandler.E4PasteSettingsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteInSettingsTableHandler extends DIParameterizedHandler<E4PasteInSettingsTableHandler> {

    public PasteInSettingsTableHandler() {
        super(E4PasteInSettingsTableHandler.class);
    }

    public static class E4PasteInSettingsTableHandler {

        @Execute
        public void paste(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard,
                final IContextService contextService, final RobotEditorCommandsStack commandsStack) {

            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();
            final List<RobotElement> selectedSettings = Selections.getElements(selection, RobotElement.class);

            if (selectedSettings.isEmpty() && contextService.getActiveContextIds()
                    .contains(GeneralSettingsFormFragment.GENERAL_SETTINGS_CONTEXT_ID)) {
                selectedSettings.addAll(E4PasteSettingsHandler.createNewGeneralSettingsIfNotPresentInSection(fileModel,
                        selectionLayerAccessor.getSelectedPositions(), commandsStack));
            }

            final List<EditorCommand> pasteCommands = new PasteSettingsCellsCommandsCollector()
                    .collectPasteCommands(selectionLayerAccessor, selectedSettings, clipboard);

            for (final EditorCommand command : pasteCommands) {
                commandsStack.execute(command);
            }
        }

    }
}
