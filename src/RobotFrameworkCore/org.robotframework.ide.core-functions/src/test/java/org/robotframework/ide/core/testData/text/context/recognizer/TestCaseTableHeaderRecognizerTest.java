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
 * @see TestCaseTableHeaderRecognizer
 */
public class TestCaseTableHeaderRecognizerTest extends ARecognizerTest {

    public TestCaseTableHeaderRecognizerTest() {
        super(TestCaseTableHeaderRecognizer.class);
    }


    @Test
    public void test_pipe_beginAsteriskTestCaseWordsAsteriskTestCasesWord_pipe_separatedByDoubleSpace_shouldReturn_twoWrongContexts()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "|";
        String text = "*Test  Case";
        String text2 = "*Test  Cases";
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
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD, RobotWordType.DOUBLE_SPACE,
                        RobotWordType.CASE_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK }, 0,
                new FilePosition(1, prefix.length() + 1),
                new String[] {});
        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(1)).getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD, RobotWordType.DOUBLE_SPACE,
                        RobotWordType.CASES_WORD }, 0,
                new FilePosition(1, p.length() + 1), new String[] {});
    }


    @Test
    public void test_pipe_beginAsteriskTestCaseWordsAsteriskTestCasesWord_pipe_shouldReturn_twoWrongContexts()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "|";
        String text = "*Test Case";
        String text2 = "*Test Cases";
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
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK }, 0,
                new FilePosition(1, prefix.length() + 1),
                new String[] {});
        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(1)).getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD }, 0,
                new FilePosition(1, p.length() + 1), new String[] {});
    }


    @Test
    public void test_pipe_beginAsteriskTestCaseWordAsterisk_pipe_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Test Case*";
        String suffix = "|";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_pipe_beginAsteriskTestCasesWordAsterisk_pipe_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Test Cases*";
        String suffix = "|";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_foobar_beginAsteriskTestCaseWordAsterisk_spaceTest_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Test Cases";
        String suffix = " Test";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_foobar_beginAsteriskTestCaseTestWordAsterisk_spaceTest_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Test Case";
        String suffix = " Test";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_foobar_beginAsteriskTestCaseCaseWordAsterisk_spaceCase_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Test Case";
        String suffix = " Case";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_foobar_beginAsteriskTestCaseWordAsterisk_barfoo_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Test Case*";
        String suffix = "barfoo";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK, });
    }


    @Test
    public void test_foobar_beginAsteriskTestCasesWordAsterisk_barfoo_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Test Cases*";
        String suffix = "barfoo";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskTestCasesWordAsterisk_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Test Cases*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.TEST_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.CASES_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskCasesWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Test Cases";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.TEST_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.CASES_WORD });
    }


    @Test
    public void test_beginAsteriskSpaceTestCasesWordSpaceAsterisk_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Test Cases *";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.TEST_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.CASES_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskSpaceTestCasesWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Test Cases";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD });
    }


    @Test
    public void test_beginAsterisksTestCasesWordAsterisks_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Test Cases***";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD,
                        MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksVariablesWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Test Cases";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD });
    }


    @Test
    public void test_beginAsterisksSpaceTestCasesWordSpaceAsterisks_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Test Cases ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.TEST_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.CASES_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksSpaceVariablesWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Test Cases";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD });
    }


    @Test
    public void test_beginAsteriskTestCaseWordAsterisk_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Test Case*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.TEST_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.CASE_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskTestCaseWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Test Case";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.TEST_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.CASE_WORD });
    }


    @Test
    public void test_beginAsteriskSpaceTestCaseWordSpaceAsterisk_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Test Case *";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.TEST_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.CASE_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskSpaceTestCaseWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Test Case";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD });
    }


    @Test
    public void test_beginAsterisksTestCaseWordAsterisks_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Test Case***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.TEST_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.CASE_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksTestCaseWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Test Case";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD });
    }


    @Test
    public void test_beginAsterisksSpaceTestCaseWordSpaceAsterisks_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Test Case ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.TEST_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.CASE_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,

                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksSpaceTestCaseWord_shouldReturn_oneContextOfVariableTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Test Case";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD, });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesTestCaseWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Test Case ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.TEST_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.CASE_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });

    }


    @Test
    public void test_beginAsterisksDoubleSpacesTestCaseWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  test case ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.TEST_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.CASE_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
        ;
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesTestCasesWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Test Cases **";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.TEST_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.CASES_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesTestCasesWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  test cases ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.TEST_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.CASES_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesTestSpaceCasesWordsAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Test Cases";
        assertThatIsExepectedContext(prefix, text, null,
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotWordType.DOUBLE_SPACE, RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesTestCasesWordsAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  test cases";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotWordType.DOUBLE_SPACE, RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASES_WORD });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesTestSpaceCaseWordsAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Test Case";
        assertThatIsExepectedContext(prefix, text, null,
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotWordType.DOUBLE_SPACE, RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesTestCaseWordsAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  test case";
        assertThatIsExepectedContext(null, text, null,
                new IRobotTokenType[] { MultipleCharTokenType.MANY_ASTERISKS,
                        RobotWordType.DOUBLE_SPACE, RobotWordType.TEST_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.CASE_WORD });
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
                SimpleRobotContextType.TEST_CASE_TABLE_HEADER);
    }
}
