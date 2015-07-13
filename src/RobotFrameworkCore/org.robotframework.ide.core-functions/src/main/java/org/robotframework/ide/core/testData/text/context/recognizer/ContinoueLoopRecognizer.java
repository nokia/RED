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
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;

import com.google.common.annotations.VisibleForTesting;


/**
 * Check if current line contains continue of loop declaration
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotWordType#DOUBLE_SPACE
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotSingleCharTokenType#SINGLE_SPACE
 * @see RobotWordType#DOUBLE_SPACE
 * @see RobotSingleCharTokenType#SINGLE_TABULATOR
 * 
 * 
 * @see SimpleRobotContextType#CONTINOUE_LOOP_DECLARATION
 */
public class ContinoueLoopRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.CONTINOUE_LOOP_DECLARATION;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();

        int numberOfPipes = 0;
        boolean wasSingleEscapeBackslash = false;

        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_PIPE) {
                if (numberOfPipes == 0) {
                    // [space] \ [space] | case
                    if (wasSingleEscapeBackslash) {
                        context.addNextToken(token);
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);

                        context = createContext(lineInterval);
                        break;
                    } else {
                        context.addNextToken(token);
                    }

                    numberOfPipes++;
                } else if (numberOfPipes == 1) {
                    numberOfPipes++;
                    if (wasSingleEscapeBackslash) {
                        context.addNextToken(token);
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);

                        context = createContext(lineInterval);
                        break;
                    } else {
                        context.removeAllContextTokens();
                        break;
                    }
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_TABULATOR
                    || type == RobotWordType.DOUBLE_SPACE) {
                context.addNextToken(token);
            } else if (type == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
                if (wasSingleEscapeBackslash) {
                    context.removeAllContextTokens();
                    break;
                } else {
                    context.addNextToken(token);
                    wasSingleEscapeBackslash = true;
                }
            } else {
                break;
            }
        }

        return foundContexts;
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }


    @VisibleForTesting
    protected OneLineSingleRobotContextPart createContext(
            final LineTokenPosition lineInterval) {
        return new OneLineSingleRobotContextPart(lineInterval.getLineNumber());
    }
}
