/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.List;

import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


public class CreateFreshCaseSettingCommand extends EditorCommand {

    private final RobotCase testCase;

    private final String settingName;

    private final List<String> args;

    private final int index;

    public CreateFreshCaseSettingCommand(final RobotCase testCase, final int index, final String settingName,
            final List<String> args) {
        this.testCase = testCase;
        this.index = index;
        this.settingName = settingName;
        this.args = args;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        final RobotDefinitionSetting setting = testCase.createSetting(index, settingName, args, "");

        if (setting.getLinkedElement().getModelType() == ModelType.TEST_CASE_SETTING_UNKNOWN) {
            setting.getLinkedElement().getDeclaration().setText(settingName);
        }

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, testCase);
    }
}
