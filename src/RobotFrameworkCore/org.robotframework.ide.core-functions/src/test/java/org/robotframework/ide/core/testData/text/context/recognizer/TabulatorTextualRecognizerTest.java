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
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see TabulatorTextualRecognizer
 */
public class TabulatorTextualRecognizerTest extends ARecognizerTest {

    public TabulatorTextualRecognizerTest() {
        super(TabulatorTextualRecognizer.class);
    }


    @Test
    public void test_backslashFollowingWord_Tab_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterT = "Tab";
        assertForSingleTextWithLetterT_atTheBeginning(additionalTextStartsWithLetterT);
    }


    @Test
    public void test_backslashFollowingWord_tab_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterT = "tab";
        assertForSingleTextWithLetterT_atTheBeginning(additionalTextStartsWithLetterT);
    }


    @Test
    public void test_backslashFollowingLetter_T_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterT = "T";
        assertForSingleTextWithLetterT_atTheBeginning(additionalTextStartsWithLetterT);
    }


    @Test
    public void test_backslashFollowingLetter_t_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterT = "t";
        assertForSingleTextWithLetterT_atTheBeginning(additionalTextStartsWithLetterT);
    }


    private void assertForSingleTextWithLetterT_atTheBeginning(
            String additionalTextStartsWithLetterT)
            throws FileNotFoundException, IOException {
        // prepare
        String text = "\\" + additionalTextStartsWithLetterT;
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
                new String[] { additionalTextStartsWithLetterT });
    }


    @Test
    public void test_escapedBackslashAndThen_tabulatorWord_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\\\tabulator";
        assertForIncorrectData(text);
    }


    @Test
    public void test_escapedAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\*";
        assertForIncorrectData(text);
    }


    @Test
    public void test_noTLetterFollowingBackslashCharacter_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar foobar";
        assertForIncorrectData(text);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.TABULATOR_TEXT);
    }
}
