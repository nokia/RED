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
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class SpecialTokensRule implements ISyntaxColouringRule {

    private final IToken textToken;

    public SpecialTokensRule(final IToken textToken) {
        this.textToken = textToken;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement nextToken) {
        return nextToken instanceof RobotToken;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        if (isNoneAwareSetting(token)) {
            return Optional.of(new PositionedTextToken(textToken, token.getStartOffset(), token.getText().length()));

        } else {
            return token.getTypes()
                    .stream()
                    .filter(this::isSpecialTokenType)
                    .findFirst()
                    .map(t -> new PositionedTextToken(textToken, token.getStartOffset(), token.getText().length()));
        }
    }

    public static boolean isNoneAwareSetting(final IRobotLineElement token) {
        final IRobotTokenType type = token.getTypes().get(0);
        return token.getText().equalsIgnoreCase("none") && EnumSet.of(
                RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME,
                RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME,
                RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME,
                RobotTokenType.TEST_CASE_SETTING_TIMEOUT_VALUE,
                RobotTokenType.TEST_CASE_SETTING_TAGS,
                
                RobotTokenType.TASK_SETTING_SETUP_KEYWORD_NAME,
                RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_NAME,
                RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_NAME,
                RobotTokenType.TASK_SETTING_TIMEOUT_VALUE,
                RobotTokenType.TASK_SETTING_TAGS,
                
                RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME,
                RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE,
                RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME,
                
                RobotTokenType.SETTING_TEST_SETUP_KEYWORD_NAME,
                RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_NAME,
                RobotTokenType.SETTING_TEST_TEMPLATE_KEYWORD_NAME,
                RobotTokenType.SETTING_TEST_TIMEOUT_VALUE,
                RobotTokenType.SETTING_TASK_SETUP_KEYWORD_NAME,
                RobotTokenType.SETTING_TASK_TEARDOWN_KEYWORD_NAME,
                RobotTokenType.SETTING_TASK_TEMPLATE_KEYWORD_NAME,
                RobotTokenType.SETTING_TASK_TIMEOUT_VALUE,
                RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME,
                RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_NAME).contains(type);
    }

    private boolean isSpecialTokenType(final IRobotTokenType type) {
        return EnumSet
                .of(RobotTokenType.SETTING_LIBRARY_ALIAS, RobotTokenType.FOR_TOKEN, RobotTokenType.IN_TOKEN,
                        RobotTokenType.FOR_END_TOKEN)
                .contains(type);
    }
}
