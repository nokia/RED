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
 * Search and builds the context for Test Case table header - i.e.
 * 
 * <pre>
 * *** Test Cases ***
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
 * @see RobotWordType#TEST_WORD
 * @see RobotWordType#CASE_WORD
 * @see RobotWordType#CASES_WORD
 * @see RobotWordType#DOUBLE_SPACE
 * 
 * @see SimpleRobotContextType#TEST_CASE_TABLE_HEADER
 */
public class TestCaseTableHeaderRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.TEST_CASE_TABLE_HEADER;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = new OneLineSingleRobotContextPart(
                lineInterval.getLineNumber());

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        boolean wasPrefixAsterisksPresent = false;
        boolean wasTestWordPresent = false;
        boolean wasCaseWordPresent = false;
        boolean wasSuffixAsterisksPresent = false;

        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_ASTERISK
                    || type == MultipleCharTokenType.MANY_ASTERISKS) {
                if (!wasPrefixAsterisksPresent && !wasTestWordPresent
                        && !wasCaseWordPresent && !wasSuffixAsterisksPresent) {
                    // begin asterisks
                    context.addNextToken(token);
                    wasPrefixAsterisksPresent = true;
                } else if (wasPrefixAsterisksPresent && wasTestWordPresent
                        && wasCaseWordPresent) {
                    // trailing asterisks
                    context.addNextToken(token);
                    context.setType(BUILD_TYPE);
                    foundContexts.add(context);

                    context = new OneLineSingleRobotContextPart(
                            lineInterval.getLineNumber());
                    context.addNextToken(token);

                    wasPrefixAsterisksPresent = false;
                    wasTestWordPresent = false;
                    wasCaseWordPresent = false;
                    wasSuffixAsterisksPresent = true;
                } else {
                    // i.e. case *** *** - asteriks after asterisks or other
                    // word
                    context.removeAllContextTokens();
                    context.addNextToken(token);

                    wasPrefixAsterisksPresent = true;
                    wasTestWordPresent = false;
                    wasCaseWordPresent = false;
                    wasSuffixAsterisksPresent = false;
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_SPACE
                    || type == RobotWordType.DOUBLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_TABULATOR) {
                if (wasPrefixAsterisksPresent || wasTestWordPresent
                        || wasCaseWordPresent || wasSuffixAsterisksPresent) {
                    // space are allowed after the first asterisks or after
                    // table name
                    context.addNextToken(token);
                }
            } else if (type == RobotWordType.TEST_WORD) {
                if (wasPrefixAsterisksPresent || wasSuffixAsterisksPresent) {
                    // *Test Test - case protection
                    if (wasTestWordPresent && wasCaseWordPresent) {
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);

                        context = new OneLineSingleRobotContextPart(
                                lineInterval.getLineNumber());

                        wasPrefixAsterisksPresent = false;
                        wasTestWordPresent = false;
                        wasCaseWordPresent = false;
                        wasSuffixAsterisksPresent = false;
                    } else if (!wasTestWordPresent && !wasCaseWordPresent) {
                        // table name after begin asterisks
                        context.addNextToken(token);
                        wasTestWordPresent = true;
                        if (wasSuffixAsterisksPresent) {
                            wasPrefixAsterisksPresent = true;
                            wasSuffixAsterisksPresent = false;
                        }
                    } else {
                        // test appears after test or case
                        context.removeAllContextTokens();

                        wasPrefixAsterisksPresent = false;
                        wasTestWordPresent = false;
                        wasCaseWordPresent = false;
                        wasSuffixAsterisksPresent = false;
                    }
                }
            } else if (type == RobotWordType.CASE_WORD
                    || type == RobotWordType.CASES_WORD) {
                // *Test Case Case - case protection
                if (wasTestWordPresent && wasCaseWordPresent) {
                    context.setType(BUILD_TYPE);
                    foundContexts.add(context);

                    context = new OneLineSingleRobotContextPart(
                            lineInterval.getLineNumber());

                    wasPrefixAsterisksPresent = false;
                    wasTestWordPresent = false;
                    wasCaseWordPresent = false;
                    wasSuffixAsterisksPresent = false;
                } else if (wasTestWordPresent && !wasCaseWordPresent) {
                    // table name after begin asterisks
                    context.addNextToken(token);
                    wasCaseWordPresent = true;
                    if (wasSuffixAsterisksPresent) {
                        wasPrefixAsterisksPresent = true;
                        wasSuffixAsterisksPresent = false;
                    }
                } else {
                    // test appears after test or case
                    context.removeAllContextTokens();

                    wasPrefixAsterisksPresent = false;
                    wasTestWordPresent = false;
                    wasCaseWordPresent = false;
                    wasSuffixAsterisksPresent = false;
                }
            } else {
                if (wasPrefixAsterisksPresent && wasTestWordPresent
                        && wasCaseWordPresent) {
                    context.setType(BUILD_TYPE);
                    foundContexts.add(context);

                    context = new OneLineSingleRobotContextPart(
                            lineInterval.getLineNumber());
                }

                wasPrefixAsterisksPresent = false;
                wasTestWordPresent = false;
                wasCaseWordPresent = false;
                wasSuffixAsterisksPresent = false;
            }
        }

        if (wasPrefixAsterisksPresent && wasTestWordPresent
                && wasCaseWordPresent) {
            // case when is not END OF LINE on header name
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
