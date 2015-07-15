package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokensForUnknownWords;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.ARecognizerSequenceInBracketsTest;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see TestTimeoutDeclaration
 */
public class TestTimeoutDeclarationTest extends
        ARecognizerSequenceInBracketsTest {

    public TestTimeoutDeclarationTest() {
        super(TestTimeoutDeclaration.class);
    }


    @Test
    public void test_recognize_word_is_correct_shouldReturnOneContext()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "[Timeout]";
        TokenOutput tokenOutput = createTokenOutput(text);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        OneLineSingleRobotContextPart comment = assertAndGetOneLineContext(recognize);
        assertThat(comment.getType()).isEqualTo(
                TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TIMEOUT);

        assertTokensForUnknownWords(
                comment.getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET,
                        RobotWordType.TIMEOUT_WORD,
                        RobotSingleCharTokenType.SINGLE_POSITION_INDEX_END_SQUARE_BRACKET },
                0, new FilePosition(1, 1), new String[] {});
    }


    @Test
    public void test_recognize_word_foobarIsWrappedInSquareBrackets_shouldReturn_emptyContextList()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("[foobar]");
    }


    @Test
    public void test_recognize_wordIs_foobar_shouldReturn_emptyContextList()
            throws FileNotFoundException, IOException {
        assertForIncorrectData("foobar");
    }


    @Test
    public void test_getExpectedElements() {
        assertExpectedSequenceAllMandatory(RobotWordType.TIMEOUT_WORD);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TIMEOUT);
    }
}
