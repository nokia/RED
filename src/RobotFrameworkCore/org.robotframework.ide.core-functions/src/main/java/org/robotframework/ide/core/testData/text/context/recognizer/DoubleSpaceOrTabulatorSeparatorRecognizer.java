package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Search and builds the context for double space or tabulator separated line -
 * i.e.
 * 
 * <pre>
 * Keyword  [DOUBLE_SPACE] ARGUMENT
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_TABULATOR
 * @see RobotSingleCharTokenType#SINGLE_SPACE
 * @see RobotWordType#DOUBLE_SPACE
 * 
 * @see SimpleRobotContextType#DOUBLE_SPACE_OR_TABULATOR_SEPARATED
 * @see SimpleRobotContextType#PRETTY_ALIGN
 * 
 */
public class DoubleSpaceOrTabulatorSeparatorRecognizer implements
        IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = new OneLineSingleRobotContextPart(
                lineInterval.getLineNumber());

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();

        List<RobotToken> temp = new LinkedList<>();
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotWordType.DOUBLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_TABULATOR) {
                temp.add(token);
            } else if (type == RobotSingleCharTokenType.SINGLE_SPACE
                    && !temp.isEmpty()) {
                // in case of three spaces following each other the first two is
                // merged to DOUBLE_SPACE
                temp.add(token);
            } else {
                context = merge(foundContexts, context, temp);
            }
        }

        context = merge(foundContexts, context, temp);

        return foundContexts;
    }


    private OneLineSingleRobotContextPart merge(
            final List<IContextElement> foundContexts,
            final OneLineSingleRobotContextPart context, final List<RobotToken> temp) {
        OneLineSingleRobotContextPart newContext = context;

        int numberOfPossibleSeparators = temp.size();
        if (numberOfPossibleSeparators > 0) {
            RobotToken separator = temp.get(0);
            context.addNextToken(separator);
            context.setType(BUILD_TYPE);
            foundContexts.add(context);

            OneLineSingleRobotContextPart prettyAlignContext = new OneLineSingleRobotContextPart(
                    context.getLineNumber());
            for (int i = 1; i < numberOfPossibleSeparators; i++) {
                prettyAlignContext.addNextToken(temp.get(i));
            }

            if (numberOfPossibleSeparators > 1) {
                prettyAlignContext.setType(SimpleRobotContextType.PRETTY_ALIGN);
                foundContexts.add(prettyAlignContext);
            }

            newContext = new OneLineSingleRobotContextPart(context.getLineNumber());
        }

        temp.clear(); // clean up after merge
        return newContext;
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }
}
