/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


public class CreateFreshCodeHolderSettingCommand extends EditorCommand {

    private final RobotCodeHoldingElement<?> codeHolder;

    private final String settingName;

    private final List<String> args;

    private final int index;

    public CreateFreshCodeHolderSettingCommand(final RobotCodeHoldingElement<?> codeHolder, final int index,
            final String settingName, final List<String> args) {
        this.codeHolder = codeHolder;
        this.index = index;
        this.settingName = settingName;
        this.args = args;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        codeHolder.createSetting(index, settingName, args, "");

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, codeHolder);
    }
}
