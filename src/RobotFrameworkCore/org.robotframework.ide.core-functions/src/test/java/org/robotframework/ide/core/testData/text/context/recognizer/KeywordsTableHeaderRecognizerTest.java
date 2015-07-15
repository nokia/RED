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
 * @see KeywordsTableHeaderRecognizer
 */
public class KeywordsTableHeaderRecognizerTest extends ARecognizerTest {

    public KeywordsTableHeaderRecognizerTest() {
        super(KeywordsTableHeaderRecognizer.class);
    }


    @Test
    public void test_pipe_beginAsteriskKeywordsWordAsteriskUser_KeywordsWord_pipe_shouldReturn_twoWrongContexts()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "|";
        String text = "*Keywords";
        String text2 = "*User Keywords";
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
                        RobotWordType.KEYWORDS_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK }, 0,
                new FilePosition(1, prefix.length() + 1),
                new String[] {});
        assertTokensForUnknownWords(
                ((OneLineSingleRobotContextPart) recognize.get(1)).getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.USER_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.KEYWORDS_WORD }, 0,
                new FilePosition(1, p.length() + 1), new String[] {});
    }


    @Test
    public void test_prefix_asterisk_user_keywords_asterisk_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "*User Keywords*";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.USER_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_prefix_asterisk_user_keyword_asterisk_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "*User Keyword*";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.USER_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_prefix_asterisk_keywords_asterisk_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "*Keywords*";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.KEYWORDS_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_prefix_asterisk_keyword_asterisk_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "*Keyword*";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.KEYWORD_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK, });
    }


    @Test
    public void test_asterisk_user_keywords_asterisk_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "*User Keywords*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.USER_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK, });
    }


    @Test
    public void test_asterisk_user_keyword_asterisk_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "*User Keyword*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.USER_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_asterisk_keywords_asterisk_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "*Keywords*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.KEYWORDS_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_asterisk_keyword_asterisk_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "*Keyword*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.KEYWORD_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_trashBeginAsterisk_user_keywords_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "*User Keywords";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.USER_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_trashBeginAsterisk_and_user_keyword_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "*User Keyword";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.USER_WORD, RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_trashBeginAsterisk_and_keywords_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "*Keywords";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_trashBeginAsterisk_and_keyword_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "*Keyword";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_prefix_asterisks_user_keywords_asterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***User Keywords**";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_prefix_asterisks_user_keyword_asterisks_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***User Keyword***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_prefix_asterisks_keywords_asterisks_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***Keywords***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORDS_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_prefix_asterisks_keyword_asterisks_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***Keyword***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORD_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_asterisks_user_keywords_asterisks_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "***User Keywords***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_asterisks_user_keyword_asterisks_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "***User Keyword***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_asterisks_keywords_asterisks_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "***Keywords***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORDS_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_asterisks_keyword_asterisks_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "***Keyword***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORD_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_trashBeginAsterisks_user_keywords_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***User Keywords";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_trashBeginAsterisks_and_user_keyword_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***User Keyword";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_trashBeginAsterisks_and_keywords_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***Keywords";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_trashBeginAsterisks_and_keyword_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***Keyword";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_beginAsterisks_user_space_keywords_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "***User keywords";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_beginAsterisks_user_space_keyword_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "***User keyword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_beginAsterisks_user_2Space_keywords_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***User  keywords";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_beginAsterisks_user_2space_keyword_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***User  keyword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS, RobotWordType.USER_WORD,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_beginAsterisks_keywords_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "***keywords";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_beginAsterisks_keyword_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "***keyword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_beginAsterisks_keywords_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***keywords";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_beginAsterisks_keyword_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***keyword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_prefix_asterisks_2Space_user_keywords_2Space_asterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  User Keywords  ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD, RobotWordType.DOUBLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_prefix_asterisks_2Space_user_keyword_2Space_asterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  User Keyword  ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD, RobotWordType.DOUBLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_prefix_asterisks_2Space_keywords_2Space_asterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Keywords  ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORDS_WORD,
                RobotWordType.DOUBLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_prefix_asterisks_2Space_keyword_2Space_asterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Keyword  ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORD_WORD,
                RobotWordType.DOUBLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_asterisks_2Space_user_keywords_2Space_asterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  User Keywords  ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD, RobotWordType.DOUBLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_asterisks_2Space_user_keyword_2Space_asterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  User Keyword  ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD, RobotWordType.DOUBLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_asterisks_2Space_keywords_2Space_asterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  Keywords  ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORDS_WORD,
                RobotWordType.DOUBLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_asterisks_2Space_keyword_2Space_asterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  Keyword  ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORD_WORD,
                RobotWordType.DOUBLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_trashBeginAsterisks_and_doubleSpace_user_keywords_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  User Keywords";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_trashBeginAsterisks_and_doubleSpace_user_keyword_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  User Keyword";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_trashBeginAsterisks_and_doubleSpace_keywords_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Keywords";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_trashBeginAsterisks_and_doubleSpace_keyword_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Keyword";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_beginAsterisks_space_user_space_keywords_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "*** User keywords";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_beginAsterisks_space_user_space_keyword_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "*** User keyword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_beginAsterisks_doubleSpace_user_space_keywords_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  User keywords";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_beginAsterisks_doubleSpace_user_space_keyword_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  User keyword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_beginAsterisks_doubleSpace_user_2Space_keywords_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  User  keywords";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_beginAsterisks_doubleSpace_user_2space_keyword_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  User  keyword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.USER_WORD,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_beginAsterisks_space_keywords_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "*** keywords";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_beginAsterisks_space_keyword_word_shouldReturn_correctContext()
            throws FileNotFoundException, IOException {
        String text = "*** keyword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_beginAsterisks_doubleSpace_keywords_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  keywords";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORDS_WORD });
    }


    @Test
    public void test_beginAsterisks_doubleSpace_keyword_word_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  keyword";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.KEYWORD_WORD });
    }


    @Test
    public void test_asterisk_twiceUserAndThenKeywordWordsOnly_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  user user keyword";
        assertForIncorrectData(text);
    }


    @Test
    public void test_asterisk_userKeywordWordsOnly_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "  user keyword";
        assertForIncorrectData(text);
    }


    @Test
    public void test_asterisks_user_asterisk_userWordOnly_suffix_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  user ***  user keywor";
        assertForIncorrectData(text);
    }


    @Test
    public void test_asterisk_userWordOnly_suffix_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  user keywor";
        assertForIncorrectData(text);
    }


    @Test
    public void test_asterisk_userWordOnly_asterisk_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  user ***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_asterisk_userWordOnly_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  user";
        assertForIncorrectData(text);
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
                SimpleRobotContextType.KEYWORD_TABLE_HEADER);
    }
}
