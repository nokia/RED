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
import org.robotframework.ide.core.testData.text.lexer.GroupedSameTokenType;
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
 * @see DotSignMatcher
 */
public class DotSignMatcherTest {

    @ForClean
    private ISingleCharTokenMatcher matcher;


    @Test
    public void test_match_forTokenStoreWithTwoAsterisks_andWithFiveDotsInTempBuffer_shouldReturn_MORE_THAN_THREE_DOTS() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap(".....");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes = null;

            assertThat(output.getTokens()).hasSize(3);
            if (charIndex == 0) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_DOT };
            } else if (charIndex == 1) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotWordType.EMPTY_CELL_DOTS };
            } else if (charIndex == 2) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS };
            } else if (charIndex >= 3) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        GroupedSameTokenType.MORE_THAN_THREE_DOTS };
            }

            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisks_andWithThreeDotsInTempBuffer_shouldReturn_CONTINOUE_PREVIOUS_LINE_DOTS() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("...");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes = null;

            assertThat(output.getTokens()).hasSize(3);
            if (charIndex == 0) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_DOT };
            } else if (charIndex == 1) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotWordType.EMPTY_CELL_DOTS };
            } else if (charIndex == 2) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS };
            }

            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisks_andWithTwoDotsInTempBuffer_shouldReturn_EMPTY_CELL_DOTS() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("..");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes;
            assertThat(output.getTokens()).hasSize(3);

            if ((charIndex + 1) % 2 == 0) {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotWordType.EMPTY_CELL_DOTS };
            } else {
                expectedSequenceOfTypes = new RobotType[] {
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_ASTERISK,
                        RobotTokenType.SINGLE_DOT };
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithFiveDotsInTempBuffer_shouldReturn_MORE_THAN_THREE_DOTS() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap(".....");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes = null;
            assertThat(output.getTokens()).hasSize(1);

            if (charIndex == 0) {
                expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_DOT };
            } else if (charIndex == 1) {
                expectedSequenceOfTypes = new RobotType[] { RobotWordType.EMPTY_CELL_DOTS };
            } else if (charIndex == 2) {
                expectedSequenceOfTypes = new RobotType[] { RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS };
            } else if (charIndex >= 3) {
                expectedSequenceOfTypes = new RobotType[] { GroupedSameTokenType.MORE_THAN_THREE_DOTS };
            }

            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithThreeDotsInTempBuffer_shouldReturn_CONTINOUE_PREVIOUS_LINE_DOTS() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("...");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes = null;
            assertThat(output.getTokens()).hasSize(1);

            if (charIndex == 0) {
                expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_DOT };
            } else if (charIndex == 1) {
                expectedSequenceOfTypes = new RobotType[] { RobotWordType.EMPTY_CELL_DOTS };
            } else if (charIndex == 2) {
                expectedSequenceOfTypes = new RobotType[] { RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS };
            }

            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithTwoDotsInTempBuffer_shouldReturn_EMPTY_CELL_DOTS() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("..");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotType[] expectedSequenceOfTypes;
            assertThat(output.getTokens()).hasSize(1);

            if ((charIndex + 1) % 2 == 0) {
                expectedSequenceOfTypes = new RobotType[] { RobotWordType.EMPTY_CELL_DOTS };
            } else {
                expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_DOT };
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisksInside_andWithOneSingleDotInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap(".");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(output, new RobotTokenType[] {
                RobotTokenType.SINGLE_ASTERISK, RobotTokenType.SINGLE_ASTERISK,
                RobotTokenType.SINGLE_DOT }, 0, 1);
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithOneSingleDotInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap(".");
        TokenOutput output = new TokenOutput();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(output,
                new RobotTokenType[] { RobotTokenType.SINGLE_DOT }, 0, 1);
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
        matcher = new DotSignMatcher();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
