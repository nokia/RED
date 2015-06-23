package org.robotframework.ide.core.testData.text.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertCurrentPosition;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertPositionMarkers;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokens;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokensForUnknownWords;

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
 * @see RobotTokenMatcher
 */
public class RobotTokenMatcherTest {

    @ForClean
    private RobotTokenMatcher matcher;


    @Test
    public void test_ifFoobarWillChangeToUnknownWord() {
        TokenOutput tokenOutput = new TokenOutput();
        String text = "foobar";
        CharBuffer tempBuffer = CharBuffer.wrap(text);

        matcher = new RobotTokenMatcher(tokenOutput);
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.UNKNOWN };
        for (int i = 0; i < tempBuffer.length(); i++) {
            matcher.offerChar(tempBuffer, i);
            assertThat(tokenOutput).isNotNull();
            assertTokensForUnknownWords(tokenOutput, expectedSequenceOfTypes,
                    0, 1, new String[] { text.substring(0, i + 1) });
            assertCurrentPosition(tokenOutput);
            assertPositionMarkers(tokenOutput);
        }

        TokenOutput finalBuildTokens = matcher.buildTokens();
        assertThat(finalBuildTokens).isNotNull();
        assertTokensForUnknownWords(tokenOutput,
                new RobotType[] { RobotWordType.UNKNOWN_WORD }, 0, 1,
                new String[] { text });
        assertCurrentPosition(tokenOutput);
        assertPositionMarkers(tokenOutput);
    }


    @Test
    public void test_ifNotUsedCharsAreConvertedToUnknownToken_recognizedTextSettingButWithoutAnyOtherCharRecognized() {
        TokenOutput tokenOutput = new TokenOutput();
        String text = "setting";
        CharBuffer tempBuffer = CharBuffer.wrap(text);

        matcher = new RobotTokenMatcher(tokenOutput);
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.UNKNOWN };
        for (int i = 0; i < tempBuffer.length(); i++) {
            matcher.offerChar(tempBuffer, i);
            assertThat(tokenOutput).isNotNull();
            assertTokensForUnknownWords(tokenOutput, expectedSequenceOfTypes,
                    0, 1, new String[] { text.substring(0, i + 1) });
            assertCurrentPosition(tokenOutput);
            assertPositionMarkers(tokenOutput);
        }
    }


    @Test
    public void test_ifEscapeBackslashCanBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] {
                RobotWordType.DOUBLE_ESCAPE_BACKSLASH,
                RobotTokenType.SINGLE_ESCAPE_BACKSLASH };
        String text = "\\\\\\";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifDotSignCanBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] {
                RobotTokenType.SINGLE_DOT, RobotTokenType.LINE_FEED,
                RobotTokenType.END_OF_LINE, RobotWordType.EMPTY_CELL_DOTS,
                RobotTokenType.LINE_FEED, RobotTokenType.END_OF_LINE,
                RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS };
        String text = ".\n..\n...";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifQuoteMarkSignCanBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_QUOTE_MARK };
        String text = "\"";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifColonCanBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_COLON };
        String text = ":";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifIndexSquareStartAndStopCanBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] {
                RobotTokenType.SINGLE_POSSITION_INDEX_BEGIN_SQUARE_BRACKET,
                RobotTokenType.SINGLE_POSSITION_INDEX_END_SQUARE_BRACKET };
        String text = "[]";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifVariableCurlyStartAndStopCanBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] {
                RobotTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                RobotTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET };
        String text = "{}";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifEqualCouldBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_EQUAL };
        String text = "=";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifDictionaryVariableBeginCouldBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_DICTIONARY_BEGIN_AMPERSAND };
        String text = "&";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifEnvironmentVariableBeginCouldBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_ENVIRONMENT_BEGIN_PROCENT };
        String text = "%";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifListVariableBeginCouldBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_LIST_BEGIN_AT };
        String text = "@";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifScalarVariableBeginCouldBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_SCALAR_BEGIN_DOLLAR };
        String text = "$";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifHashCommentCouldBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_COMMENT_HASH };
        String text = "#";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifAsteriskCouldBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_ASTERISK };
        String text = "*";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifPipeCouldBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotTokenType.SINGLE_PIPE };
        String text = "|";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifWhitespaceCouldBeMatch() {
        RobotType[] expectedSequenceOfTypes = new RobotType[] {
                RobotTokenType.SINGLE_SPACE, RobotTokenType.SINGLE_TABULATOR };
        String text = " \t";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifEndOfLineCouldBeMatch() {
        String text = "\n";
        RobotType[] expectedSequenceOfTypes = new RobotType[] {
                RobotTokenType.LINE_FEED, RobotTokenType.END_OF_LINE };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    private void assertThatCorrespondingMatcherWillBeUsed(String text,
            RobotType[] expectedSequenceOfTypes) {
        CharBuffer tempBuffer = CharBuffer.wrap(text);
        for (int i = 0; i < tempBuffer.length(); i++) {
            matcher.offerChar(tempBuffer, i);
        }

        TokenOutput tokenOutput = matcher.buildTokens();
        assertThat(tokenOutput).isNotNull();
        assertTokens(tokenOutput, expectedSequenceOfTypes, 0, 1);
        assertCurrentPosition(tokenOutput);
        assertPositionMarkers(tokenOutput);
    }


    @Test
    public void test_ifOutputGeneratedIsNotTheSame() {
        TokenOutput theFirstOutput = matcher.buildTokens();
        assertThat(theFirstOutput).isNotSameAs(matcher.buildTokens());
    }


    @Before
    public void setUp() {
        matcher = new RobotTokenMatcher();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
