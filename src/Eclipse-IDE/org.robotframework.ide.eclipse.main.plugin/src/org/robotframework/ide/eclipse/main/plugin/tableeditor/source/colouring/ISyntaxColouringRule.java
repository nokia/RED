package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;

import com.google.common.base.Optional;

public interface ISyntaxColouringRule {

    public static final IToken DEFAULT_TOKEN = new Token(null);

    public boolean isApplicable(IRobotLineElement nextToken);

    public Optional<PositionedTextToken> evaluate(final IRobotLineElement nextToken, int offsetInToken,
            List<IRobotLineElement> analyzedTokens);

    public static class PositionedTextToken {

        private final IToken token;

        private final int offset;

        private final int length;

        protected PositionedTextToken(final IToken token, final int offset, final int length) {
            this.token = token;
            this.offset = offset;
            this.length = length;
        }

        public IToken getToken() {
            return token;
        }

        public Position getPosition() {
            return new Position(offset, length);
        }
    }
}
