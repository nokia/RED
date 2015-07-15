package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

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
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.ARecognizerTest;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.UnicodeCharacterWithHexValue;
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
 * @see UnicodeCharacterWithHexValue
 */
public class UnicodeCharacterWithHexValueTest extends ARecognizerTest {

    public UnicodeCharacterWithHexValueTest() {
        super(UnicodeCharacterWithHexValue.class);
    }


    @Test
    public void testCorrectHexValue_numberFrom_0010FFFF()
            throws FileNotFoundException, IOException {
        assertForSingleTextWithLetterU_atTheBeginning("U0010FFFF");
    }


    @Test
    public void testCorrectHexValue_numberFrom_00000000()
            throws FileNotFoundException, IOException {
        assertForSingleTextWithLetterU_atTheBeginning("U00000000");
    }


    @Test
    public void test_valueOutOfRange_110000() throws FileNotFoundException,
            IOException {
        assertForIncorrectData("U00110000");
    }


    @Test
    public void testCorrectHexValue_numberFrom_00001010()
            throws FileNotFoundException, IOException {
        assertForSingleTextWithLetterU_atTheBeginning("U00001010");
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
    public void test_emptyText_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "";
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
                SimpleRobotContextType.UNICODE_CHAR_WITH_HEX_VALUE);
    }
}
