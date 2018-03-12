/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


class TokenTypeBasedRule implements ISyntaxColouringRule {

    protected final IToken textToken;

    private final Collection<? extends IRobotTokenType> types;
    
    public TokenTypeBasedRule(final IToken textToken, final Collection<? extends IRobotTokenType> types) {
        this.textToken = textToken;
        this.types = types;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return token instanceof RobotToken;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInRobotToken,
            final List<IRobotLineElement> analyzedTokens) {
        final IRobotTokenType type = token.getTypes().get(0);

        if (types.contains(type)) {
            return Optional.of(new PositionedTextToken(textToken, token.getStartOffset(), token.getText().length()));
        }
        return Optional.empty();
    }
}
