package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Check if current line contains literal character with hex value hhhhhhhh.
 * love hotel: \U0001f3e9
 * 
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType
 * 
 * @see SimpleRobotContextType#UNICODE_CHAR_WITH_HEX_VALUE
 */
public class UnicodeCharacterWithHexValue extends ACharacterAsHexValue {

    public UnicodeCharacterWithHexValue() {
        super(SimpleRobotContextType.UNICODE_CHAR_WITH_HEX_VALUE, 'U', 8);
    }


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> recognize = super.recognize(currentContext,
                lineInterval);

        if (recognize != null) {
            for (int i = 0; i < recognize.size(); i++) {
                IContextElement context = recognize.get(i);
                if (context instanceof OneLineSingleRobotContextPart) {
                    OneLineSingleRobotContextPart ctx = (OneLineSingleRobotContextPart) context;
                    if (!isValid(ctx)) {
                        recognize.remove(i);
                        i = i - 1;
                    }
                }
            }
        } else {
            recognize = new LinkedList<>();
        }

        return recognize;
    }


    private boolean isValid(OneLineSingleRobotContextPart ctx) {
        boolean result = false;
        RobotToken unicodeToken = ctx.getContextTokens().get(1);
        StringBuilder text = unicodeToken.getText();
        String unicode = text.substring(1, 8);
        try {
            int value = Integer.parseInt(unicode, 16);
            // check if is correct unicode
            result = (value >= 0 && value <= 0x10FFFF);
        } catch (NumberFormatException nfe) {
            result = false;
        }

        return result;
    }
}
