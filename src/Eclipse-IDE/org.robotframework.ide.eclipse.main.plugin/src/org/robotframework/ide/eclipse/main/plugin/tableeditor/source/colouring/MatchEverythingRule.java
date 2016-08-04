package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Optional;


public class MatchEverythingRule implements ISyntaxColouringRule {

    private final IToken textToken;

    public MatchEverythingRule(final IToken textToken) {
        this.textToken = textToken;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return token instanceof RobotToken;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<IRobotLineElement> analyzedTokens) {
        return Optional.of(new PositionedTextToken(textToken, token.getStartOffset(), token.getText().length()));
    }
}
