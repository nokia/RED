package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContinuePreviousLineRecognizer
 */
public class ContinuePreviousLineRecognizerTest extends ARecognizerTest {

    public ContinuePreviousLineRecognizerTest() {
        super(ContinuePreviousLineRecognizer.class);
    }


    @Test
    public void test_foobarWordAndThenThreeDotsOnly_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {

        // prepare
        String prefix = "foobar";
        TokenOutput tokenOutput = createTokenOutput(prefix + "...");

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertTheSameLinesContext(recognize,
                OneLineSingleRobotContextPart.class, 1);

        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(0))
                        .getContextTokens(),
                new IRobotTokenType[] { RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS },
                0, new FilePosition(1, prefix.length() + 1), new String[] {});
    }


    @Test
    public void test_threeDotsBackslashAndThenAgainThreeDots_shouldReturn_twoElements()
            throws FileNotFoundException, IOException {

        // prepare
        TokenOutput tokenOutput = createTokenOutput("...\\...");

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertTheSameLinesContext(recognize,
                OneLineSingleRobotContextPart.class, 2);

        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(0))
                        .getContextTokens(),
                new IRobotTokenType[] { RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS },
                0, new FilePosition(1, 1), new String[] {});

        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(1))
                        .getContextTokens(),
                new IRobotTokenType[] { RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS },
                0, new FilePosition(1, 5), new String[] {});
    }


    @Test
    public void test_fourDotsOnly_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {

        // prepare
        TokenOutput tokenOutput = createTokenOutput("....");

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertTheSameLinesContext(recognize,
                OneLineSingleRobotContextPart.class, 1);

        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(0))
                        .getContextTokens(),
                new IRobotTokenType[] { MultipleCharTokenType.MORE_THAN_THREE_DOTS },
                0, new FilePosition(1, 1), new String[] {});
    }


    @Test
    public void test_threeDotsOnly_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {

        // prepare
        TokenOutput tokenOutput = createTokenOutput("...");

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertTheSameLinesContext(recognize,
                OneLineSingleRobotContextPart.class, 1);

        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(0))
                        .getContextTokens(),
                new IRobotTokenType[] { RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS },
                0, new FilePosition(1, 1), new String[] {});
    }


    @Test
    public void test_oneDotAndAfterBackslashAndTwoDots_shouldReturnEmptyList()
            throws FileNotFoundException, IOException {
        assertForIncorrectData(".\\..");
    }


    @Test
    public void test_twoDots_shouldReturnEmptyList()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("..");
    }


    @Test
    public void test_oneDot_shouldReturnEmptyList()
            throws FileNotFoundException, IOException {
        assertForIncorrectData(".");
    }


    @Test
    public void test_noDots_shouldReturnEmptyList()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("foobar");
    }


    @Test
    public void test_createContext() {
        LineTokenPosition pos = mock(LineTokenPosition.class);
        when(pos.getLineNumber()).thenReturn(1);

        OneLineSingleRobotContextPart l = ((ContinuePreviousLineRecognizer) context)
                .createContext(pos);
        assertThat(l).isNotNull();
        assertThat(l.getLineNumber()).isEqualTo(1);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.CONTINUE_PREVIOUS_LINE_DECLARATION);
    }
}
