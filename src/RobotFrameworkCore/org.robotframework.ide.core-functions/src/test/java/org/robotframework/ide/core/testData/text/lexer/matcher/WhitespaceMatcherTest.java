package org.robotframework.ide.core.testData.text.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertIsTotalEmpty;

import java.nio.CharBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;
import org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper;


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
    public void test_match_forEmptyTokenStore_andWithOneSingleSpaceInTempBuffer_shouldReturn_TRUE() {
        // prepare
        CharBuffer tempBuffer = CharBuffer.wrap(" ");
        TokenOutput output = new TokenOutput();

        // execute & verify
        assertThat(matcher.match(output, tempBuffer, 0)).isTrue();
        TokenOutputAsserationHelper.assertCurrentPosition(output);
        TokenOutputAsserationHelper.assertPositionMarkers(output);

        TokenOutputAsserationHelper.assertTokens(output,
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
