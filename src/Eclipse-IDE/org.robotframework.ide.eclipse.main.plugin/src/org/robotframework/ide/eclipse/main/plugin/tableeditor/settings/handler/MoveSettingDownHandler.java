/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.MoveSettingDownCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.MoveSettingDownHandler.E4MoveSettingDownHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class MoveSettingDownHandler extends DIParameterizedHandler<E4MoveSettingDownHandler> {

    public MoveSettingDownHandler() {
        super(E4MoveSettingDownHandler.class);
    }

    public static class E4MoveSettingDownHandler {

        @Execute
        public Object moveSettingDown(final RobotEditorCommandsStack stack,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {

            final RobotSetting selectedSetting = Selections.getSingleElement(selection, RobotSetting.class);
            if (selectedSetting.getGroup() != SettingsGroup.NO_GROUP) {
                stack.execute(new MoveSettingDownCommand(selectedSetting));
            }

            return null;
        }
    }
}
