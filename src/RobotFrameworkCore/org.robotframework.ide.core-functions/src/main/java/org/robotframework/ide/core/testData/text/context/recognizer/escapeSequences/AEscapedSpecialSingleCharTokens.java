package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

import java.util.List;

import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;


/**
 * Provides extraction of functionality related to handling special escape
 * sequence like i.e. '\@' or '\$'.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public abstract class AEscapedSpecialSingleCharTokens extends
        AEscapedRecognizer {

    protected AEscapedSpecialSingleCharTokens(SimpleRobotContextType buildType,
            char lowerCaseCharRecognized, char upperCaseCharRecognized,
            List<IRobotTokenType> expectedType) {
        super(buildType, lowerCaseCharRecognized, upperCaseCharRecognized);
        setCustomWordTypeHandler(this.new OwnSignTypeHandler(expectedType,
                buildType));
    }

    private class OwnSignTypeHandler implements IRobotTypeCustomHandler {

        private final List<IRobotTokenType> expectedType;
        private final SimpleRobotContextType buildType;


        public OwnSignTypeHandler(final List<IRobotTokenType> expectedType,
                final SimpleRobotContextType buildType) {
            this.expectedType = expectedType;
            this.buildType = buildType;
        }


        @Override
        public boolean canAccept(IRobotTokenType o) {
            return (expectedType.contains(o));
        }


        @Override
        public CustomHandlerOutput handle(List<IContextElement> foundContexts,
                OneLineSingleRobotContextPart context, RobotToken token,
                boolean wasEscape, LineTokenPosition lineInterval) {
            CustomHandlerOutput out = new CustomHandlerOutput(foundContexts,
                    context, token, wasEscape);
            if (wasEscape) {
                context.addNextToken(token);
                context.setType(buildType);
                foundContexts.add(context);

                context = createContext(lineInterval);
                out.setContextToUseInNextIteration(context);
                out.setWasEscape(false);
                out.setWasUsed(true);
            } else {
                out.setWasUsed(false);
            }
            return out;
        }
    }
}
