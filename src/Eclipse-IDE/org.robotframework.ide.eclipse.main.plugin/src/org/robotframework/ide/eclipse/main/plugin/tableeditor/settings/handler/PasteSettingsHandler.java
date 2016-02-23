/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordCallsTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.PasteSettingsHandler.E4PasteSettingsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class PasteSettingsHandler extends DIParameterizedHandler<E4PasteSettingsHandler> {

    public PasteSettingsHandler() {
        super(E4PasteSettingsHandler.class);
    }

    public static class E4PasteSettingsHandler {

        @Inject
        @Named(RobotEditorSources.SUITE_FILE_MODEL)
        private RobotSuiteFile fileModel;

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object pasteKeywords(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final Clipboard clipboard) {
            final Object probablySettings = clipboard.getContents(KeywordCallsTransfer.getInstance());
            if (probablySettings instanceof RobotKeywordCall[]) {
                insertSettings(selection, (RobotKeywordCall[]) probablySettings);
            }
            return null;
        }

        private void insertSettings(final IStructuredSelection selection, final RobotKeywordCall[] settings) {
            final Optional<RobotSetting> firstSelected = Selections.getOptionalFirstElement(selection,
                    RobotSetting.class);

            if (firstSelected.isPresent()) {
                final int index = firstSelected.get().getParent().getChildren().indexOf(firstSelected);
                commandsStack.execute(new InsertSettingCommand(firstSelected.get().getParent(), index, settings));
            } else {
                final RobotSettingsSection section = fileModel.findSection(
                        RobotSettingsSection.class).orNull();
                if (section != null) {
                    commandsStack.execute(new InsertSettingCommand(section, settings));
                }
            }
        }
    }
}
