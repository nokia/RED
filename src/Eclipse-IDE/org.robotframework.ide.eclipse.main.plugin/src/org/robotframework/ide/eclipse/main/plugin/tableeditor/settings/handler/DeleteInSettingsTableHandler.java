/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetSettingKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4DeleteInTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteInSettingsTableHandler.E4DeleteInSettingsTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class DeleteInSettingsTableHandler extends DIParameterizedHandler<E4DeleteInSettingsTableHandler> {

    public DeleteInSettingsTableHandler() {
        super(E4DeleteInSettingsTableHandler.class);
    }

    public static class E4DeleteInSettingsTableHandler extends E4DeleteInTableHandler {

        @Override
        protected EditorCommand getCommandForSelectedElement(final RobotElement selectedElement, final int columnIndex,
                final int tableColumnCount) {
            final RobotSetting selectedSetting = (RobotSetting) selectedElement;
            if (columnIndex < tableColumnCount - 1) {
                if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
                    return new SetSettingKeywordCallArgumentCommand(selectedSetting, columnIndex, null);
                } else if (columnIndex > 0) {
                    return new SetSettingKeywordCallArgumentCommand(selectedSetting, columnIndex - 1, null);
                }
            } else if (columnIndex == tableColumnCount - 1) {
                return new SetKeywordCallCommentCommand(selectedSetting, null);
            }
            return null;
        }

    }
}
