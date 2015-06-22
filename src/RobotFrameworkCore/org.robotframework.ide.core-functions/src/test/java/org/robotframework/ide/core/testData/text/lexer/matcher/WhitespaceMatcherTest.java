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
 * @see WhitespaceMatcher
 */
public class WhitespaceMatcherTest {

    @ForClean
    private ISingleCharTokenMatcher matcher;


    @Test
    public void test_matchForEmptyTokenStore_andWithFourSpacesInTempBuffer_shouldReturn_two_DOUBLE_SPACE_TOKEN_and_SINGLE_SPACE_tokens() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("   ");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes = null;
            if (charIndex == 0) {
                expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_SPACE };
                assertThat(output.getTokens()).hasSize(1);
            } else if (charIndex == 1) {
                expectedSequenceOfTypes = new RobotType[] { RobotWordType.DOUBLE_SPACE };
                assertThat(output.getTokens()).hasSize(1);
            } else if (charIndex == 2) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotWordType.DOUBLE_SPACE, RobotTokenType.SINGLE_SPACE };
                assertThat(output.getTokens()).hasSize(2);
            } else if (charIndex == 3) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotWordType.DOUBLE_SPACE, RobotWordType.DOUBLE_SPACE };
                assertThat(output.getTokens()).hasSize(2);
            }

            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisks_andWithTwoSpacesInTempBuffer_shouldReturn_DOUBLE_SPACE_TOKEN() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("  ");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes;
            if ((charIndex + 1) % 2 == 0) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotWordType.DOUBLE_SPACE };
                assertThat(output.getTokens()).hasSize(3);
            } else {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_SPACE };
                assertThat(output.getTokens()).hasSize(3);
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithTwoSpacesInTempBuffer_shouldReturn_DOUBLE_SPACE_TOKEN() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("  ");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes;
            if ((charIndex + 1) % 2 == 0) {
                expectedSequenceOfTypes = new RobotType[] { RobotWordType.DOUBLE_SPACE };
                assertThat(output.getTokens()).hasSize(1);
            } else {
                expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_SPACE };
                assertThat(output.getTokens()).hasSize(1);
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisksInside_andWithSpaceAndTabulatorInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap(" \t");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotTokenType[] expectedSequenceOfTypes;
            if ((charIndex + 1) % 2 == 0) {
                expectedSequenceOfTypes = new RobotTokenType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_SPACE,
                        RobotTokenType.SINGLE_TABULATOR };
                assertThat(output.getTokens()).hasSize(4);
            } else {
                expectedSequenceOfTypes = new RobotTokenType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_SPACE };
                assertThat(output.getTokens()).hasSize(3);
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisksInside_andWithOneTabulatorInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\t");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(output, new RobotTokenType[] {
                RobotTokenType.SINGLE_ASTERISK, RobotTokenType.SINGLE_ASTERISK,
                RobotTokenType.SINGLE_TABULATOR }, 0, 1);
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithOneSingleTabulatorInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\t");
        TokenOutput output = new TokenOutput();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(output,
                new RobotTokenType[] { RobotTokenType.SINGLE_TABULATOR }, 0, 1);
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisksInside_andWithOneSingleSpaceInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap(" ");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(output, new RobotTokenType[] {
                RobotTokenType.SINGLE_ASTERISK, RobotTokenType.SINGLE_ASTERISK,
                RobotTokenType.SINGLE_SPACE }, 0, 1);
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithOneSingleSpaceInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap(" ");
        TokenOutput output = new TokenOutput();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(output,
                new RobotTokenType[] { RobotTokenType.SINGLE_SPACE }, 0, 1);
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
        matcher = new WhitespaceMatcher();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
