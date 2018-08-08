/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskSettingsRule extends TokenTypeBasedRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.TASK_SETTING_DOCUMENTATION,
            RobotTokenType.TASK_SETTING_SETUP, RobotTokenType.TASK_SETTING_TAGS_DECLARATION,
            RobotTokenType.TASK_SETTING_TEARDOWN, RobotTokenType.TASK_SETTING_TEMPLATE,
            RobotTokenType.TASK_SETTING_TIMEOUT, RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION);

    public TaskSettingsRule(final IToken textToken) {
        super(textToken, types);
    }
}
