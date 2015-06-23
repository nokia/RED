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
 * @see ScalarVariableBeginSignMatcher
 */
public class ScalarVariableBeginSignMatcherTest {

    @ForClean
    private ISingleCharTokenMatcher matcher;


    @Test
    public void test_match_forTokenStoreWithTwoAsterisks_andWithTwoDolarSignsInTempBuffer_shouldReturn_two_SINGLE_SCALAR_BEGIN_DOLLAR_tokens() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("$$");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes;
            if ((charIndex + 1) % 2 == 0) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_SCALAR_BEGIN_DOLLAR,
                        RobotTokenType.SINGLE_SCALAR_BEGIN_DOLLAR };
                assertThat(output.getTokens()).hasSize(4);
            } else {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_SCALAR_BEGIN_DOLLAR };
                assertThat(output.getTokens()).hasSize(3);
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithOneSingleDolarSignInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("$");
        TokenOutput output = new TokenOutput();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(
                output,
                new RobotTokenType[] { RobotTokenType.SINGLE_SCALAR_BEGIN_DOLLAR },
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
        matcher = new ScalarVariableBeginSignMatcher();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
