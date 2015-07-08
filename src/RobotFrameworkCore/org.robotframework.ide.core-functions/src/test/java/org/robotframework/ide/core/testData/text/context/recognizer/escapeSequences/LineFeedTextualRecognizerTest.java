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
import org.robotframework.ide.core.testData.text.context.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.ARecognizerTest;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.LineFeedTextualRecognizer;
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
 * @see LineFeedTextualRecognizer
 */
public class LineFeedTextualRecognizerTest extends ARecognizerTest {

    public LineFeedTextualRecognizerTest() {
        super(LineFeedTextualRecognizer.class);
    }


    @Test
    public void test_backslashFollowingWord_New_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterN = "New";
        assertForSingleTextWithLetterN_atTheBeginning(additionalTextStartsWithLetterN);
    }


    @Test
    public void test_backslashFollowingWord_new_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterN = "new";
        assertForSingleTextWithLetterN_atTheBeginning(additionalTextStartsWithLetterN);
    }


    @Test
    public void test_backslashFollowingLetter_N_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterN = "N";
        assertForSingleTextWithLetterN_atTheBeginning(additionalTextStartsWithLetterN);
    }


    @Test
    public void test_backslashFollowingLetter_n_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithLetterN = "n";
        assertForSingleTextWithLetterN_atTheBeginning(additionalTextStartsWithLetterN);
    }


    private void assertForSingleTextWithLetterN_atTheBeginning(
            String additionalTextStartsWithLetterN)
            throws FileNotFoundException, IOException {
        // prepare
        String text = "\\" + additionalTextStartsWithLetterN;
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
                new String[] { additionalTextStartsWithLetterN });
    }


    @Test
    public void test_escapedBackslashAndThen_newWord_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\\\new";
        assertForIncorrectData(text);
    }


    @Test
    public void test_escapedAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\*";
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
                SimpleRobotContextType.LINE_FEED_TEXT);
    }
}
