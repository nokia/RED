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
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Search and builds the context for Setting table header - i.e.
 * 
 * <pre>
 * *** Settings ***
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
 * @see RobotWordType#SETTING_WORD
 * @see RobotWordType#SETTINGS_WORD
 * @see RobotWordType#METADATA_WORD
 * @see RobotWordType#DOUBLE_SPACE
 * 
 * @see SimpleRobotContextType#SETTING_TABLE_HEADER
 */
public class SettingTableHeaderRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.SETTING_TABLE_HEADER;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineRobotContext context = new OneLineRobotContext(
                lineInterval.getLineNumber());

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        boolean wasPrefixAsterisksPresent = false;
        boolean wasSettingNamePresent = false;
        boolean wasSuffixAsterisksPresent = false;

        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_ASTERISK
                    || type == MultipleCharTokenType.MANY_ASTERISKS) {

                if (!wasPrefixAsterisksPresent && !wasSettingNamePresent) {
                    // begin asterisks
                    context.addNextToken(token);
                    wasPrefixAsterisksPresent = true;
                } else if (wasPrefixAsterisksPresent && wasSettingNamePresent) {
                    // trailing asterisks
                    context.addNextToken(token);
                    context.setType(BUILD_TYPE);
                    foundContexts.add(context);

                    context = new OneLineRobotContext(
                            lineInterval.getLineNumber());
                    context.addNextToken(token);

                    wasPrefixAsterisksPresent = false;
                    wasSettingNamePresent = false;
                    wasSuffixAsterisksPresent = true;
                } else {
                    // i.e. case *** *** - asteriks after asterisks or other
                    // word
                    context.removeAllContextTokens();
                    context.addNextToken(token);

                    wasPrefixAsterisksPresent = true;
                    wasSettingNamePresent = false;
                    wasSuffixAsterisksPresent = false;
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_SPACE
                    || type == RobotWordType.DOUBLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_TABULATOR) {
                if (wasPrefixAsterisksPresent || wasSettingNamePresent
                        || wasSuffixAsterisksPresent) {
                    // space are allowed after the first asterisks or after
                    // table name
                    context.addNextToken(token);
                }
            } else if (type == RobotWordType.SETTING_WORD
                    || type == RobotWordType.SETTINGS_WORD
                    || type == RobotWordType.METADATA_WORD) {
                if (wasPrefixAsterisksPresent || wasSuffixAsterisksPresent) {
                    // *Settings Settings case protection
                    if (wasSettingNamePresent) {
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);

                        context = new OneLineRobotContext(
                                lineInterval.getLineNumber());

                        wasPrefixAsterisksPresent = false;
                        wasSettingNamePresent = false;
                        wasSuffixAsterisksPresent = false;
                    } else {
                        // table name after begin asterisks
                        context.addNextToken(token);
                        wasSettingNamePresent = true;
                        if (wasSuffixAsterisksPresent) {
                            wasPrefixAsterisksPresent = true;
                            wasSuffixAsterisksPresent = false;
                        }
                    }
                }
            } else {
                if (wasPrefixAsterisksPresent && wasSettingNamePresent) {
                    context.setType(BUILD_TYPE);
                    foundContexts.add(context);

                    context = new OneLineRobotContext(
                            lineInterval.getLineNumber());
                }

                wasPrefixAsterisksPresent = false;
                wasSettingNamePresent = false;
                wasSuffixAsterisksPresent = false;
            }
        }

        if (wasPrefixAsterisksPresent && wasSettingNamePresent) {
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
