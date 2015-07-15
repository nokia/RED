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
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see EscapedBackslashSign
 */
public class EscapedBackslashSignTest extends ARecognizerTest {

    public EscapedBackslashSignTest() {
        super(EscapedBackslashSign.class);
    }


    @Test
    public void test_backslashFollowing_sign_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String additionalTextStartsWithSign = "\\";
        assertForSingleTextWithBackslash_atTheBeginning(additionalTextStartsWithSign);
    }


    private void assertForSingleTextWithBackslash_atTheBeginning(
            String textInput) throws FileNotFoundException, IOException {
        // prepare
        String text = "\\" + textInput;
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
                new IRobotTokenType[] { RobotWordType.DOUBLE_ESCAPE_BACKSLASH },
                0, new FilePosition(1, 1), new String[] {});
    }


    @Test
    public void test_escapedBackslashSpaceAndThen_escapedBackslash_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\ \\";
        assertForIncorrectData(text);
    }


    @Test
    public void test_escapedAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "\\*";
        assertForIncorrectData(text);
    }


    @Test
    public void test_noBackslashSignFollowingBackslashCharacter_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar foobar";
        assertForIncorrectData(text);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.ESCAPED_BACKSLASH_SIGN);
    }
}
