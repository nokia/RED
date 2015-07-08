package org.robotframework.ide.core.testData.text.context.recognizer.variables;

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
import org.robotframework.ide.core.testData.text.context.recognizer.ARecognizerTest;
import org.robotframework.ide.core.testData.text.context.recognizer.variables.EnvironmentVariableRecognizer;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
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
 * @see EnvironmentVariableRecognizer
 */
public class EnvironmentVariableRecognizerTest extends ARecognizerTest {

    public EnvironmentVariableRecognizerTest() {
        super(EnvironmentVariableRecognizer.class);
    }


    @Test
    public void test_envInEnv() throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        String text2 = "foobar2";
        TokenOutput tokenOutput = createTokenOutput("%{" + text + "%{" + text2
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
                SimpleRobotContextType.ENVIRONMENT_VARIABLE);
        assertThatGetTokens(toks, contextOne, Arrays.asList(0, 1, 2, 7));

        OneLineSingleRobotContextPart contextTwo = (OneLineSingleRobotContextPart) recognize
                .get(1);
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.ENVIRONMENT_VARIABLE);
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
    public void test_envVariable_withoutAnyInsideTokens_withEscape()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("%{" + text + "\\%{}");

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
                        RobotSingleCharTokenType.SINGLE_ENVIRONMENT_BEGIN_PROCENT,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH,
                        RobotSingleCharTokenType.SINGLE_ENVIRONMENT_BEGIN_PROCENT,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET },
                0, new FilePosition(1, 1), new String[] { text });
    }


    @Test
    public void test_envVariableAfterEnvSign_withoutAnyInsideTokens()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("%%{" + text + "}");

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
                        RobotSingleCharTokenType.SINGLE_ENVIRONMENT_BEGIN_PROCENT,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET },
                0, new FilePosition(1, 2), new String[] { text });
    }


    @Test
    public void test_envVariable_withoutAnyInsideTokens()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("%{" + text + "}");

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
                        RobotSingleCharTokenType.SINGLE_ENVIRONMENT_BEGIN_PROCENT,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET },
                0, new FilePosition(1, 1), new String[] { text });
    }


    @Test
    public void test_onlyEscapedProcentAndCurrlyBracketAndTextSign()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("\\%{foobar}");
    }


    @Test
    public void test_onlyProcentAndCurrlyBracketAndTextSign()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("%{foobar");
    }


    @Test
    public void test_onlyProcentAndCurrlyBracketSign()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("%{");
    }


    @Test
    public void test_onlyProcentSign() throws FileNotFoundException,
            IOException {
        assertForIncorrectData("%");
    }


    @Test
    public void test_noProcentSign() throws FileNotFoundException, IOException {
        assertForIncorrectData("foobar");
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.ENVIRONMENT_VARIABLE);
    }
}
