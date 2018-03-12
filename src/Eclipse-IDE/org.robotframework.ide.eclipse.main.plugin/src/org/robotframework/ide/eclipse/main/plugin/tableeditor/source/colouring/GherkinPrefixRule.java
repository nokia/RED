/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;

public class GherkinPrefixRule extends ExecutableRowCallRule {

    public GherkinPrefixRule(final IToken textToken) {
        super(textToken);
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<IRobotLineElement> analyzedTokens) {
        if (offsetInToken == 0 && shouldBeColored(token, analyzedTokens)) {
            final String textAfterPrefix = GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(token.getText());
            final int prefixLength = token.getText().length() - textAfterPrefix.length();
            if (prefixLength > 0) {
                return Optional.of(new PositionedTextToken(textToken, token.getStartOffset(), prefixLength));
            }
        }
        return Optional.empty();
    }
}
