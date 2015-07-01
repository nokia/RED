package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineRobotContext;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Search and builds the context for quotes sequence of words - i.e.
 * 
 * <pre>
 * &quot;foobar is subsequence of acronyms 'foo' and 'bar'&quot;
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ASTERISK
 * @see RobotSingleCharTokenType#SINGLE_TABULATOR
 * @see RobotSingleCharTokenType#SINGLE_SPACE
 * @see RobotSingleCharTokenType#SINGLE_QUOTE_MARK
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType#DOUBLE_SPACE
 * 
 */
public class QuotesSentenceRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.QUOTES_SENTENCE;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineRobotContext context = new OneLineRobotContext(
                lineInterval.getLineNumber());

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();

        boolean wasEscapeChar = false;
        boolean wasQuoteMark = false;

        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_QUOTE_MARK) {
                if (wasQuoteMark) {
                    if (wasEscapeChar) {
                        // case "\"d"
                        context.addNextToken(token);
                        wasEscapeChar = false;
                    } else {
                        // closing quote mark
                        context.addNextToken(token);
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);

                        context = new OneLineRobotContext(
                                lineInterval.getLineNumber());
                        // for catch all possible quote marks, we adding it as
                        // begin quote mark
                        wasEscapeChar = false;
                        context.addNextToken(token);
                    }
                } else if (!wasEscapeChar) {
                    wasQuoteMark = true;
                    context.addNextToken(token);
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
                wasEscapeChar = true;

                if (wasQuoteMark) {
                    context.addNextToken(token);
                }
            } else {
                // this false is set to handle: "abcd \h" case
                // this prevents to think that last quote mark was after escape
                // backslash
                wasEscapeChar = false;
                if (wasQuoteMark) {
                    context.addNextToken(token);
                }
            }
        }

        return foundContexts;
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }

}
