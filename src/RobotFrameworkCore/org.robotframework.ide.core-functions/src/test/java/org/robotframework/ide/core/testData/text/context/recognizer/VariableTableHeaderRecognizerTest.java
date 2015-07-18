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
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
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
 * @see VariableTableHeaderRecognizer
 */
public class VariableTableHeaderRecognizerTest extends ARecognizerTest {

    public VariableTableHeaderRecognizerTest() {
        super(VariableTableHeaderRecognizer.class);
    }


    @Test
    public void test_trashDataWithAsterisk_thenJustCorrectContextInWrongPlace_oneContext()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "** Context ";
        String text = "*** Variables ***";
        TokenOutput tokenOutput = createTokenOutput(prefix + text);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertTheSameLinesContext(recognize,
                OneLineSingleRobotContextPart.class, 1);

        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(0))
                        .getContextTokens(),
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.VARIABLES_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        MultipleCharTokenType.MANY_ASTERISKS }, 0,
                new FilePosition(1, prefix.length() + 1), new String[] {});
    }


    @Test
    public void test_pipe_beginAsteriskVariableWordAsteriskVariablesWord_pipe_shouldReturn_twoWrongContexts()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "|";
        String text = "*Variable";
        String text2 = "*Variables";
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
        assertTheSameLinesContext(recognize,
                OneLineSingleRobotContextPart.class, 2);

        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(0))
                        .getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.VARIABLE_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK }, 0,
                new FilePosition(1, prefix.length() + 1), new String[] {});
        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(1))
                        .getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.VARIABLES_WORD }, 0, new FilePosition(1,
                        p.length() + 1), new String[] {});
    }


    @Test
    public void test_pipe_beginAsteriskVariableWordAsterisk_pipe_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Variable*";
        String suffix = "|";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.VARIABLE_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_pipe_beginAsteriskVariablesWordAsterisk_pipe_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Variables*";
        String suffix = "|";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.VARIABLES_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_foobar_beginAsteriskVariablsWordAsterisk_spaceVariables_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Variables";
        String suffix = " Variables";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.VARIABLES_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_foobar_beginAsteriskVariableWordAsterisk_spaceVariable_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Variable";
        String suffix = " Variable";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.VARIABLE_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_foobar_beginAsteriskVariableWordAsterisk_barfoo_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Variable*";
        String suffix = "barfoo";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.VARIABLE_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_foobar_beginAsteriskVariablesWordAsterisk_barfoo_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Variables*";
        String suffix = "barfoo";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.VARIABLES_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskVariablesWordAsterisk_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Variables*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.VARIABLES_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskVariablesWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Variables";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.VARIABLES_WORD });
    }


    @Test
    public void test_beginAsteriskSpaceVariablesWordSpaceAsterisk_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Variables *";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.VARIABLES_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskSpaceVariablesWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Variables";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.VARIABLES_WORD });
    }


    @Test
    public void test_beginAsterisksVariablesWordAsterisks_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Variables***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.VARIABLES_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksVariablesWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Variables";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.VARIABLES_WORD });
    }


    @Test
    public void test_beginAsterisksSpaceVariablesWordSpaceAsterisks_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Variables ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.VARIABLES_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksSpaceVariablesWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Variables";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.VARIABLES_WORD });
    }


    @Test
    public void test_beginAsteriskVariableWordAsterisk_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Variable*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.VARIABLE_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskVariableWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Variable";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.VARIABLE_WORD });
    }


    @Test
    public void test_beginAsteriskSpaceVariableWordSpaceAsterisk_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Variable *";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.VARIABLE_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskSpaceVariableWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Variable";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.VARIABLE_WORD });
    }


    @Test
    public void test_beginAsterisksVariableWordAsterisks_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Variable***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.VARIABLE_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksVariableWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Variable";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.VARIABLE_WORD });
    }


    @Test
    public void test_beginAsterisksSpaceVariableWordSpaceAsterisks_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Variable ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.VARIABLE_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksSpaceVariableWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Variable";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.VARIABLE_WORD });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesVariableWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Variable ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.VARIABLE_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });

    }


    @Test
    public void test_beginAsterisksDoubleSpacesVariableWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  variable ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.VARIABLE_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
        ;
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesVariablesWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Variables **";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.VARIABLES_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesVariablesWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  variables ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.VARIABLES_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesVariablesWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "***  Variables";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.VARIABLES_WORD });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesVariablesWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  variables";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.VARIABLES_WORD });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesVariableWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Variable";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.VARIABLE_WORD });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesVariableWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  variable";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.VARIABLE_WORD });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***  ";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksDoubleSpacesAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  ";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksSpaceAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar *** ";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksSpaceAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "*** ";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_noAsterisksNorTableName_shouldReturn_anEmptyList()
            throws IOException {
        String text = "foobar foobar";
        assertForIncorrectData(text);
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.VARIABLE_TABLE_HEADER);
    }
}
