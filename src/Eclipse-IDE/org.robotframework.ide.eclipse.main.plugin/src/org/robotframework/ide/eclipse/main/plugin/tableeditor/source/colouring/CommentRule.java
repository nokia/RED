package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;


public class CommentRule implements ISyntaxColouringRule {

    private final IToken textToken;

    public CommentRule(final IToken textToken) {
        this.textToken = textToken;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return token instanceof RobotToken;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<IRobotLineElement> analyzedTokens) {
        final List<IRobotTokenType> tokenTypes = token.getTypes();

        if (tokenTypes.contains(RobotTokenType.START_HASH_COMMENT)
                || tokenTypes.contains(RobotTokenType.COMMENT_TOKEN)
                || tokenTypes.contains(RobotTokenType.COMMENT_CONTINUE)) {
            return Optional.of(new PositionedTextToken(textToken, token.getStartOffset(), token.getText().length()));
        }
        return Optional.absent();
    }
}
