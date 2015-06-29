package org.robotframework.ide.core.testData.text.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertCurrentPosition;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertIsTotalEmpty;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertPositionMarkers;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokens;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.createTokenOutputWithTwoAsterisksInside;

import java.nio.CharBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see EscapeBackslashSignMatcher
 */
public class EscapeBackslashSignMatcherTest {

    @ForClean
    private ISingleCharTokenMatcher matcher;


    @Test
    public void test_matchForEmptyTokenStore_andWithFourBackslashInTempBuffer_shouldReturn_two_DOUBLE_SPACE_TOKEN_tokens() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\\\\\\\\");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            IRobotTokenType[] expectedSequenceOfTypes = null;
            if (charIndex == 0) {
                expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH };
                assertThat(output.getTokens()).hasSize(1);
            } else if (charIndex == 1) {
                expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.DOUBLE_ESCAPE_BACKSLASH };
                assertThat(output.getTokens()).hasSize(1);
            } else if (charIndex == 2) {
                expectedSequenceOfTypes = new IRobotTokenType[] {
                        RobotWordType.DOUBLE_ESCAPE_BACKSLASH,
                        RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH };
                assertThat(output.getTokens()).hasSize(2);
            } else if (charIndex == 3) {
                expectedSequenceOfTypes = new IRobotTokenType[] {
                        RobotWordType.DOUBLE_ESCAPE_BACKSLASH,
                        RobotWordType.DOUBLE_ESCAPE_BACKSLASH };
                assertThat(output.getTokens()).hasSize(2);
            }

            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisks_andWithTwoBackslashInTempBuffer_shouldReturn_DOUBLE_ESCAPE_BACKSLASH_TOKEN() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\\\\");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            IRobotTokenType[] expectedSequenceOfTypes;
            assertThat(output.getTokens()).hasSize(3);
            if ((charIndex + 1) % 2 == 0) {
                expectedSequenceOfTypes = new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.DOUBLE_ESCAPE_BACKSLASH };
            } else {
                expectedSequenceOfTypes = new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH };
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithTwoBackslashInTempBuffer_shouldReturn_DOUBLE_ESCAPE_BACKSLASH_TOKEN() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\\\\");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            IRobotTokenType[] expectedSequenceOfTypes;
            assertThat(output.getTokens()).hasSize(1);
            if ((charIndex + 1) % 2 == 0) {
                expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.DOUBLE_ESCAPE_BACKSLASH };
            } else {
                expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH };
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisksInside_andWithOneSingleBackslashInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\\");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(output, new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK, RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH }, 0, 1);
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithOneSingleBackslashInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\\");
        TokenOutput output = new TokenOutput();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(
                output,
                new RobotSingleCharTokenType[] { RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH },
                0, 1);
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithAsterisks_shouldReturn_FALSE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("*");
        TokenOutput output = new TokenOutput();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isFalse();
        assertIsTotalEmpty(output);
    }


    @Before
    public void setUp() {
        matcher = new EscapeBackslashSignMatcher();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
