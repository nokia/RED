/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class KeywordSettingsCallRule extends TokenTypeBasedRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME);

    public KeywordSettingsCallRule(final IToken textToken) {
        super(textToken, types);
    }
}
