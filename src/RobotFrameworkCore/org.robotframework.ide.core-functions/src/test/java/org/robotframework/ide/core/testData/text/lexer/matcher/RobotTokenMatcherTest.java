package org.robotframework.ide.core.testData.text.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertCurrentPosition;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertPositionMarkers;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokens;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokensForUnknownWords;

import java.nio.CharBuffer;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.NumberType;
import org.robotframework.ide.core.testData.text.lexer.RobotTimeType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;
import org.robotframework.ide.core.testHelpers.ExactlyTheSameClassComperator;


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
    public void test_if_numberTokenWillBeRecognizedCorrectly() {
        String text = "-1";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { NumberType.NUMBER_WITH_SIGN };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_timeTokenWillBeRecognizedCorrectly() {
        String text = "hour";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotTimeType.HOUR };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_declaredSingleCharMatchers_areCorrect() {
        assertThat(matcher.getDeclaredSingleCharMatchers())
                .usingElementComparator(new ExactlyTheSameClassComperator())
                .containsExactlyElementsOf(
                        Arrays.asList(new EndOfLineMatcher(),
                                new WhitespaceMatcher(), new PipeMatcher(),
                                new AsteriskMatcher(),
                                new HashCommentMatcher(),
                                new ScalarVariableBeginSignMatcher(),
                                new ListVariableBeginSignMatcher(),
                                new EnvironmentVariableBeginSignMatcher(),
                                new DictionaryVariableBeginSignMatcher(),
                                new EqualSignMatcher(),
                                new VariableBeginCurlySignMatcher(),
                                new VariableEndCurlySignMatcher(),
                                new IndexBeginSquareSignMatcher(),
                                new IndexEndSquareSignMatcher(),
                                new ColonSignMatcher(),
                                new QuoteMarkSignMatcher(),
                                new DotSignMatcher(),
                                new EscapeBackslashSignMatcher()));
    }


    @Test
    public void test_if_RANGE_willBeRecognizedAsCorrectWordType() {
        String text = "RANGE";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.RANGE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_IN_willBeRecognizedAsCorrectWordType() {
        String text = "IN";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.IN_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_FOR_willBeRecognizedAsCorrectWordType() {
        String text = "FOR";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.FOR_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Return_willBeRecognizedAsCorrectWordType() {
        String text = "Return";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.RETURN_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Arguments_willBeRecognizedAsCorrectWordType() {
        String text = "Arguments";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.ARGUMENTS_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Timeout_willBeRecognizedAsCorrectWordType() {
        String text = "Timeout";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.TIMEOUT_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Template_willBeRecognizedAsCorrectWordType() {
        String text = "Template";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.TEMPLATE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Tags_willBeRecognizedAsCorrectWordType() {
        String text = "Tags";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.TAGS_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Default_willBeRecognizedAsCorrectWordType() {
        String text = "Default";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.DEFAULT_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Force_willBeRecognizedAsCorrectWordType() {
        String text = "Force";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.FORCE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Postcondition_willBeRecognizedAsCorrectWordType() {
        String text = "Postcondition";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.POSTCONDITION_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Precondition_willBeRecognizedAsCorrectWordType() {
        String text = "Precondition";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.PRECONDITION_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Teardown_willBeRecognizedAsCorrectWordType() {
        String text = "Teardown";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.TEARDOWN_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Setup_willBeRecognizedAsCorrectWordType() {
        String text = "Setup";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.SETUP_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Suite_willBeRecognizedAsCorrectWordType() {
        String text = "Suite";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.SUITE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Documentation_willBeRecognizedAsCorrectWordType() {
        String text = "Documentation";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.DOCUMENTATION_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Resource_willBeRecognizedAsCorrectWordType() {
        String text = "Resource";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.RESOURCE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_NAME_willBeRecognizedAsCorrectWordType() {
        String text = "NAME";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.NAME_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_WITH_willBeRecognizedAsCorrectWordType() {
        String text = "WITH";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.WITH_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Library_willBeRecognizedAsCorrectWordType() {
        String text = "Library";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.LIBRARY_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_User_willBeRecognizedAsCorrectWordType() {
        String text = "User";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.USER_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Keywords_willBeRecognizedAsCorrectWordType() {
        String text = "Keywords";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.KEYWORDS_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Keyword_willBeRecognizedAsCorrectWordType() {
        String text = "Keyword";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.KEYWORD_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Cases_willBeRecognizedAsCorrectWordType() {
        String text = "Cases";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.CASES_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Case_willBeRecognizedAsCorrectWordType() {
        String text = "Case";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.CASE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Test_willBeRecognizedAsCorrectWordType() {
        String text = "Test";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.TEST_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Variables_willBeRecognizedAsCorrectWordType() {
        String text = "Variables";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.VARIABLES_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Variable_willBeRecognizedAsCorrectWordType() {
        String text = "Variable";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.VARIABLE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Metadata_willBeRecognizedAsCorrectWordType() {
        String text = "Metadata";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.METADATA_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Settings_willBeRecognizedAsCorrectWordType() {
        String text = "Settings";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.SETTINGS_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Setting_willBeRecognizedAsCorrectWordType() {
        String text = "Setting";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotWordType.SETTING_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifFoobarWillChangeToUnknownWord() {
        TokenOutput tokenOutput = new TokenOutput();
        String text = "foobar";
        CharBuffer tempBuffer = CharBuffer.wrap(text);

        matcher = new RobotTokenMatcher(tokenOutput);
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.UNKNOWN };
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
                new IRobotTokenType[] { RobotWordType.UNKNOWN_WORD }, 0, 1,
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
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.UNKNOWN };
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
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] {
                RobotWordType.DOUBLE_ESCAPE_BACKSLASH,
                RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH };
        String text = "\\\\\\";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifDotSignCanBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_DOT, RobotSingleCharTokenType.LINE_FEED,
                RobotSingleCharTokenType.END_OF_LINE, RobotWordType.EMPTY_CELL_DOTS,
                RobotSingleCharTokenType.LINE_FEED, RobotSingleCharTokenType.END_OF_LINE,
                RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS };
        String text = ".\n..\n...";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifQuoteMarkSignCanBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_QUOTE_MARK };
        String text = "\"";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifColonCanBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_COLON };
        String text = ":";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifIndexSquareStartAndStopCanBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_POSSITION_INDEX_BEGIN_SQUARE_BRACKET,
                RobotSingleCharTokenType.SINGLE_POSSITION_INDEX_END_SQUARE_BRACKET };
        String text = "[]";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifVariableCurlyStartAndStopCanBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET };
        String text = "{}";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifEqualCouldBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_EQUAL };
        String text = "=";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifDictionaryVariableBeginCouldBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_DICTIONARY_BEGIN_AMPERSAND };
        String text = "&";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifEnvironmentVariableBeginCouldBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_ENVIRONMENT_BEGIN_PROCENT };
        String text = "%";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifListVariableBeginCouldBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_LIST_BEGIN_AT };
        String text = "@";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifScalarVariableBeginCouldBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SCALAR_BEGIN_DOLLAR };
        String text = "$";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifHashCommentCouldBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_COMMENT_HASH };
        String text = "#";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifAsteriskCouldBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_ASTERISK };
        String text = "*";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifPipeCouldBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_PIPE };
        String text = "|";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifWhitespaceCouldBeMatch() {
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_SPACE, RobotSingleCharTokenType.SINGLE_TABULATOR };
        String text = " \t";
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_ifEndOfLineCouldBeMatch() {
        String text = "\n";
        IRobotTokenType[] expectedSequenceOfTypes = new IRobotTokenType[] {
                RobotSingleCharTokenType.LINE_FEED, RobotSingleCharTokenType.END_OF_LINE };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    private void assertThatCorrespondingMatcherWillBeUsed(String text,
            IRobotTokenType[] expectedSequenceOfTypes) {
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
