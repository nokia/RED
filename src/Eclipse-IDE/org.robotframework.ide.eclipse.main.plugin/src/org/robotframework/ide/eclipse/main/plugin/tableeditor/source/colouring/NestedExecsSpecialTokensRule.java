/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.SpecialKeywords;

public class NestedExecsSpecialTokensRule implements ISyntaxColouringRule {

    private final IToken textToken;

    private final EnumSet<RobotTokenType> applicableTypes;

    private final EnumSet<RobotTokenType> readStoppingTypes;

    public NestedExecsSpecialTokensRule(final IToken textToken) {
        this.textToken = textToken;
        this.applicableTypes = EnumSet.of(RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT,
                RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT,
                RobotTokenType.KEYWORD_ACTION_ARGUMENT,
                RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT,
                RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT,
                RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT,
                RobotTokenType.SETTING_TEST_SETUP_KEYWORD_ARGUMENT,
                RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT);
        this.readStoppingTypes = EnumSet.of(RobotTokenType.TEST_CASE_NAME,
                RobotTokenType.TEST_CASE_SETTING_SETUP,
                RobotTokenType.TEST_CASE_SETTING_TEARDOWN,
                RobotTokenType.KEYWORD_NAME,
                RobotTokenType.KEYWORD_SETTING_TEARDOWN,
                RobotTokenType.SETTING_SUITE_SETUP_DECLARATION,
                RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION,
                RobotTokenType.SETTING_TEST_SETUP_DECLARATION,
                RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION);
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return token instanceof RobotToken && applicableTypes.contains(token.getTypes().get(0));
    }

    private boolean shouldStopOnElement(final IRobotLineElement token) {
        return readStoppingTypes.contains(token.getTypes().get(0));
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        final List<RobotToken> tokensBefore = ExecutableCallRule.getPreviousTokensInThisExecutable(token, context,
                this::shouldStopOnElement);
        for (int j = tokensBefore.size() - 1; j >= 0; j--) {
            final QualifiedKeywordName qualifiedKeywordName = QualifiedKeywordName
                    .fromOccurrence(tokensBefore.get(j).getText());
            if (SpecialKeywords.isNestingKeyword(qualifiedKeywordName)
                    && SpecialKeywords.isNestedSyntaxSpecialToken(qualifiedKeywordName, (RobotToken) token)) {
                return Optional
                        .of(new PositionedTextToken(textToken, token.getStartOffset(), token.getText().length()));
            }
        }
        return Optional.empty();
    }
}
