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
 * @see DoubleSpaceOrTabulatorSeparatorRecognizer
 * 
 */
public class DoubleSpaceOrTabulatorSeparatorRecognizerTest extends
        ARecognizerTest {

    public DoubleSpaceOrTabulatorSeparatorRecognizerTest() {
        super(DoubleSpaceOrTabulatorSeparatorRecognizer.class);
    }


    @Test
    public void test_tripple_tabulator_oneShouldBeSetAsSeparatorTheRestAsPrettyAlign()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "\t";
        String text2 = "\t\t";
        TokenOutput tokenOutput = createTokenOutput(text + text2);

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
        OneLineSingleRobotContextPart separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertTokensForUnknownWords(
                separator.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_TABULATOR },
                0, new FilePosition(1, 1), new String[] {});

        element = recognize.get(1);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(separator.getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_TABULATOR,
                        RobotSingleCharTokenType.SINGLE_TABULATOR }, 0,
                new FilePosition(1, text.length() + 1), new String[] {});
    }


    @Test
    public void test_twice_tabulator_oneShouldBeSetAsSeparatorTheSecondAsPrettyAlign()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "\t";
        String text2 = "\t";
        TokenOutput tokenOutput = createTokenOutput(text + text2);

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
        OneLineSingleRobotContextPart separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertTokensForUnknownWords(
                separator.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_TABULATOR },
                0, new FilePosition(1, 1), new String[] {});

        element = recognize.get(1);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(
                separator.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_TABULATOR },
                0, new FilePosition(1, text.length() + 1),
                new String[] {});
    }


    @Test
    public void test_tripplet_doubleSpace_oneShouldBeSetAsSeparatorTheRestAsPrettyAlign()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "  ";
        String text2 = "    ";
        TokenOutput tokenOutput = createTokenOutput(text + text2);

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
        OneLineSingleRobotContextPart separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertTokensForUnknownWords(separator.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, 1), new String[] {});

        element = recognize.get(1);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(separator.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE,
                        RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, text.length() + 1), new String[] {});
    }


    @Test
    public void test_twice_doubleSpace_oneShouldBeSetAsSeparatorTheSecondAsPrettyAlign()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "  ";
        String text2 = "  ";
        TokenOutput tokenOutput = createTokenOutput(text + text2);

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
        OneLineSingleRobotContextPart separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertTokensForUnknownWords(separator.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, 1), new String[] {});

        element = recognize.get(1);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(separator.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, text.length() + 1), new String[] {});
    }


    @Test
    public void test_prefix_doubleSpaces_space_tabulator_suffix_shouldReturn_oneSeparator_and_onePrettyAlign()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "Library";
        String text = "  ";
        String text2 = " \tOperating System";
        String p = prefix + text;
        TokenOutput tokenOutput = createTokenOutput(p + text2);

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
        OneLineSingleRobotContextPart separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertTokensForUnknownWords(separator.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, prefix.length() + 1),
                new String[] {});

        element = recognize.get(1);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.PRETTY_ALIGN);
        assertTokensForUnknownWords(separator.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotSingleCharTokenType.SINGLE_TABULATOR }, 0,
                new FilePosition(1, p.length() + 1), new String[] {});
    }


    @Test
    public void test_prefix_double_spaces_followingBy_tabulator_shouldReturn_oneSeparator_and_onePrettyAlign()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "Library";
        String text = "  Operating System";
        String text2 = "\tWITH NAME";
        String p = prefix + text;
        TokenOutput tokenOutput = createTokenOutput(p + text2);

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
        OneLineSingleRobotContextPart separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertTokensForUnknownWords(separator.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, prefix.length() + 1),
                new String[] {});

        element = recognize.get(1);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertTokensForUnknownWords(
                separator.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_TABULATOR },
                0, new FilePosition(1, p.length() + 1), new String[] {});
    }


    @Test
    public void test_prefix_one_tabulator_followingBy_double_spaces_shouldReturn_oneSeparator_and_onePrettyAlign()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "Library";
        String text = "\tOperating System";
        String text2 = "  WITH NAME";
        String p = prefix + text;
        TokenOutput tokenOutput = createTokenOutput(p + text2);

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
        OneLineSingleRobotContextPart separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertTokensForUnknownWords(
                separator.getContextTokens(),
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_TABULATOR },
                0, new FilePosition(1, prefix.length() + 1),
                new String[] {});

        element = recognize.get(1);
        assertThat(element).isInstanceOf(OneLineSingleRobotContextPart.class);
        separator = (OneLineSingleRobotContextPart) element;
        assertThat(separator.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertTokensForUnknownWords(separator.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE }, 0,
                new FilePosition(1, p.length() + 1), new String[] {});
    }


    @Test
    public void test_one_tabulator_addTheEndOfText_shouldReturn_oneSeparator()
            throws FileNotFoundException, IOException {
        String prefix = "Library";
        String text = "\t";
        assertThatIsExepectedContext(
                prefix,
                text,
                null,
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_TABULATOR });
    }


    @Test
    public void test_one_tabulator_addTheBeginningOfText_shouldReturn_oneSeparator()
            throws FileNotFoundException, IOException {
        String text = "\ttext";
        assertThatIsExepectedContext(
                null,
                text,
                null,
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_TABULATOR });
    }


    @Test
    public void test_one_tabulator_insideText_shouldReturn_oneSeparator()
            throws FileNotFoundException, IOException {
        String prefix = "Library";
        String text = "\ttext";
        assertThatIsExepectedContext(
                prefix,
                text,
                null,
                new IRobotTokenType[] { RobotSingleCharTokenType.SINGLE_TABULATOR });
    }


    @Test
    public void test_double_spaces_addTheEndOfText_shouldReturn_oneSeparator()
            throws FileNotFoundException, IOException {
        String prefix = "Library";
        String text = "  ";
        assertThatIsExepectedContext(prefix, text, null,
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE });
    }


    @Test
    public void test_double_spaces_addTheBeginningOfText_shouldReturn_oneSeparator()
            throws FileNotFoundException, IOException {
        String text = "  text";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE });
    }


    @Test
    public void test_double_spaces_insideText_shouldReturn_oneSeparator()
            throws FileNotFoundException, IOException {
        String prefix = "Library";
        String text = "  text";
        assertThatIsExepectedContext(prefix, text, null,
                new IRobotTokenType[] { RobotWordType.DOUBLE_SPACE });
    }


    @Test
    public void test_oneSpaceInsideText_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "temp foobar";
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
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
    }
}
