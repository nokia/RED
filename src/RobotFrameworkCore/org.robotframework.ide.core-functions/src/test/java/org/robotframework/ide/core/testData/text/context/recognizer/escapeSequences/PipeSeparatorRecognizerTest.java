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
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see PipeSeparatorRecognizer
 */
public class PipeSeparatorRecognizerTest extends ARecognizerTest {

    public PipeSeparatorRecognizerTest() {
        super(PipeSeparatorRecognizer.class);
    }


    @Test
    public void test_lineFromTestCaseSection_withEscapedSpace()
            throws FileNotFoundException, IOException {
        // prepare
        TokenOutput tokenOutput = createTokenOutput("| | Log | \\ | INFO |  |  \t|");

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertThat(recognize).hasSize(7);

        IContextElement element = recognize.get(0);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextOne = (OneLineSingleRobotContextPart) element;
        assertThat(contextOne.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextOne.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotSingleCharTokenType.SINGLE_SPACE }, 0,
                new FilePosition(1, 1), new String[] {});

        IContextElement elementTwo = recognize.get(1);
        assertThat(elementTwo)
                .isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextTwo = (OneLineSingleRobotContextPart) elementTwo;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextTwo.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotSingleCharTokenType.SINGLE_SPACE }, 0,
                new FilePosition(1, 3), new String[] {});

        IContextElement elementThree = recognize.get(2);
        assertThat(elementThree).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextThree = (OneLineSingleRobotContextPart) elementThree;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextThree.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotSingleCharTokenType.SINGLE_SPACE }, 0,
                new FilePosition(1, 8), new String[] {});

        IContextElement elementFour = recognize.get(3);
        assertThat(elementFour).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextFour = (OneLineSingleRobotContextPart) elementFour;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextFour.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotSingleCharTokenType.SINGLE_SPACE }, 0,
                new FilePosition(1, 13), new String[] {});

        IContextElement elementFive = recognize.get(4);
        assertThat(elementFive).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextFive = (OneLineSingleRobotContextPart) elementFive;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextFive.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, 19), new String[] {});

        IContextElement elementSix = recognize.get(5);
        assertThat(elementSix)
                .isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextSix = (OneLineSingleRobotContextPart) elementSix;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextSix.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, 23), new String[] {});

        IContextElement elementSeven = recognize.get(6);
        assertThat(elementSeven).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextSeven = (OneLineSingleRobotContextPart) elementSeven;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextSeven.getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_TABULATOR,
                        RobotSingleCharTokenType.SINGLE_PIPE }, 0,
                new FilePosition(1, 26), new String[] {});
    }


    @Test
    public void test_lineFromTestCaseSection_withEscapedPipe()
            throws FileNotFoundException, IOException {
        // prepare
        TokenOutput tokenOutput = createTokenOutput("| | Log | \\|  | INFO |  |  \t|");

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertThat(recognize).hasSize(7);

        IContextElement element = recognize.get(0);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextOne = (OneLineSingleRobotContextPart) element;
        assertThat(contextOne.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextOne.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotSingleCharTokenType.SINGLE_SPACE }, 0,
                new FilePosition(1, 1), new String[] {});

        IContextElement elementTwo = recognize.get(1);
        assertThat(elementTwo)
                .isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextTwo = (OneLineSingleRobotContextPart) elementTwo;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextTwo.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotSingleCharTokenType.SINGLE_SPACE }, 0,
                new FilePosition(1, 3), new String[] {});

        IContextElement elementThree = recognize.get(2);
        assertThat(elementThree).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextThree = (OneLineSingleRobotContextPart) elementThree;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextThree.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotSingleCharTokenType.SINGLE_SPACE }, 0,
                new FilePosition(1, 8), new String[] {});

        IContextElement elementFour = recognize.get(3);
        assertThat(elementFour).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextFour = (OneLineSingleRobotContextPart) elementFour;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextFour.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE,
                        RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotSingleCharTokenType.SINGLE_SPACE }, 0,
                new FilePosition(1, 13), new String[] {});

        IContextElement elementFive = recognize.get(4);
        assertThat(elementFive).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextFive = (OneLineSingleRobotContextPart) elementFive;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextFive.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, 21), new String[] {});

        IContextElement elementSix = recognize.get(5);
        assertThat(elementSix)
                .isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextSix = (OneLineSingleRobotContextPart) elementSix;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextSix.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, 25), new String[] {});

        IContextElement elementSeven = recognize.get(6);
        assertThat(elementSeven).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextSeven = (OneLineSingleRobotContextPart) elementSeven;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextSeven.getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_TABULATOR,
                        RobotSingleCharTokenType.SINGLE_PIPE }, 0,
                new FilePosition(1, 28), new String[] {});
    }


    @Test
    public void test_word_doubleSpace_word_space()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "word";
        String text = "  word ";
        TokenOutput tokenOutput = createTokenOutput(prefix + text);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertThat(recognize).hasSize(2);

        IContextElement element = recognize.get(0);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextOne = (OneLineSingleRobotContextPart) element;
        assertThat(contextOne.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(contextOne.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, prefix.length() + 1), new String[] {});

        IContextElement elementTwo = recognize.get(1);
        assertThat(elementTwo)
                .isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextTwo = (OneLineSingleRobotContextPart) elementTwo;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(
                contextTwo.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE },
                0, new FilePosition(1, 11), new String[] {});
    }


    @Test
    public void test_word_doubleSpace_word() throws FileNotFoundException,
            IOException {
        // prepare
        String prefix = "word";
        String text = "  word";
        TokenOutput tokenOutput = createTokenOutput(prefix + text);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertThat(recognize).hasSize(1);

        IContextElement element = recognize.get(0);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextOne = (OneLineSingleRobotContextPart) element;
        assertThat(contextOne.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(contextOne.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, prefix.length() + 1), new String[] {});

    }


    @Test
    public void test_doubleSpace_thenSpace_pipe_doubleSpace_space_word()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "  ";
        String text = " | ";
        String text2 = "  word";
        String p = prefix + text;
        TokenOutput tokenOutput = createTokenOutput(p + text2);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertThat(recognize).hasSize(3);

        IContextElement element = recognize.get(0);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextOne = (OneLineSingleRobotContextPart) element;
        assertThat(contextOne.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(contextOne.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, 1), new String[] {});

        IContextElement elementTwo = recognize.get(1);
        assertThat(elementTwo)
                .isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextTwo = (OneLineSingleRobotContextPart) elementTwo;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextTwo.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotWordType.DOUBLE_SPACE }, 0, new FilePosition(1,
                        prefix.length() + 1), new String[] {});

        IContextElement elementThree = recognize.get(2);
        assertThat(elementThree).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextThree = (OneLineSingleRobotContextPart) elementThree;
        assertThat(contextThree.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(
                contextThree.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE },
                0, new FilePosition(1, 7), new String[] {});
    }


    @Test
    public void test_doubleSpace_thenSpace_pipe_doubleSpace_space()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "  ";
        String text = " | ";
        String text2 = "  ";
        String p = prefix + text;
        TokenOutput tokenOutput = createTokenOutput(p + text2);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertThat(recognize).hasSize(3);

        IContextElement element = recognize.get(0);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextOne = (OneLineSingleRobotContextPart) element;
        assertThat(contextOne.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(contextOne.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, 1), new String[] {});

        IContextElement elementTwo = recognize.get(1);
        assertThat(elementTwo)
                .isInstanceOf(OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextTwo = (OneLineSingleRobotContextPart) elementTwo;
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
        assertTokensForUnknownWords(contextTwo.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotSingleCharTokenType.SINGLE_PIPE,
                        RobotWordType.DOUBLE_SPACE }, 0, new FilePosition(1,
                        prefix.length() + 1), new String[] {});

        IContextElement elementThree = recognize.get(2);
        assertThat(elementThree).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart contextThree = (OneLineSingleRobotContextPart) elementThree;
        assertThat(contextThree.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(
                contextThree.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE },
                0, new FilePosition(1, 7), new String[] {});
    }


    @Test
    public void test_double_space_pipe() throws FileNotFoundException,
            IOException {
        String text = "  |";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotWordType.DOUBLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE });
    }


    @Test
    public void test_tabulator_pipe() throws FileNotFoundException, IOException {
        String text = "\t|";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_PIPE });
    }


    @Test
    public void test_space_pipe() throws FileNotFoundException, IOException {
        String text = " |";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE });
    }


    @Test
    public void test_double_space_pipe_double_space()
            throws FileNotFoundException, IOException {
        String text = "  |  ";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotWordType.DOUBLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotWordType.DOUBLE_SPACE });
    }


    @Test
    public void test_double_space_pipe_space() throws FileNotFoundException,
            IOException {
        String text = "  | ";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotWordType.DOUBLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_tabulator_pipe_tabulator() throws FileNotFoundException,
            IOException {
        String text = "\t|\t";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotSingleCharTokenType.SINGLE_TABULATOR });
    }


    @Test
    public void test_tabulator_pipe_double_space()
            throws FileNotFoundException, IOException {
        String text = "\t|  ";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotWordType.DOUBLE_SPACE });
    }


    @Test
    public void test_tabulator_pipe_space() throws FileNotFoundException,
            IOException {
        String text = " | ";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_space_pipe_tabulator() throws FileNotFoundException,
            IOException {
        String text = " |\t";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotSingleCharTokenType.SINGLE_TABULATOR });
    }


    @Test
    public void test_space_pipe_double_space() throws FileNotFoundException,
            IOException {
        String text = " |  ";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotWordType.DOUBLE_SPACE });
    }


    @Test
    public void test_space_pipe_space() throws FileNotFoundException,
            IOException {
        String text = " | ";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_double_space_pipe_word_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "  |word";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotWordType.DOUBLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE });
    }


    @Test
    public void test_tabulator_pipe_word_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "\t|word";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_PIPE });
    }


    @Test
    public void test_space_pipe_word_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = " |word";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_PIPE });
    }


    @Test
    public void test_pipe_double_space_word() throws FileNotFoundException,
            IOException {
        String text = "|  word";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotWordType.DOUBLE_SPACE });
    }


    @Test
    public void test_pipe_tabulator_word() throws FileNotFoundException,
            IOException {
        String text = "|\tword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotSingleCharTokenType.SINGLE_TABULATOR });
    }


    @Test
    public void test_pipe_space_word() throws FileNotFoundException,
            IOException {
        String text = "| word";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_PIPE,
                RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_oneSpaceInsideText_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "temp foobar";
        assertForIncorrectData(text);
    }


    @Test
    public void test_pipe_fewWhitespace_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "|word|word word";
        assertForIncorrectData(text);
    }


    @Test
    public void test_pipe_noWhitespace_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "|word";
        assertForIncorrectData(text);
    }


    @Test
    public void test_noSeparators_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "temp";
        assertForIncorrectData(text);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.PIPE_SEPARATED);
    }
}
