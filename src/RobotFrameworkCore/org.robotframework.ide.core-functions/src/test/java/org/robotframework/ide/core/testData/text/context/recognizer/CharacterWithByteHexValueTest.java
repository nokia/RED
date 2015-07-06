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
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see CharacterWithByteHexValue
 */
public class CharacterWithByteHexValueTest extends ARecognizerTest {

    public CharacterWithByteHexValueTest() {
        super(CharacterWithByteHexValue.class);
    }


    @Test
    public void testPartOfWordIsHexTheNextIsJustLetter()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterX = "xff" + "notNumber";
        assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_upperCaseLetterF_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "F";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_lowerCaseLetterF_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "f";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_upperCaseLetterD_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "D";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_lowerCaseLetterD_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "d";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_upperCaseLetterC_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "C";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_lowerCaseLetterC_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "c";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_upperCaseLetterB_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "B";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_lowerCaseLetterB_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "b";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_upperCaseLetterA_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "A";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_numbersFrom_0_to_9_nextIs_lowerCaseLetterA_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "x" + j + "a";
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_upperCaseLetterF_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xF" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_lowerCaseLetterF_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xf" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_upperCaseLetterD_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xD" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_lowerCaseLetterD_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xd" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_upperCaseLetterC_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xC" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_lowerCaseLetterC_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xc" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_upperCaseLetterB_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xB" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_lowerCaseLetterB_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xb" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_upperCaseLetterA_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xA" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_theFirstNibbleIs_lowerCaseLetterA_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int j = 0; j < 10; j++) {
            String additionalTextStartsWithLetterX = "xa" + j;
            assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
        }
    }


    @Test
    public void testCorrectHexByteValue_numbersFrom_0_to_9_allPossibilities()
            throws FileNotFoundException, IOException {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                String additionalTextStartsWithLetterX = "x" + i + "" + j;
                assertForSingleTextWithLetterX_atTheBeginning(additionalTextStartsWithLetterX);
            }
        }
    }


    private void assertForSingleTextWithLetterX_atTheBeginning(
            String additionalTextStartsWithLetterX)
            throws FileNotFoundException, IOException {
        // prepare
        String text = "\\" + additionalTextStartsWithLetterX;
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
                new LinearPositionMarker(1, 1),
                new String[] { additionalTextStartsWithLetterX });
    }


    @Test
    public void test_onlyEscapeSmallX_letterG_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\xg";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeSmallX_letterA_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\xa";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeUpperCaseXnumberZeroTwice_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\X00";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeSmallXnumberZero_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\x0";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeSmallX_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\x";
        assertForIncorrectData(text);
    }


    @Test
    public void test_onlyEscapeBiggerX_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\X";
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
                SimpleRobotContextType.CHAR_WITH_BYTE_HEX_VALUE);
    }
}
