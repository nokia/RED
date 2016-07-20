/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetCaseSettingNameCommand extends EditorCommand {

    private final RobotDefinitionSetting setting;
    private final String name;

    public SetCaseSettingNameCommand(final RobotDefinitionSetting setting, final String name) {
        this.setting = setting;
        this.name = name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (setting.getName().equals(name)) {
            return;
        }
        setting.setName(name.substring(0, name.length() - 1));

        final AModelElement<?> linkedSetting = setting.getLinkedElement();
        linkedSetting.getDeclaration().setText(name);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, setting);
    }
}
