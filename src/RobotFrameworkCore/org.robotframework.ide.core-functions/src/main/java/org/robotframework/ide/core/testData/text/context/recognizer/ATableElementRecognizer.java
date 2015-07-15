package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.ExpectedSequenceElement.PriorityType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;

import com.google.common.annotations.VisibleForTesting;


/**
 * Gives common functionality for search, which are not multiple lines - just
 * one line and in all tables.
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
        this.expectedSequence = Collections.unmodifiableList(expectedSequence);
        this.sequenceLength = expectedSequence.size();
    }


    /**
     * 
     * @param expectedSequence
     *            sequence will be modifiable so we will able to test iteration
     *            over this list
     * @param buildType
     */
    @VisibleForTesting
    protected ATableElementRecognizer(
            final List<ExpectedSequenceElement> expectedSequence,
            final IContextElementType buildType) {
        // order change special just to have possibility to use the same amount
        // of arguments
        this.BUILD_TYPE = buildType;
        this.expectedSequence = expectedSequence;
        this.sequenceLength = expectedSequence.size();
    }


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

        /**
         * Logic for this method base on {@link ExpectedSequenceElement} and
         * idea, that some elements could be optional, so present of them are
         * not crucial for parsing, therefore we can skip missing optional
         * element until we will find mandatory, which can't be skipped
         */
        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        int expectedTokenId = 0;
        int previousTokenId = -1;
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            ExpectedSequenceElement currentType = expectedSequence
                    .get(expectedTokenId);
            if (type == currentType.getType()) {
                context.addNextToken(token);
                if (expectedTokenId + 1 >= sequenceLength) {
                    context.setType(BUILD_TYPE);
                    foundContexts.add(context);
                    context = createContext(lineInterval);
                    expectedTokenId = 0;
                } else {
                    expectedTokenId++;
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
                        tokId = tokId - 1; // lets try to check this token again
                                           // if we do not
                                           // have
                                           // case
                        // like i.e. Suite Suite Setup
                    }
                } else {
                    if (expectedTokenId + 1 >= sequenceLength) {
                        // this last element is optional so can be skipped and
                        // context can be close
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);
                        context = createContext(lineInterval);
                        expectedTokenId = 0;
                    } else {
                        if (previousTokenId != tokId) {
                            previousTokenId = tokId;
                            tokId = tokId - 1; // lets try with next element if
                                               // it belongs to this token
                            expectedTokenId++;
                        }
                    }
                }
            }
        }

        if (wasAllMandatoryFound(expectedSequence, expectedTokenId)) {
            context.setType(BUILD_TYPE);
            foundContexts.add(context);
        }

        return foundContexts;
    }


    @VisibleForTesting
    protected boolean wasAllMandatoryFound(
            final List<ExpectedSequenceElement> expectedSequence,
            final int currentPosition) {
        boolean result = true;

        int size = expectedSequence.size();
        if (currentPosition >= 0 && currentPosition < size) {
            for (int i = currentPosition; i < size; i++) {
                if (expectedSequence.get(i).getPriority() == PriorityType.MANDATORY) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }


    @VisibleForTesting
    public List<ExpectedSequenceElement> getExpectedElements() {
        return Collections.unmodifiableList(expectedSequence);
    }


    protected static List<ExpectedSequenceElement> createExpectedAllMandatory(
            IRobotTokenType... types) {
        List<ExpectedSequenceElement> elems = new LinkedList<>();
        for (IRobotTokenType t : types) {
            elems.add(ExpectedSequenceElement.buildMandatory(t));
        }

        return elems;
    }


    /**
     * helper method for creation square wrapped elements
     * 
     * @param types
     *            mandatory {@code tokens types} , which should exist inside
     *            {@code '[' tokens types ']'}
     * @return
     */
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


    /**
     * 
     * @param types
     *            mandatory {@code tokens types} , which should exist, the last
     *            element is optional colon {@code ':'}
     * @return
     */
    protected static List<ExpectedSequenceElement> createExpectedWithOptionalColonAsLast(
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
