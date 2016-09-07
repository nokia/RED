/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.PasteRobotElementCellsCommandsCollector;

/**
 * @author mmarzec
 */
public class PasteSettingsCellsCommandsCollector extends PasteRobotElementCellsCommandsCollector {

    @Override
    protected boolean hasRobotElementsInClipboard(final RedClipboard clipboard) {
        return clipboard.hasSettings();
    }

    @Override
    protected RobotElement[] getRobotElementsFromClipboard(final RedClipboard clipboard) {
        return clipboard.getKeywordCalls();
    }

    @Override
    protected List<String> findValuesToPaste(final RobotElement elementFromClipboard,
            final int clipboardSettingColumnIndex, final int tableColumnsCount) {

        final RobotSetting settingFromClipboard = (RobotSetting) elementFromClipboard;
        final List<String> arguments = settingFromClipboard.getArguments();
        if (settingFromClipboard.getGroup() == SettingsGroup.METADATA) {
            if (!arguments.isEmpty()) {
                if (clipboardSettingColumnIndex == 0) {
                    return newArrayList(arguments.get(0));
                } else if (clipboardSettingColumnIndex == 1 && arguments.size() > 1) {
                    return newArrayList(arguments.get(1));

                } else if (clipboardSettingColumnIndex == 2) {
                    return newArrayList(settingFromClipboard.getComment());
                }
            }
        } else {
            if (clipboardSettingColumnIndex == 0) {
                return newArrayList(settingFromClipboard.getName());
            } else {
                final int argIndex = clipboardSettingColumnIndex - 1;
                if (argIndex < arguments.size()) {
                    return newArrayList(arguments.get(argIndex));
                } else if (clipboardSettingColumnIndex == tableColumnsCount - 1) {
                    return newArrayList(settingFromClipboard.getComment());
                }
            }
        }
        return newArrayList();
    }

    @Override
    protected List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final List<String> valuesToPaste, final int selectedElementColumnIndex, final int tableColumnsCount) {

        final List<EditorCommand> pasteCommands = new ArrayList<>();
        if (selectedElement instanceof RobotSetting && !valuesToPaste.isEmpty()) {
            final String valueToPaste = valuesToPaste.get(0);
            final RobotSetting selectedSetting = (RobotSetting) selectedElement;
            if (selectedElementColumnIndex == tableColumnsCount - 1) {
                pasteCommands.add(new SetKeywordCallCommentCommand(selectedSetting, valueToPaste));
            } else {
                if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
                    pasteCommands.add(new SetSettingArgumentCommand(selectedSetting, selectedElementColumnIndex,
                            valueToPaste));
                } else if (selectedSetting.getGroup() == SettingsGroup.NO_GROUP) {
                    if (selectedElementColumnIndex > 0) {
                        pasteCommands.add(new SetSettingArgumentCommand(selectedSetting,
                                selectedElementColumnIndex - 1, valueToPaste));
                    }
                } else {
                    if (selectedElementColumnIndex == 0) {
                        pasteCommands.add(new SetSettingNameCommand(selectedSetting, valueToPaste));
                    } else {
                        pasteCommands.add(new SetSettingArgumentCommand(selectedSetting,
                                selectedElementColumnIndex - 1, valueToPaste));
                    }
                }
            }
        }
        return pasteCommands;
    }
}
