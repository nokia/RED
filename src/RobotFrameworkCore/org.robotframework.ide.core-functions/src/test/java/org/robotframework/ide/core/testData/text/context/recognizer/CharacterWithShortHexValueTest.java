package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokensForUnknownWords;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see CharacterWithShortHexValue
 */
public class CharacterWithShortHexValueTest extends ARecognizerTest {

    public CharacterWithShortHexValueTest() {
        super(CharacterWithShortHexValue.class);
    }


    @Test
    public void testCorrectHexShortValue_lettersA9A8()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterX = "uA9A8";
        assertForSingleTextWithLetterU_atTheBeginning(additionalTextStartsWithLetterX);
    }


    @Test
    public void testCorrectHexShortValue_lettersAAAA()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterX = "uAAAA";
        assertForSingleTextWithLetterU_atTheBeginning(additionalTextStartsWithLetterX);
    }


    @Test
    public void testCorrectHexShortValue_numberFrom_1234()
            throws FileNotFoundException, IOException {
        assertForSingleTextWithLetterU_atTheBeginning("u1234");
    }


    private void assertForSingleTextWithLetterU_atTheBeginning(
            String additionalTextStartsWithLetterU)
            throws FileNotFoundException, IOException {
        // prepare
        String text = "\\" + additionalTextStartsWithLetterU;
        TokenOutput tokenOutput = createTokenOutput(text);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertTheSameLinesContext(recognize,
                OneLineSingleRobotContextPart.class, 1);
        assertThat(recognize.get(0).getType()).isEqualTo(
                context.getContextType());

        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(0))
                        .getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH,
                        RobotWordType.UNKNOWN_WORD }, 0,
                new FilePosition(1, 1),
                new String[] { additionalTextStartsWithLetterU });
    }


    @Test
    public void test_onlyEscapeSmallU_letterG_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\ug";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeSmallU_letterA_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\ua";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeUpperCaseUnumberZeroTwice_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\U00";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeSmallUnumberZero_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\u0";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeSmallU_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\u";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeBiggerU_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\U";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeAndTrashText_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\d";
        assertForIncorrectData(text);
    }


    @Test
    public void test_noNLetterFollowingBackslashCharacter_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar foobar";
        assertForIncorrectData(text);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.CHAR_WITH_SHORT_HEX_VALUE);
    }
}
