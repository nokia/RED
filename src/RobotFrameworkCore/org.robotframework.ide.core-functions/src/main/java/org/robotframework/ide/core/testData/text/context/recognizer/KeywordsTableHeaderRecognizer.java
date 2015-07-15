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
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Search and builds the context for User keyword table header - i.e.
 * 
 * <pre>
 * *** Keywords ***
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
 * @see RobotWordType#USER_WORD
 * @see RobotWordType#KEYWORD_WORD
 * @see RobotWordType#KEYWORDS_WORD
 * @see RobotWordType#DOUBLE_SPACE
 * 
 * @see SimpleRobotContextType#KEYWORD_TABLE_HEADER
 */
public class KeywordsTableHeaderRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.KEYWORD_TABLE_HEADER;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = new OneLineSingleRobotContextPart(
                lineInterval.getLineNumber());

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        boolean wasPrefixAsterisksPresent = false;
        boolean wasUserWordPresent = false;
        boolean wasKeywordWordPresent = false;
        boolean wasSuffixAsterisksPresent = false;

        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_ASTERISK
                    || type == MultipleCharTokenType.MANY_ASTERISKS) {
                if (!wasPrefixAsterisksPresent && !wasUserWordPresent
                        && !wasKeywordWordPresent && !wasSuffixAsterisksPresent) {
                    // begin asterisk
                    context.addNextToken(token);
                    wasPrefixAsterisksPresent = true;
                } else if (wasPrefixAsterisksPresent && wasKeywordWordPresent) {
                    // trailing asterisks
                    context.addNextToken(token);
                    context.setType(BUILD_TYPE);
                    foundContexts.add(context);

                    context = new OneLineSingleRobotContextPart(
                            lineInterval.getLineNumber());
                    context.addNextToken(token);

                    wasPrefixAsterisksPresent = false;
                    wasUserWordPresent = false;
                    wasKeywordWordPresent = false;
                    wasSuffixAsterisksPresent = true;
                } else {
                    // i.e. case *** *** - asteriks after asterisks or other
                    // word
                    context.removeAllContextTokens();
                    context.addNextToken(token);

                    wasPrefixAsterisksPresent = true;
                    wasUserWordPresent = false;
                    wasKeywordWordPresent = false;
                    wasSuffixAsterisksPresent = false;
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_SPACE
                    || type == RobotWordType.DOUBLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_TABULATOR) {
                if (wasPrefixAsterisksPresent || wasUserWordPresent
                        || wasKeywordWordPresent || wasSuffixAsterisksPresent) {
                    // space are allowed after the first asterisks or after
                    // table name
                    context.addNextToken(token);
                }
            } else if (type == RobotWordType.USER_WORD) {
                if (wasPrefixAsterisksPresent || wasSuffixAsterisksPresent) {
                    // *Keyword User - case protection
                    if (wasKeywordWordPresent) {
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);

                        context = new OneLineSingleRobotContextPart(
                                lineInterval.getLineNumber());

                        wasPrefixAsterisksPresent = false;
                        wasUserWordPresent = false;
                        wasKeywordWordPresent = false;
                        wasSuffixAsterisksPresent = false;
                    } else if (!wasKeywordWordPresent && !wasUserWordPresent) {
                        // table name after begin asterisks
                        context.addNextToken(token);
                        wasUserWordPresent = true;
                        if (wasSuffixAsterisksPresent) {
                            wasPrefixAsterisksPresent = true;
                            wasSuffixAsterisksPresent = false;
                        }
                    } else {
                        // test appears after test or case
                        context.removeAllContextTokens();

                        wasPrefixAsterisksPresent = false;
                        wasUserWordPresent = false;
                        wasKeywordWordPresent = false;
                        wasSuffixAsterisksPresent = false;
                    }
                }
            } else if (type == RobotWordType.KEYWORD_WORD
                    || type == RobotWordType.KEYWORDS_WORD) {
                if (wasPrefixAsterisksPresent || wasSuffixAsterisksPresent) {
                    // *Keyword Keyword - case protection
                    if (wasKeywordWordPresent) {
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);

                        context = new OneLineSingleRobotContextPart(
                                lineInterval.getLineNumber());

                        wasPrefixAsterisksPresent = false;
                        wasUserWordPresent = false;
                        wasKeywordWordPresent = false;
                        wasSuffixAsterisksPresent = false;
                    } else if (!wasKeywordWordPresent) {
                        // table name after begin asterisks
                        context.addNextToken(token);
                        wasKeywordWordPresent = true;
                        if (wasSuffixAsterisksPresent) {
                            wasPrefixAsterisksPresent = true;
                            wasSuffixAsterisksPresent = false;
                        }
                    } else {
                        // keyword appears after keyword or case
                        context.removeAllContextTokens();

                        wasPrefixAsterisksPresent = false;
                        wasUserWordPresent = false;
                        wasKeywordWordPresent = false;
                        wasSuffixAsterisksPresent = false;
                    }
                }
            } else {
                if (wasPrefixAsterisksPresent && wasKeywordWordPresent) {
                    context.setType(BUILD_TYPE);
                    foundContexts.add(context);

                    context = new OneLineSingleRobotContextPart(
                            lineInterval.getLineNumber());
                }

                wasPrefixAsterisksPresent = false;
                wasUserWordPresent = false;
                wasKeywordWordPresent = false;
                wasSuffixAsterisksPresent = false;
            }
        }

        if (wasPrefixAsterisksPresent && wasKeywordWordPresent) {
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
