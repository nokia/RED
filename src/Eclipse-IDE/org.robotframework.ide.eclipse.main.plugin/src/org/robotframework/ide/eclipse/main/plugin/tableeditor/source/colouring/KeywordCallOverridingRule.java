/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.KeywordUsagesFinder;


public class KeywordCallOverridingRule implements ISyntaxColouringRule {

    private final ISyntaxColouringRule wrappedRule;

    private final IToken tokenToOverride;

    private final IToken overridingToken;

    private final KeywordUsagesFinder kwUsagesFinder;

    public KeywordCallOverridingRule(final ISyntaxColouringRule wrappedRule, final IToken tokenToOverride,
            final IToken overridingToken, final KeywordUsagesFinder kwUsagesFinder) {
        this.wrappedRule = wrappedRule;
        this.tokenToOverride = tokenToOverride;
        this.overridingToken = overridingToken;
        this.kwUsagesFinder = kwUsagesFinder;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement nextToken) {
        return wrappedRule.isApplicable(nextToken);
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        final Optional<PositionedTextToken> evaluatedToken = wrappedRule.evaluate(token, offsetInToken, context);

        if (evaluatedToken.isPresent() && evaluatedToken.get().getToken() == tokenToOverride
                && kwUsagesFinder.isLibraryKeyword(token.getStartOffset() + offsetInToken)) {
            return Optional.of(new PositionedTextToken(overridingToken, evaluatedToken.get().getOffset(),
                    evaluatedToken.get().getLength()));
        }
        return evaluatedToken;
    }

}
