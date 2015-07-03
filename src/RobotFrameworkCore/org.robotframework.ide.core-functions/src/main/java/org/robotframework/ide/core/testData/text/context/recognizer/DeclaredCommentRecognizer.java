package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Check if current line contains literal (word) comment or comment by hash
 * sign.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_COMMENT_HASH
 * @see RobotWordType#COMMENT_FROM_BUILTIN
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * 
 * @see SimpleRobotContextType#DECLARED_COMMENT
 */
public class DeclaredCommentRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.DECLARED_COMMENT;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = new OneLineSingleRobotContextPart(
                lineInterval.getLineNumber());

        boolean wasCommentHash = false;
        boolean wasCommentWord = false;
        boolean wasEscape = false;

        RobotToken lastEscape = null;
        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_COMMENT_HASH) {
                if (!wasCommentHash && !wasCommentWord) {
                    if (wasEscape) {
                        wasEscape = false;
                    } else {
                        wasCommentHash = true;
                    }
                }

                if (wasCommentHash || wasCommentWord) {
                    context.addNextToken(token);
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
                if (wasCommentHash || wasCommentWord) {
                    context.addNextToken(token);
                } else {
                    lastEscape = token;
                    wasEscape = true;
                }
            } else if (type == RobotWordType.COMMENT_FROM_BUILTIN) {
                wasEscape = false;
                lastEscape = null;
                wasCommentWord = true;
                context.addNextToken(token);
            } else if (type == MultipleCharTokenType.MANY_COMMENT_HASHS) {
                if (!wasCommentHash && !wasCommentWord) {
                    if (wasEscape) {
                        context.addNextToken(lastEscape);
                    }

                    context.addNextToken(token);
                    wasCommentHash = true;
                } else {
                    wasCommentHash = true;
                }

                lastEscape = null;
                wasEscape = false;
                if (wasCommentHash || wasCommentWord) {
                    context.addNextToken(token);
                }
            } else {
                wasEscape = false;
                lastEscape = null;
                if (wasCommentWord || wasCommentHash) {
                    context.addNextToken(token);
                }
            }
        }

        if (!context.getContextTokens().isEmpty()) {
            context.setType(BUILD_TYPE);
            foundContexts.add(context);
        }

        return foundContexts;
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }
}
