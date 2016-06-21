/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.List;

import org.eclipse.swt.dnd.Clipboard;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordCallsTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.PasteRobotElementCellsCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;

/**
 * @author mmarzec
 *
 */
public class PasteSettingsCellsCommandsCollector extends PasteRobotElementCellsCommandsCollector {

    @Override
    protected boolean hasRobotElementsInClipboard(final Clipboard clipboard) {
        return KeywordCallsTransfer.hasSettings(clipboard);
    }

    @Override
    protected RobotElement[] getRobotElementsFromClipboard(final Clipboard clipboard) {
        final Object probablySettings = clipboard.getContents(KeywordCallsTransfer.getInstance());
        return probablySettings != null && probablySettings instanceof RobotKeywordCall[]
                ? (RobotKeywordCall[]) probablySettings : null;
    }
    
    @Override
    protected int findSelectedElementTableIndex(final RobotElement section, final RobotElement selectedElement) {
        return section instanceof RobotSettingsSection && selectedElement instanceof RobotSetting ? TableHandlersSupport
                .findTableIndexOfSelectedSetting((RobotSettingsSection) section, (RobotSetting) selectedElement) : -1;
    }
    
    @Override
    protected String findValueToPaste(final RobotElement elementFromClipboard, final int clipboardSettingColumnIndex,
            final int tableColumnsCount) {
        
        String valueToPaste = "";
        final RobotSetting settingFromClipboard = (RobotSetting) elementFromClipboard;
        final List<String> arguments = settingFromClipboard.getArguments();
        if (settingFromClipboard.getGroup() == SettingsGroup.METADATA) {
            if (!arguments.isEmpty()) {
                if (clipboardSettingColumnIndex == 0) {
                    valueToPaste = arguments.get(0);
                } else if (clipboardSettingColumnIndex == 1 && arguments.size() > 0) {
                    valueToPaste = arguments.get(1);

                } else if (clipboardSettingColumnIndex == 2) {
                    valueToPaste = settingFromClipboard.getComment();
                }
            }
        } else {
            if (clipboardSettingColumnIndex == 0) {
                valueToPaste = settingFromClipboard.getName();
            } else {
                int argIndex = clipboardSettingColumnIndex - 1;
                if (argIndex < arguments.size()) {
                    valueToPaste = arguments.get(argIndex);
                } else if (clipboardSettingColumnIndex == tableColumnsCount - 1) {
                    valueToPaste = settingFromClipboard.getComment();
                }
            }
        }
        return valueToPaste;
    }

    @Override
    protected void collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final int selectedElementColumnIndex, final String valueToPaste, final int tableColumnsCount,
            final List<EditorCommand> pasteCommands) {

        if (selectedElement instanceof RobotSetting) {
            final RobotSetting selectedSetting = (RobotSetting) selectedElement;
            if (selectedElementColumnIndex == tableColumnsCount - 1) {
                pasteCommands.add(new SetKeywordCallCommentCommand(selectedSetting, valueToPaste));
            } else {
                if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
                    pasteCommands.add(new SetKeywordCallArgumentCommand(selectedSetting, selectedElementColumnIndex,
                            valueToPaste));
                } else if (selectedSetting.getGroup() == SettingsGroup.NO_GROUP) {
                    if (selectedElementColumnIndex > 0) {
                        pasteCommands.add(new SetKeywordCallArgumentCommand(selectedSetting,
                                selectedElementColumnIndex - 1, valueToPaste));
                    }
                } else {
                    if (selectedElementColumnIndex == 0) {
                        pasteCommands.add(new SetKeywordCallNameCommand(selectedSetting, valueToPaste));
                    } else {
                        pasteCommands.add(new SetKeywordCallArgumentCommand(selectedSetting,
                                selectedElementColumnIndex - 1, valueToPaste));
                    }
                }
            }
        }
    }

}
