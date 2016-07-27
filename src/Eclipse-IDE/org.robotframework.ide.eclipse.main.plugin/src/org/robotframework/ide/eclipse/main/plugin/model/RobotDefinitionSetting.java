/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Predicate;

public class RobotDefinitionSetting extends RobotKeywordCall {

    private static final long serialVersionUID = 1L;

    public RobotDefinitionSetting(final RobotCodeHoldingElement robotCodeHoldingElement,
            final AModelElement<?> linkedElement) {
        super(robotCodeHoldingElement, linkedElement);
    }

    @Override
    public String getName() {
        final String nameInBrackets = super.getName();
        return nameInBrackets.substring(1, nameInBrackets.length() - 1);
    }

    @Override
    public List<String> getArguments() {
        if (arguments == null) {
            final List<RobotToken> allTokens = getLinkedElement().getElementTokens();
            final Iterable<RobotToken> tokensWithoutComments = filter(allTokens, new Predicate<RobotToken>() {

                @Override
                public boolean apply(final RobotToken token) {
                    return !token.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                            && !token.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)
                            && !token.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_SETUP)
                            && !token.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION)
                            && !token.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION)
                            && !token.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_TEARDOWN)
                            && !token.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE)
                            && !token.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_TIMEOUT)
                            && !token.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION)
                            && !token.getTypes().contains(RobotTokenType.KEYWORD_SETTING_ARGUMENTS)
                            && !token.getTypes().contains(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION)
                            && !token.getTypes().contains(RobotTokenType.KEYWORD_SETTING_TAGS)
                            && !token.getTypes().contains(RobotTokenType.KEYWORD_SETTING_TEARDOWN)
                            && !token.getTypes().contains(RobotTokenType.KEYWORD_SETTING_RETURN)
                            && !token.getTypes().contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT)
                            && !token.getTypes().contains(RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION);
                }
            });
            arguments = newArrayList(transform(tokensWithoutComments, TokenFunctions.tokenToString()));
        }
        return arguments;
    }
}
