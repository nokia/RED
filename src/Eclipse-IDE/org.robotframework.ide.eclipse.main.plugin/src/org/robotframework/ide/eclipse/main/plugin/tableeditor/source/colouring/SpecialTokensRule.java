/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

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
        return token.getTypes()
                .stream()
                .filter(this::isSpecialTokenType)
                .findFirst()
                .map(t -> new PositionedTextToken(textToken, token.getStartOffset(), token.getText().length()));
    }

    private boolean isSpecialTokenType(final IRobotTokenType type) {
        return type == RobotTokenType.SETTING_LIBRARY_ALIAS || type == RobotTokenType.FOR_TOKEN
                || type == RobotTokenType.IN_TOKEN;
    }
}
