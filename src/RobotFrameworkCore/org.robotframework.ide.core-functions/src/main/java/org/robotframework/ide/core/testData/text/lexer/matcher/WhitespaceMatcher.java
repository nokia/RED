package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;

import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class WhitespaceMatcher implements ISingleCharTokenMatcher {

    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;

        char c = tempBuffer.get(charIndex);
        RobotTokenType type = RobotTokenType.getToken(c);

        if (type == RobotTokenType.SINGLE_SPACE) {
            // jezeli previous jest tez spacja zrob merge'a
        } else if (type == RobotTokenType.SINGLE_TABULATOR) {

        }
        // TODO: sprawdz ostatni czy ma specjalne znaczenie [fix-1] jezeli
        // zostal uzyty

        return wasUsed;
    }

}
