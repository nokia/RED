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
 * @see DictionaryVariableRecognizer
 */
public class DictionaryVariableRecognizerTest extends ARecognizerTest {

    public DictionaryVariableRecognizerTest() {
        super(DictionaryVariableRecognizer.class);
    }


    @Test
    public void test_dictionaryInDictionary() throws FileNotFoundException,
            IOException {
        // prepare
        String text = "foobar";
        String text2 = "foobar2";
        TokenOutput tokenOutput = createTokenOutput("&{" + text + "&{" + text2
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
                SimpleRobotContextType.DICTIONARY_VARIABLE);
        assertThatGetTokens(toks, contextOne, Arrays.asList(0, 1, 2, 7));

        OneLineSingleRobotContextPart contextTwo = (OneLineSingleRobotContextPart) recognize
                .get(1);
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.DICTIONARY_VARIABLE);
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
    public void test_dictionaryVariable_withoutAnyInsideTokens_withEscape()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("&{" + text + "\\&{}");

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
                        RobotSingleCharTokenType.SINGLE_DICTIONARY_BEGIN_AMPERSAND,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH,
                        RobotSingleCharTokenType.SINGLE_DICTIONARY_BEGIN_AMPERSAND,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET },
                0, new FilePosition(1, 1), new String[] { text });
    }


    @Test
    public void test_dictionaryVariableAfterDictionaryVariable_withoutAnyInsideTokens()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("&&{" + text + "}");

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
                        RobotSingleCharTokenType.SINGLE_DICTIONARY_BEGIN_AMPERSAND,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET },
                0, new FilePosition(1, 2), new String[] { text });
    }


    @Test
    public void test_dictionaryVariable_withoutAnyInsideTokens()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("&{" + text + "}");

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
                        RobotSingleCharTokenType.SINGLE_DICTIONARY_BEGIN_AMPERSAND,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET },
                0, new FilePosition(1, 1), new String[] { text });
    }


    @Test
    public void test_onlyEscapedAmpersandAndCurrlyBracketAndTextSign()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("\\&{foobar}");
    }


    @Test
    public void test_onlyAmpersandAndCurrlyBracketAndTextSign()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("&{foobar");
    }


    @Test
    public void test_onlyAmpersandAndCurrlyBracketSign()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("&{");
    }


    @Test
    public void test_onlyAmpersandSign() throws FileNotFoundException,
            IOException {
        assertForIncorrectData("&");
    }


    @Test
    public void test_noAmpersandSign() throws FileNotFoundException,
            IOException {
        assertForIncorrectData("foobar");
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.DICTIONARY_VARIABLE);
    }
}
