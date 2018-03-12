/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class SettingRule extends TokenTypeBasedRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.SETTING_LIBRARY_DECLARATION,
            RobotTokenType.SETTING_RESOURCE_DECLARATION, RobotTokenType.SETTING_VARIABLES_DECLARATION,
            RobotTokenType.SETTING_DOCUMENTATION_DECLARATION, RobotTokenType.SETTING_METADATA_DECLARATION,
            RobotTokenType.SETTING_SUITE_SETUP_DECLARATION, RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION,
            RobotTokenType.SETTING_FORCE_TAGS_DECLARATION, RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION,
            RobotTokenType.SETTING_TEST_SETUP_DECLARATION, RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION,
            RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION, RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION,
            RobotTokenType.SETTING_UNKNOWN);

    public SettingRule(final IToken textToken) {
        super(textToken, types);
    }
}
