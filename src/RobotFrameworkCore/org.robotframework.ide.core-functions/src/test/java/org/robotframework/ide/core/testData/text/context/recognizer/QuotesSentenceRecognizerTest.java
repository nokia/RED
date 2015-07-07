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
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see QuotesSentenceRecognizer
 */
public class QuotesSentenceRecognizerTest extends ARecognizerTest {

    public QuotesSentenceRecognizerTest() {
        super(QuotesSentenceRecognizer.class);
    }


    @Test
    public void test_QuotesMarkThenDoubleBackslashAndAgainQuoteQuoteMarkMark_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {

        // prepare
        String prefix = "|";
        String text = "\"\\\\\"";
        String text2 = "\"";
        String suffix = "|";
        String p = prefix + text;
        TokenOutput tokenOutput = createTokenOutput(p + text2 + suffix);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertTheSameLinesContext(recognize, OneLineSingleRobotContextPart.class, 2);

        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(0)).getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_QUOTE_MARK,
                        RobotWordType.DOUBLE_ESCAPE_BACKSLASH,
                        RobotSingleCharTokenType.SINGLE_QUOTE_MARK }, 0,
                new FilePosition(1, prefix.length() + 1),
                new String[] {});
        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(1)).getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_QUOTE_MARK,
                        RobotSingleCharTokenType.SINGLE_QUOTE_MARK }, 0,
                new FilePosition(1, p.length()), new String[] {});

    }


    @Test
    public void test_QuotesMarkThenDoubleBackslashAndAgainQuoteMark_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String text = "\"\\\\\"";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_QUOTE_MARK,
                RobotWordType.DOUBLE_ESCAPE_BACKSLASH,
                RobotSingleCharTokenType.SINGLE_QUOTE_MARK });
    }


    @Test
    public void test_QuotesMarkThenAsterisksAndAgainQuoteMark_shouldReturn_oneElement()
            throws FileNotFoundException, IOException {
        String text = "\"***\"";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_QUOTE_MARK,
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_QUOTE_MARK });
    }


    @Test
    public void test_QuotesMarkThenEscapeQuoteMark_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar \"\\\"";
        assertForIncorrectData(text);
    }


    @Test
    public void test_noQuotesMarks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar foobar";
        assertForIncorrectData(text);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.QUOTES_SENTENCE);
    }
}
