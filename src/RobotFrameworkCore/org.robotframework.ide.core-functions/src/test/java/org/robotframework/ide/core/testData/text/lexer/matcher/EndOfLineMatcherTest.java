package org.robotframework.ide.core.testData.text.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.CharBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see EndOfLineMatcher
 */
public class EndOfLineMatcherTest {

    @ForClean
    private ISingleCharTokenMatcher matcher;


    @Test
    public void test_match_forRobotTypeCARRIAGE_RETURNs_likeInSomeOS_onlySeparator() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\r\r\r\r".toCharArray());
        TokenOutput tokenOutput = new TokenOutput();

        // execute & verify
        for (int i = 0; i < tempBuffer.length(); i++) {
            assertThat(matcher.match(tokenOutput, tempBuffer, i)).isTrue();
            // times 2 because we adding also END_OF_LINE token
            assertThat(tokenOutput.getTokens()).hasSize((i + 1) * 2);
            int currentLine = 1;
            for (int j = 0; j < (i + 1) * 2; j += 2) {
                RobotToken crToken = tokenOutput.getTokens().get(j);

                assertTokenTypeAndText(crToken, RobotTokenType.CARRIAGE_RETURN,
                        "\r");

                assertTokenStartPosition(crToken, currentLine,
                        LinearPositionMarker.THE_FIRST_COLUMN);
                assertTokenEndPosition(crToken, currentLine,
                        LinearPositionMarker.THE_FIRST_COLUMN + 1);

                RobotToken eolToken = tokenOutput.getTokens().get(j + 1);
                assertTokenTypeAndText(eolToken, RobotTokenType.END_OF_LINE,
                        null);

                assertTokenStartPosition(eolToken, currentLine,
                        LinearPositionMarker.THE_FIRST_COLUMN + 1);
                assertTokenEndPosition(eolToken, currentLine,
                        LinearPositionMarker.THE_FIRST_COLUMN + 1);

                currentLine++;
            }
        }
    }


    private void assertTokenTypeAndText(RobotToken token, RobotType type,
            String text) {
        assertThat(token).isNotNull();
        assertThat(token.getType()).isEqualTo(type);
        if (text == null) {
            assertThat(token.getText()).isNull();
        } else {
            assertThat(token.getText().toString()).isEqualTo(text);
        }
    }


    private void assertTokenStartPosition(RobotToken token, int line, int column) {
        LinearPositionMarker eolStartPosition = token.getStartPosition();
        assertThat(eolStartPosition).isNotNull();
        assertThat(eolStartPosition.getLine()).isEqualTo(line);
        assertThat(eolStartPosition.getColumn()).isEqualTo(column);
    }


    private void assertTokenEndPosition(RobotToken token, int line, int column) {
        LinearPositionMarker eolEndPosition = token.getEndPosition();
        assertThat(eolEndPosition).isNotNull();
        assertThat(eolEndPosition.getLine()).isEqualTo(line);
        assertThat(eolEndPosition.getColumn()).isEqualTo(column);
    }


    @Test
    public void test_match_noEndOfLineCharacters_shouldAlwaysSay_FALSE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("abcdefgh".toCharArray());

        // execute & verify
        for (int i = 0; i < tempBuffer.length(); i++) {
            TokenOutput tokenOutput = new TokenOutput();
            assertThat(matcher.match(tokenOutput, tempBuffer, i)).isFalse();
            assertThat(tokenOutput.getTokens()).isEmpty();
            assertThat(tokenOutput.getTokensPosition().size()).isEqualTo(0);
            LinearPositionMarker currentMarker = tokenOutput.getCurrentMarker();
            assertThat(currentMarker).isNotNull();
            assertThat(currentMarker.getLine()).isEqualTo(
                    LinearPositionMarker.THE_FIRST_LINE);
            assertThat(currentMarker.getColumn()).isEqualTo(
                    LinearPositionMarker.THE_FIRST_COLUMN);
        }
    }


    @Before
    public void setUp() {
        matcher = new EndOfLineMatcher();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
