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
import org.eclipse.swt.dnd.Clipboard;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
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

        @Execute
        public Object pasteKeywords(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final Clipboard clipboard) {
            final Object probablySettings = clipboard.getContents(KeywordCallsTransfer.getInstance());
            if (probablySettings instanceof RobotKeywordCall[]) {
                insertSettings(fileModel, commandsStack, selection, (RobotKeywordCall[]) probablySettings);
            }
            return null;
        }

        private void insertSettings(final RobotSuiteFile fileModel, final RobotEditorCommandsStack commandsStack,
                final IStructuredSelection selection, final RobotKeywordCall[] settings) {
            final Optional<RobotSetting> firstSelected = Selections.getOptionalFirstElement(selection,
                    RobotSetting.class);

            if (firstSelected.isPresent()) {
                if (firstSelected.get().getGroup() == SettingsGroup.NO_GROUP) {
                    insertArgumentsInNoGroupSetting(commandsStack, firstSelected.get(), settings);
                } else {
                    commandsStack.execute(
                            new InsertSettingCommand(firstSelected.get().getParent(), firstSelected, settings));
                }
            } else {
                final RobotSettingsSection section = fileModel.findSection(RobotSettingsSection.class).orNull();
                if (section != null && !isNoGroupSetting(settings)) {
                    commandsStack.execute(new InsertSettingCommand(section, firstSelected, settings));
                }
            }
        }

        private void insertArgumentsInNoGroupSetting(final RobotEditorCommandsStack commandsStack,
                final RobotSetting firstSelectedSetting, final RobotKeywordCall[] settingsFromClipboard) {
            if (settingsFromClipboard.length > 0) {
                final List<String> arguments = settingsFromClipboard[0].getArguments();
                for (int i = 0; i < arguments.size(); i++) {
                    commandsStack.execute(new SetKeywordCallArgumentCommand(firstSelectedSetting, i, arguments.get(i)));
                }
                final String comment = settingsFromClipboard[0].getComment();
                if (comment != null && !comment.isEmpty()) {
                    commandsStack.execute(new SetKeywordCallCommentCommand(firstSelectedSetting, comment));
                }
            }
        }

        public boolean isNoGroupSetting(final RobotKeywordCall[] settings) {
            return settings.length > 0 && settings[0] instanceof RobotSetting
                    && ((RobotSetting) settings[0]).getGroup() == SettingsGroup.NO_GROUP;
        }
    }
}
