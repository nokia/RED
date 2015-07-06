package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see EmptyLineRecognizer
 */
public class EmptyLineRecognizerTest extends ARecognizerTest {

    public EmptyLineRecognizerTest() {
        super(EmptyLineRecognizer.class);
    }


    @Test
    public void test_recognize_onlyCRLFcase() throws FileNotFoundException,
            IOException {
        String text = "\r\n";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.CARRIAGE_RETURN,
                RobotSingleCharTokenType.LINE_FEED });
    }


    @Test
    public void test_recognize_forEmptyLine() throws FileNotFoundException,
            IOException {
        TokenOutput tokenOutput = createTokenOutput("");

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        if (line == null) {
            line = new LineTokenPosition(0, 0,
                    LinearPositionMarker.THE_FIRST_LINE);
        }
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        OneLineSingleRobotContextPart header = assertAndGetOneLineContext(recognize);
        assertThat(header.getContextTokens()).isEmpty();
    }


    @Test
    public void test_recognize_forManySpacesTabulatorsWithoutAnyWords_plusCRLFcase()
            throws FileNotFoundException, IOException {
        String text = "\t\t  \t \t \r\n";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotWordType.DOUBLE_SPACE,
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.CARRIAGE_RETURN,
                RobotSingleCharTokenType.LINE_FEED });
    }


    @Test
    public void test_recognize_forManySpacesTabulatorsWithoutAnyWords()
            throws FileNotFoundException, IOException {
        String text = "\t\t  \t \t ";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotWordType.DOUBLE_SPACE,
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_TABULATOR,
                RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_recognize_forLineWithManySpacesAndThenLastWord_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("\t\t\t\t\t\t\t\t\t\t  \t\t\tWORD");
    }


    @Test
    public void test_recognize_forLineWithWords_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        assertForIncorrectData(" fooooooobaaaaaarrrrrrrrr fooooooobar");
    }


    @Test
    public void test_createContext() {
        LineTokenPosition pos = mock(LineTokenPosition.class);
        when(pos.getLineNumber()).thenReturn(1);

        OneLineSingleRobotContextPart l = ((EmptyLineRecognizer) context)
                .createContext(pos);
        assertThat(l).isNotNull();
        assertThat(l.getLineNumber()).isEqualTo(1);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.EMPTY_LINE);
    }
}
