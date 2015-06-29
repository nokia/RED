package org.robotframework.ide.core.testData.text.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertCurrentPosition;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertIsTotalEmpty;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertPositionMarkers;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokens;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.createTokenOutputWithTwoAsterisksInside;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
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
    public void test_match_forTokenStoreWithTwoAsterisksInside_and_lineFeedFollowingByCarriageReturn() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\n\r");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();
        RobotSingleCharTokenType[] types = new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK, RobotSingleCharTokenType.SINGLE_ASTERISK };

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotSingleCharTokenType[] crSequence;
            if ((charIndex + 1) % 2 == 0) {
                crSequence = new RobotSingleCharTokenType[] { RobotSingleCharTokenType.LINE_FEED,
                        RobotSingleCharTokenType.CARRIAGE_RETURN,
                        RobotSingleCharTokenType.END_OF_LINE };
                assertThat(output.getTokens()).hasSize(5);
            } else {
                crSequence = new RobotSingleCharTokenType[] { RobotSingleCharTokenType.LINE_FEED,
                        RobotSingleCharTokenType.END_OF_LINE };
                assertThat(output.getTokens()).hasSize(4);
            }
            RobotSingleCharTokenType[] expectedSequenceOfTypes = extendByArrayNTimes(
                    types, crSequence, 1);
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisksInside_and_carriageReturnFollowingByLineFeed() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\r\n");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();
        RobotSingleCharTokenType[] types = new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK, RobotSingleCharTokenType.SINGLE_ASTERISK };

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            RobotSingleCharTokenType[] crSequence;
            if ((charIndex + 1) % 2 == 0) {
                crSequence = new RobotSingleCharTokenType[] {
                        RobotSingleCharTokenType.CARRIAGE_RETURN,
                        RobotSingleCharTokenType.LINE_FEED, RobotSingleCharTokenType.END_OF_LINE };
                assertThat(output.getTokens()).hasSize(5);
            } else {
                crSequence = new RobotSingleCharTokenType[] {
                        RobotSingleCharTokenType.CARRIAGE_RETURN,
                        RobotSingleCharTokenType.END_OF_LINE };
                assertThat(output.getTokens()).hasSize(4);
            }
            RobotSingleCharTokenType[] expectedSequenceOfTypes = extendByArrayNTimes(
                    types, crSequence, 1);
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisksInside_and_weProcessingLineFeeds() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\n\n\n\n\n");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();
        RobotSingleCharTokenType[] types = new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK, RobotSingleCharTokenType.SINGLE_ASTERISK };
        RobotSingleCharTokenType[] crSequence = new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.LINE_FEED, RobotSingleCharTokenType.END_OF_LINE };

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            assertThat(output.getTokens()).hasSize((charIndex + 2) * 2);
            RobotSingleCharTokenType[] expectedSequenceOfTypes = extendByArrayNTimes(
                    types, crSequence, charIndex + 1);
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoAsterisksInside_and_weProccessingCarriageReturns() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\r\r\r\r\r");
        TokenOutput output = createTokenOutputWithTwoAsterisksInside();
        RobotSingleCharTokenType[] types = new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK, RobotSingleCharTokenType.SINGLE_ASTERISK };
        RobotSingleCharTokenType[] crSequence = new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.CARRIAGE_RETURN, RobotSingleCharTokenType.END_OF_LINE };

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            assertThat(output.getTokens()).hasSize((charIndex + 2) * 2);
            RobotSingleCharTokenType[] expectedSequenceOfTypes = extendByArrayNTimes(
                    types, crSequence, charIndex + 1);
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    private RobotSingleCharTokenType[] extendByArrayNTimes(RobotSingleCharTokenType[] types,
            RobotSingleCharTokenType[] sequenceToAddManyTimes, int i) {
        List<RobotSingleCharTokenType> tempTypes = new LinkedList<>();
        tempTypes.addAll(Arrays.asList(types));
        for (int j = 0; j < i; j++) {
            for (int k = 0; k < sequenceToAddManyTimes.length; k++) {
                tempTypes.add(sequenceToAddManyTimes[k]);
            }
        }
        RobotSingleCharTokenType[] buildTypes = tempTypes.toArray(new RobotSingleCharTokenType[0]);
        tempTypes.clear();

        return buildTypes;
    }


    @Test
    public void test_match_forEmptyTokensStore_and_LineFeedFollowingByCarriageReturn() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\n\r");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            // plus one because we have also End-Of-Line token
            assertThat(output.getTokens()).hasSize(charIndex + 2);
            RobotSingleCharTokenType[] types;
            if ((charIndex + 1) % 2 == 0) {
                types = new RobotSingleCharTokenType[] { RobotSingleCharTokenType.LINE_FEED,
                        RobotSingleCharTokenType.CARRIAGE_RETURN,
                        RobotSingleCharTokenType.END_OF_LINE };
            } else {
                types = new RobotSingleCharTokenType[] { RobotSingleCharTokenType.LINE_FEED,
                        RobotSingleCharTokenType.END_OF_LINE };
            }
            assertTokens(output, types, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokensStore_and_CarriageReturnFollowingByLineFeed() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\r\n");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            // plus one because we have also End-Of-Line token
            assertThat(output.getTokens()).hasSize(charIndex + 2);
            RobotSingleCharTokenType[] types;
            if ((charIndex + 1) % 2 == 0) {
                types = new RobotSingleCharTokenType[] { RobotSingleCharTokenType.CARRIAGE_RETURN,
                        RobotSingleCharTokenType.LINE_FEED, RobotSingleCharTokenType.END_OF_LINE };
            } else {
                types = new RobotSingleCharTokenType[] { RobotSingleCharTokenType.CARRIAGE_RETURN,
                        RobotSingleCharTokenType.END_OF_LINE };
            }
            assertTokens(output, types, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokensStore_and_onlyFiveLineFeeds() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\n\n\n\n\n");
        TokenOutput output = new TokenOutput();
        RobotSingleCharTokenType[] types = new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.LINE_FEED, RobotSingleCharTokenType.END_OF_LINE };

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            // plus one because we have also End-Of-Line token
            assertThat(output.getTokens()).hasSize((charIndex + 1) * 2);
            assertTokens(output, types, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokensStore_and_onlyFiveCarriageReturns() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("\r\r\r\r\r");
        TokenOutput output = new TokenOutput();
        RobotSingleCharTokenType[] types = new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.CARRIAGE_RETURN, RobotSingleCharTokenType.END_OF_LINE };

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            // plus one because we have also End-Of-Line token
            assertThat(output.getTokens()).hasSize((charIndex + 1) * 2);
            assertTokens(output, types, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forNotEndOfLineChars_shouldAlwaysReturn_FALSE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("abcdefgh");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isFalse();
            assertIsTotalEmpty(output);
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
