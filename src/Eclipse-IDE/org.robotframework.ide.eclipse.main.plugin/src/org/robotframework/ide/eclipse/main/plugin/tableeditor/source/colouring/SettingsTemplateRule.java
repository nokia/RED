/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class SettingsTemplateRule extends TokenTypeBasedRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.SETTING_TEST_TEMPLATE_KEYWORD_NAME,
            RobotTokenType.SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT,
            RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME,
            RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);

    public SettingsTemplateRule(final IToken textToken) {
        super(textToken, types);
    }
}
