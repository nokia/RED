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
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class GherkinPrefixRule implements ISyntaxColouringRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.KEYWORD_ACTION_NAME,
            RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.KEYWORD_ACTION_ARGUMENT,
            RobotTokenType.TEST_CASE_ACTION_ARGUMENT);

    private final IToken textToken;

    public GherkinPrefixRule(final IToken textToken) {
        this.textToken = textToken;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return token instanceof RobotToken;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<IRobotLineElement> analyzedTokens) {
        if (shouldBeColored(token, offsetInToken, analyzedTokens)) {
            final String textAfterPrefix = GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(token.getText());
            final int prefixLength = token.getText().length() - textAfterPrefix.length();
            if (prefixLength > 0) {
                return Optional.of(new PositionedTextToken(textToken, token.getStartOffset(), prefixLength));
            }
        }
        return Optional.empty();
    }

    private boolean shouldBeColored(final IRobotLineElement token, final int offsetInToken,
            final List<IRobotLineElement> analyzedTokens) {
        return offsetInToken == 0 && types.contains(token.getTypes().get(0));
    }

}
