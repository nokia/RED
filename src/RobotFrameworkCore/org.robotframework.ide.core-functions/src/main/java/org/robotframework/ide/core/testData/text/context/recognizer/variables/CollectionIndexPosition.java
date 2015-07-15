package org.robotframework.ide.core.testData.text.context.recognizer.variables;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.ContextElementComparator;
import org.robotframework.ide.core.testData.text.context.recognizer.IContextRecognizer;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


/**
 * Check if current line contains literal open and close square brackets @{code
 * []} between them should be for collection type variable index in case of
 * usage
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * 
 * @see RobotSingleCharTokenType#SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET
 * @see RobotSingleCharTokenType#SINGLE_POSITION_INDEX_END_SQUARE_BRACKET
 * @see SimpleRobotContextType#COLLECTION_TYPE_VARIABLE_POSITION
 * 
 * @see ListVariableRecognizer
 * @see DictionaryVariableRecognizer
 * @see ScalarVariableRecognizer
 */
public class CollectionIndexPosition implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.COLLECTION_TYPE_VARIABLE_POSITION;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

        List<OneLineSingleRobotContextPart> tempContexts = new LinkedList<>();
        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET) {
                if (containsInBeginPositionStartToken(context)) {
                    tempContexts.add(context);
                    context = createContext(lineInterval);
                }
                context.addNextToken(token);
            } else if (type == RobotSingleCharTokenType.SINGLE_POSITION_INDEX_END_SQUARE_BRACKET) {
                if (containsInBeginPositionStartToken(context)) {
                    context.addNextToken(token);
                    context.setType(BUILD_TYPE);
                    foundContexts.add(context);

                    if (!tempContexts.isEmpty()) {
                        context = tempContexts.remove(tempContexts.size() - 1);
                    } else {
                        context = createContext(lineInterval);
                    }
                }
            } else {
                if (containsInBeginPositionStartToken(context)) {
                    context.addNextToken(token);
                }
            }
        }

        Collections.sort(foundContexts, new ContextElementComparator());
        return foundContexts;
    }


    @VisibleForTesting
    protected boolean containsInBeginPositionStartToken(
            OneLineSingleRobotContextPart context) {
        boolean result = false;
        List<RobotToken> contextTokens = context.getContextTokens();
        if (!contextTokens.isEmpty()) {
            result = contextTokens.get(0).getType() == RobotSingleCharTokenType.SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET;
        }
        return result;
    }


    @VisibleForTesting
    protected boolean wasPreviousPositionStartToken(
            OneLineSingleRobotContextPart context) {
        boolean result = false;
        List<RobotToken> contextTokens = context.getContextTokens();
        if (!contextTokens.isEmpty()) {
            result = contextTokens.get(contextTokens.size() - 1).getType() == RobotSingleCharTokenType.SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET;
        }

        return result;
    }


    @VisibleForTesting
    protected OneLineSingleRobotContextPart createContext(
            final LineTokenPosition lineInterval) {
        return new OneLineSingleRobotContextPart(lineInterval.getLineNumber());
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }
}
