package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.ExpectedSequenceElement;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.ExpectedSequenceElement.PriorityType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;

import com.google.common.annotations.VisibleForTesting;


/**
 * Gives common functionality for search, which are not multiple lines - just
 * one line and for Setting table.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public abstract class ATableElementRecognizer implements IContextRecognizer {

    private final IContextElementType BUILD_TYPE;
    private final List<ExpectedSequenceElement> expectedSequence;
    private final int sequenceLength;


    protected ATableElementRecognizer(final IContextElementType buildType,
            final List<ExpectedSequenceElement> expectedSequence) {
        this.BUILD_TYPE = buildType;
        this.expectedSequence = expectedSequence;
        this.sequenceLength = expectedSequence.size();
    }


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        int expectedTokenId = 0;
        ExpectedSequenceElement currentType = expectedSequence
                .get(expectedTokenId);
        int previousTokenId = -1;
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == currentType.getType()) {
                context.addNextToken(token);
                if (expectedTokenId + 1 >= sequenceLength) {
                    foundContexts.add(context);
                    context = createContext(lineInterval);
                    expectedTokenId = 0;
                } else {
                    expectedTokenId++;
                    currentType = expectedSequence.get(expectedTokenId);
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_TABULATOR
                    || type == RobotSingleCharTokenType.SINGLE_PIPE
                    || type == RobotWordType.DOUBLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_COMMENT_HASH
                    || type == MultipleCharTokenType.MANY_COMMENT_HASHS) {
                context.addNextToken(token);
            } else {
                if (currentType.getPriority() == PriorityType.MANDATORY) {
                    // this is for ensure not line which is interesting for use
                    context.removeAllContextTokens();
                    expectedTokenId = 0;
                    if (previousTokenId != tokId) {
                        previousTokenId = tokId;
                        tokId = tokId - 1; // lets try to check if we do not
                                           // have
                                           // case
                        // like i.e. Suite Suite Setup
                    }
                } else {
                    // search for next mandatory
                    if (expectedTokenId + 1 >= sequenceLength) {
                        context.removeAllContextTokens();
                        expectedTokenId = 0;
                        if (previousTokenId != tokId) {
                            previousTokenId = tokId;
                            tokId = tokId - 1; // lets try to check if we do not
                                               // have
                                               // case
                        }
                    } else {
                        expectedTokenId++;
                        currentType = expectedSequence.get(expectedTokenId);
                    }
                }
            }
        }

        return foundContexts;
    }


    private static List<ExpectedSequenceElement> createExpectedAllMandatory(
            IRobotTokenType... types) {
        List<ExpectedSequenceElement> elems = new LinkedList<>();
        for (IRobotTokenType t : types) {
            elems.add(ExpectedSequenceElement.buildMandatory(t));
        }

        return elems;
    }


    protected static List<ExpectedSequenceElement> createExpectedInsideSquareBrackets(
            IRobotTokenType... types) {
        List<ExpectedSequenceElement> elems = createExpectedAllMandatory(types);
        if (!elems.isEmpty()) {
            elems.add(
                    0,
                    ExpectedSequenceElement
                            .buildMandatory(RobotSingleCharTokenType.SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET));

            elems.add(ExpectedSequenceElement
                    .buildMandatory(RobotSingleCharTokenType.SINGLE_POSITION_INDEX_END_SQUARE_BRACKET));
        }
        return elems;
    }


    protected static List<ExpectedSequenceElement> createExpectedForSettingsTable(
            IRobotTokenType... types) {
        List<ExpectedSequenceElement> elems = createExpectedAllMandatory(types);
        if (!elems.isEmpty()) {
            elems.add(ExpectedSequenceElement
                    .buildOptional(RobotSingleCharTokenType.SINGLE_COLON));
        }

        return elems;
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
