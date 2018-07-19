/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class SectionHeaderRule extends TokenTypeBasedRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.TEST_CASES_TABLE_HEADER,
            RobotTokenType.KEYWORDS_TABLE_HEADER, RobotTokenType.SETTINGS_TABLE_HEADER,
            RobotTokenType.VARIABLES_TABLE_HEADER, RobotTokenType.USER_OWN_TABLE_HEADER);

    public SectionHeaderRule(final IToken textToken) {
        super(textToken, types);
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInRobotToken,
            final List<RobotLine> context) {
        final List<IRobotTokenType> tokenTypes = token.getTypes();

        if (tokenTypes.contains(RobotTokenType.START_HASH_COMMENT)
                || tokenTypes.contains(RobotTokenType.COMMENT_CONTINUE)) {
            return Optional.empty();
        }
        return super.evaluate(token, offsetInRobotToken, context);
    }
}
