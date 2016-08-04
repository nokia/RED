package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;


public class KeywordNameRule extends VariableUsageRule {

    private final IToken nameToken;

    public KeywordNameRule(final IToken nameToken, final IToken embeddedVariablesToken) {
        super(embeddedVariablesToken);
        this.nameToken = nameToken;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<IRobotLineElement> analyzedTokens) {
        final IRobotTokenType type = token.getTypes().get(0);

        if (type == RobotTokenType.KEYWORD_NAME) {
            return super.evaluate(token, offsetInToken, analyzedTokens).or(
                    Optional.of(new PositionedTextToken(nameToken, token.getStartOffset(), token.getText().length())));
        }
        return Optional.absent();
    }

    @Override
    protected IToken getTokenForNonVariablePart() {
        return nameToken;
    }
}
