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
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
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
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.RANGE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_IN_willBeRecognizedAsCorrectWordType() {
        String text = "IN";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.IN_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_FOR_willBeRecognizedAsCorrectWordType() {
        String text = "FOR";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.FOR_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Return_willBeRecognizedAsCorrectWordType() {
        String text = "Return";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.RETURN_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Arguments_willBeRecognizedAsCorrectWordType() {
        String text = "Arguments";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.ARGUMENTS_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Timeout_willBeRecognizedAsCorrectWordType() {
        String text = "Timeout";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.TIMEOUT_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Template_willBeRecognizedAsCorrectWordType() {
        String text = "Template";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.TEMPLATE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Tags_willBeRecognizedAsCorrectWordType() {
        String text = "Tags";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.TAGS_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Default_willBeRecognizedAsCorrectWordType() {
        String text = "Default";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.DEFAULT_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Force_willBeRecognizedAsCorrectWordType() {
        String text = "Force";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.FORCE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Postcondition_willBeRecognizedAsCorrectWordType() {
        String text = "Postcondition";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.POSTCONDITION_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Precondition_willBeRecognizedAsCorrectWordType() {
        String text = "Precondition";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.PRECONDITION_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Teardown_willBeRecognizedAsCorrectWordType() {
        String text = "Teardown";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.TEARDOWN_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Setup_willBeRecognizedAsCorrectWordType() {
        String text = "Setup";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.SETUP_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Suite_willBeRecognizedAsCorrectWordType() {
        String text = "Suite";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.SUITE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Documentation_willBeRecognizedAsCorrectWordType() {
        String text = "Documentation";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.DOCUMENTATION_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Resource_willBeRecognizedAsCorrectWordType() {
        String text = "Resource";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.RESOURCE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_NAME_willBeRecognizedAsCorrectWordType() {
        String text = "NAME";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.NAME_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_WITH_willBeRecognizedAsCorrectWordType() {
        String text = "WITH";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.WITH_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Library_willBeRecognizedAsCorrectWordType() {
        String text = "Library";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.LIBRARY_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_User_willBeRecognizedAsCorrectWordType() {
        String text = "User";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.USER_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Keywords_willBeRecognizedAsCorrectWordType() {
        String text = "Keywords";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.KEYWORDS_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Keyword_willBeRecognizedAsCorrectWordType() {
        String text = "Keyword";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.KEYWORD_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Cases_willBeRecognizedAsCorrectWordType() {
        String text = "Cases";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.CASES_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Case_willBeRecognizedAsCorrectWordType() {
        String text = "Case";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.CASE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Test_willBeRecognizedAsCorrectWordType() {
        String text = "Test";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.TEST_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Variables_willBeRecognizedAsCorrectWordType() {
        String text = "Variables";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.VARIABLES_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Variable_willBeRecognizedAsCorrectWordType() {
        String text = "Variable";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.VARIABLE_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Metadata_willBeRecognizedAsCorrectWordType() {
        String text = "Metadata";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.METADATA_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Settings_willBeRecognizedAsCorrectWordType() {
        String text = "Settings";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.SETTINGS_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


    @Test
    public void test_if_Setting_willBeRecognizedAsCorrectWordType() {
        String text = "Setting";
        RobotType[] expectedSequenceOfTypes = new RobotType[] { RobotWordType.SETTING_WORD };
        assertThatCorrespondingMatcherWillBeUsed(text, expectedSequenceOfTypes);
    }


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
