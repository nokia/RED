package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

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
import org.robotframework.ide.core.testData.text.context.recognizer.ARecognizerSequenceWithOptionalLastColonTest;
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
 * @see TestPreconditionDeclaration
 */
public class TestPreconditionDeclarationTest extends
        ARecognizerSequenceWithOptionalLastColonTest {

    public TestPreconditionDeclarationTest() {
        super(TestPreconditionDeclaration.class);
    }


    @Test
    public void test_recognize_word_is_correct_withOptionalColon_shouldReturnOneContext()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "Test Precondition:";
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
                SettingTableRobotContextType.TABLE_SETTINGS_TEST_PRECONDITION);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.PRECONDITION_WORD,
                        RobotSingleCharTokenType.SINGLE_COLON }, 0,
                new FilePosition(1, 1), new String[] {});
    }


    @Test
    public void test_recognize_word_is_correct_optionalColonIsNotPresent_shouldReturnOneContext()
            throws FileNotFoundException, IOException {
        // prepare
        String text = "Test Precondition";
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
                SettingTableRobotContextType.TABLE_SETTINGS_TEST_PRECONDITION);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.PRECONDITION_WORD }, 0, new FilePosition(
                        1, 1), new String[] {});
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
        assertExpectedSequenceAllMandatory(RobotWordType.TEST_WORD,
                RobotWordType.PRECONDITION_WORD);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SettingTableRobotContextType.TABLE_SETTINGS_TEST_PRECONDITION);
    }
}
