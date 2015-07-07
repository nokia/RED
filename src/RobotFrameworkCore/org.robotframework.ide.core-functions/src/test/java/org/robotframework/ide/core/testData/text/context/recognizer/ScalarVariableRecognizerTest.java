package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokensForUnknownWords;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
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
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ScalarVariableRecognizer
 */
public class ScalarVariableRecognizerTest extends ARecognizerTest {

    public ScalarVariableRecognizerTest() {
        super(ScalarVariableRecognizer.class);
    }


    @Test
    public void test_scalarInScalar() throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        String text2 = "foobar2";
        TokenOutput tokenOutput = createTokenOutput("${" + text + "${" + text2
                + "}}");

        List<RobotToken> toks = Collections.unmodifiableList(tokenOutput
                .getTokens());

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertTheSameLinesContext(recognize,
                OneLineSingleRobotContextPart.class, 2);
        assertThat(recognize.get(0).getType()).isEqualTo(
                context.getContextType());

        OneLineSingleRobotContextPart contextOne = (OneLineSingleRobotContextPart) recognize
                .get(0);
        assertThat(contextOne.getType()).isEqualTo(
                SimpleRobotContextType.SCALAR_VARIABLE);
        assertThatGetTokens(toks, contextOne, Arrays.asList(0, 1, 2, 7));

        OneLineSingleRobotContextPart contextTwo = (OneLineSingleRobotContextPart) recognize
                .get(1);
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.SCALAR_VARIABLE);
        assertThatGetTokens(toks, contextTwo, Arrays.asList(3, 4, 5, 6));

    }


    private void assertThatGetTokens(List<RobotToken> tokens,
            OneLineSingleRobotContextPart context, List<Integer> position) {
        List<RobotToken> toCheck = new LinkedList<>();
        for (Integer i : position) {
            toCheck.add(tokens.get(i));
        }

        assertThat(context.getContextTokens()).containsExactlyElementsOf(
                toCheck);
    }


    @Test
    public void test_scalaraVariable_withoutAnyInsideTokens_withEscape()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("${" + text + "\\${}");

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
                        RobotSingleCharTokenType.SINGLE_SCALAR_BEGIN_DOLLAR,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH,
                        RobotSingleCharTokenType.SINGLE_SCALAR_BEGIN_DOLLAR,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET },
                0, new FilePosition(1, 1), new String[] { text });
    }


    @Test
    public void test_scalaraVariableAfterScalarVariable_withoutAnyInsideTokens()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("$${" + text + "}");

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
                        RobotSingleCharTokenType.SINGLE_SCALAR_BEGIN_DOLLAR,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET },
                0, new FilePosition(1, 2), new String[] { text });
    }


    @Test
    public void test_scalaraVariable_withoutAnyInsideTokens()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("${" + text + "}");

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
                        RobotSingleCharTokenType.SINGLE_SCALAR_BEGIN_DOLLAR,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET },
                0, new FilePosition(1, 1), new String[] { text });
    }


    @Test
    public void test_onlyEscapedDolarAndCurrlyBracketAndTextSign()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("\\${foobar}");
    }


    @Test
    public void test_onlyDolarAndCurrlyBracketAndTextSign()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("${foobar");
    }


    @Test
    public void test_onlyDolarAndCurrlyBracketSign()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("${");
    }


    @Test
    public void test_onlyDolarSign() throws FileNotFoundException, IOException {
        assertForIncorrectData("$");
    }


    @Test
    public void test_noDolarSign() throws FileNotFoundException, IOException {
        assertForIncorrectData("foobar");
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.SCALAR_VARIABLE);
    }
}
