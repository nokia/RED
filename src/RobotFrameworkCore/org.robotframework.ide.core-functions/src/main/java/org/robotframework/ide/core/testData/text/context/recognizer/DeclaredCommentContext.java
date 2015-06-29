package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineRobotContext;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotTokenType#SINGLE_COMMENT_HASH
 * @see RobotWordType#COMMENT_FROM_BUILTIN
 * @see RobotTokenType#SINGLE_ESCAPE_BACKSLASH
 */
public class DeclaredCommentContext implements IContextRecognizer {

    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineRobotContext context = new OneLineRobotContext(
                lineInterval.getLineNumber());

        boolean isComment = false;
        boolean escape = false;
        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            RobotType type = token.getType();
            if (type == RobotTokenType.SINGLE_ESCAPE_BACKSLASH && !isComment) {
                escape = !escape;
            } else if (type == RobotTokenType.SINGLE_COMMENT_HASH) {
                if (!escape) {
                    isComment = true;
                    context.addNextToken(token);
                }
            } else if (type == RobotWordType.COMMENT_FROM_BUILTIN) {
                isComment = true;
                context.addNextToken(token);
            } else {
                if (isComment) {
                    context.addNextToken(token);
                } else {
                    escape = false;
                }
            }
        }

        if (!context.getContextTokens().isEmpty()) {
            context.setType(SimpleRobotContextType.DECLARED_COMMENT);
            foundContexts.add(context);
        }

        return foundContexts;
    }
}
