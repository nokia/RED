package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TestCaseSettingsCallRule extends TokenTypeBasedRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME,
            RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME,
            RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME);

    public TestCaseSettingsCallRule(final IToken textToken) {
        super(textToken, types);
    }
}
