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
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.ARecognizerTest;
import org.robotframework.ide.core.testData.text.context.recognizer.variables.CollectionIndexPosition;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;

import com.google.common.base.Joiner;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see CollectionIndexPosition
 */
public class CollectionIndexPositionTest extends ARecognizerTest {

    public CollectionIndexPositionTest() {
        super(CollectionIndexPosition.class);
    }


    @Test
    public void test_threePositionInsideOtherPosition()
            throws FileNotFoundException, IOException {
        // prepare
        List<String> tokens = Arrays.asList("[", "[", "]", "[", "]", "]");
        TokenOutput tokenOutput = createTokenOutput(Joiner.on("").join(tokens));

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
                OneLineSingleRobotContextPart.class, 3);

        OneLineSingleRobotContextPart mainContext = (OneLineSingleRobotContextPart) recognize
                .get(0);
        assertThat(mainContext.getType()).isEqualTo(
                SimpleRobotContextType.COLLECTION_TYPE_VARIABLE_POSITION);
        assertThatGetTokens(toks, mainContext, Arrays.asList(0, 5));

        OneLineSingleRobotContextPart theFirstSubPosition = (OneLineSingleRobotContextPart) recognize
                .get(1);
        assertThat(theFirstSubPosition.getType()).isEqualTo(
                SimpleRobotContextType.COLLECTION_TYPE_VARIABLE_POSITION);
        assertThatGetTokens(toks, theFirstSubPosition, Arrays.asList(1, 2));

        OneLineSingleRobotContextPart theSecondSubPosition = (OneLineSingleRobotContextPart) recognize
                .get(2);
        assertThat(theSecondSubPosition.getType()).isEqualTo(
                SimpleRobotContextType.COLLECTION_TYPE_VARIABLE_POSITION);
        assertThatGetTokens(toks, theSecondSubPosition, Arrays.asList(3, 4));
    }


    @Test
    public void test_onePositionInsideOtherPosition()
            throws FileNotFoundException, IOException {
        // prepare
        List<String> tokens = Arrays.asList("$", "{", "sclara", "[", "$", "{",
                "paramDict", "[", "key", "]", "}", "]", "}");
        TokenOutput tokenOutput = createTokenOutput(Joiner.on("").join(tokens));

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
                SimpleRobotContextType.COLLECTION_TYPE_VARIABLE_POSITION);
        assertThatGetTokens(toks, contextOne, Arrays.asList(3, 4, 5, 6, 10, 11));

        OneLineSingleRobotContextPart contextTwo = (OneLineSingleRobotContextPart) recognize
                .get(1);
        assertThat(contextTwo.getType()).isEqualTo(
                SimpleRobotContextType.COLLECTION_TYPE_VARIABLE_POSITION);
        assertThatGetTokens(toks, contextTwo, Arrays.asList(7, 8, 9));

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
    public void test_positionAfterListEnv_withoutAnyInsideTokens()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("[[" + text + "]");

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
                        RobotSingleCharTokenType.SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_POSITION_INDEX_END_SQUARE_BRACKET },
                0, new FilePosition(1, 2), new String[] { text });
    }


    @Test
    public void test_position_withoutAnyInsideTokens()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "foobar";
        TokenOutput tokenOutput = createTokenOutput("${var[" + text + "]}");

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
                        RobotSingleCharTokenType.SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET,
                        RobotWordType.UNKNOWN_WORD,
                        RobotSingleCharTokenType.SINGLE_POSITION_INDEX_END_SQUARE_BRACKET },
                0, new FilePosition(1, 6), new String[] { text });
    }


    @Test
    public void test_onlyEndBracketSign() throws FileNotFoundException,
            IOException {
        assertForIncorrectData("]");
    }


    @Test
    public void test_onlyBeginBracketSign() throws FileNotFoundException,
            IOException {
        assertForIncorrectData("[");
    }


    @Test
    public void test_noSquareBracketsSign() throws FileNotFoundException,
            IOException {
        assertForIncorrectData("foobar");
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.COLLECTION_TYPE_VARIABLE_POSITION);
    }
}
