package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseSettingsRule extends TokenTypeBasedRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION,
            RobotTokenType.TEST_CASE_SETTING_SETUP, RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION,
            RobotTokenType.TEST_CASE_SETTING_TEARDOWN, RobotTokenType.TEST_CASE_SETTING_TEMPLATE,
            RobotTokenType.TEST_CASE_SETTING_TIMEOUT, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION);

    public TestCaseSettingsRule(final IToken textToken) {
        super(textToken, types);
    }
}
