package org.robotframework.ide.core.testData.text.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertCurrentPosition;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertIsTotalEmpty;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertPositionMarkers;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokens;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.createTokenOutputWithTwoTabulatorsInside;

import java.nio.CharBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see HashCommentMatcher
 */
public class HashCommentMatcherTest {

    @ForClean
    private ISingleCharTokenMatcher matcher;


    @Test
    public void test_match_forTokenStoreWithTwoTabulators_andWithThreeCommentHashInTempBuffer_shouldReturn_MANY_COMMENT_HASH_TOKEN() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("###");
        TokenOutput output = createTokenOutputWithTwoTabulatorsInside();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            IRobotTokenType[] expectedSequenceOfTypes;
            if (charIndex > 0) {
                expectedSequenceOfTypes = new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_TABULATOR,
                        RobotSingleCharTokenType.SINGLE_TABULATOR,
                        MultipleCharTokenType.MANY_COMMENT_HASHS };
                assertThat(output.getTokens()).hasSize(3);
            } else {
                expectedSequenceOfTypes = new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_TABULATOR,
                        RobotSingleCharTokenType.SINGLE_TABULATOR,
                        RobotSingleCharTokenType.SINGLE_COMMENT_HASH };
                assertThat(output.getTokens()).hasSize(3);
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithTrippletCommentHashInTempBuffer_shouldReturn_MANY_COMMENT_HASH_TOKEN() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("###");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            IRobotTokenType[] expectedSequenceOfTypes;
            if (charIndex >= 1) {
                expectedSequenceOfTypes = new IRobotTokenType[] { MultipleCharTokenType.MANY_COMMENT_HASHS };
                assertThat(output.getTokens()).hasSize(1);
            } else {
                expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_COMMENT_HASH };
                assertThat(output.getTokens()).hasSize(1);
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithTwoCommentHashInTempBuffer_shouldReturn_MANY_COMMENT_HASH_TOKEN() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("##");
        TokenOutput output = new TokenOutput();

        // execute & verify
        for (int charIndex = 0; charIndex < tempBuffer.length(); charIndex++) {
            assertThat(matcher.match(output, tempBuffer, charIndex)).isTrue();
            IRobotTokenType[] expectedSequenceOfTypes;
            if ((charIndex + 1) % 2 == 0) {
                expectedSequenceOfTypes = new IRobotTokenType[] { MultipleCharTokenType.MANY_COMMENT_HASHS };
                assertThat(output.getTokens()).hasSize(1);
            } else {
                expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_COMMENT_HASH };
                assertThat(output.getTokens()).hasSize(1);
            }
            assertTokens(output, expectedSequenceOfTypes, 0, 1);
            assertCurrentPosition(output);
            assertPositionMarkers(output);
        }
    }


    @Test
    public void test_match_forTokenStoreWithTwoTabulatorsInside_andWithOneSingleCommentHashInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("#");
        TokenOutput output = createTokenOutputWithTwoTabulatorsInside();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(output, new RobotSingleCharTokenType[] {
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_COMMENT_HASH }, 0, 1);
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithOneSingleCommentHashInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap("#");
        TokenOutput output = new TokenOutput();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        assertCurrentPosition(output);
        assertPositionMarkers(output);

        assertTokens(output,
                new RobotSingleCharTokenType[] { RobotSingleCharTokenType.SINGLE_COMMENT_HASH }, 0,
                1);
    }


    @Test
    public void test_match_forEmptyTokenStore_andWithSpace_shouldReturn_FALSE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap(" ");
        TokenOutput output = new TokenOutput();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isFalse();
        assertIsTotalEmpty(output);
    }


    @Before
    public void setUp() {
        matcher = new HashCommentMatcher();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
