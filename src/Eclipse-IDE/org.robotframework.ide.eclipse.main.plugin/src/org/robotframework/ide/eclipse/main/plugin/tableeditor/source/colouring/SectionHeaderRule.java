package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class SectionHeaderRule extends TokenTypeBasedRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.TEST_CASES_TABLE_HEADER,
            RobotTokenType.KEYWORDS_TABLE_HEADER, RobotTokenType.SETTINGS_TABLE_HEADER,
            RobotTokenType.VARIABLES_TABLE_HEADER, RobotTokenType.USER_OWN_TABLE_HEADER);

    public SectionHeaderRule(final IToken textToken) {
        super(textToken, types);
    }
}
