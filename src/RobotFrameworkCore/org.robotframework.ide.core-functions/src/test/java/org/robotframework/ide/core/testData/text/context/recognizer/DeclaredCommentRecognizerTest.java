package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokensForUnknownWords;

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
 * @see DeclaredCommentRecognizer
 */
public class DeclaredCommentRecognizerTest extends ARecognizerTest {

    public DeclaredCommentRecognizerTest() {
        super(DeclaredCommentRecognizer.class);
    }


    @Test
    public void test_correctWordComment_shouldReturn_oneContext()
            throws IOException {
        // prepare
        String commentText = "escaped";
        String text = "\tComment " + commentText;
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
                SimpleRobotContextType.DECLARED_COMMENT);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.COMMENT_FROM_BUILTIN,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.UNKNOWN_WORD }, 0,
                new FilePosition(1, 2), new String[] { commentText });
    }


    @Test
    public void test_correctHashComment_shouldReturn_oneContext()
            throws IOException {
        // prepare
        String commentText = "escaped";
        String text = "#" + commentText;
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
                SimpleRobotContextType.DECLARED_COMMENT);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_COMMENT_HASH,
                        RobotWordType.UNKNOWN_WORD }, 0,
                new FilePosition(1,
                        FilePosition.THE_FIRST_COLUMN),
                new String[] { commentText });
    }


    @Test
    public void test_escapeWordAndSpaceAndThenFourHashs_shouldReturn_oneContext()
            throws IOException {
        // prepare
        String trashText = "foobar\\ ";
        String commentText = "escaped";
        String text = trashText + "Comment ####" + commentText;
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
                SimpleRobotContextType.DECLARED_COMMENT);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.COMMENT_FROM_BUILTIN,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        MultipleCharTokenType.MANY_COMMENT_HASHS,
                        RobotWordType.UNKNOWN_WORD }, 4,
                new FilePosition(1,
                        FilePosition.THE_FIRST_COLUMN),
                new String[] { commentText });
    }


    @Test
    public void test_escapeWordAndSpaceAndThenHash_shouldReturn_oneContext()
            throws IOException {
        // prepare
        String trashText = "foobar\\ ";
        String commentText = "escaped";
        String text = trashText + "Comment #" + commentText;
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
                SimpleRobotContextType.DECLARED_COMMENT);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.COMMENT_FROM_BUILTIN,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotSingleCharTokenType.SINGLE_COMMENT_HASH,
                        RobotWordType.UNKNOWN_WORD }, 4,
                new FilePosition(1,
                        FilePosition.THE_FIRST_COLUMN),
                new String[] { commentText });
    }


    @Test
    public void test_escapeCharAndSpaceAndThenCommentWord_shouldReturn_oneContext()
            throws IOException {
        // prepare
        String trashText = "foobar\\ ";
        String commentText = "escaped";
        String text = trashText + "Comment " + commentText;
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
                SimpleRobotContextType.DECLARED_COMMENT);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new IRobotTokenType[] { RobotWordType.COMMENT_FROM_BUILTIN,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.UNKNOWN_WORD }, 9,
                new FilePosition(1,
                        FilePosition.THE_FIRST_COLUMN),
                new String[] { commentText });
    }


    @Test
    public void test_escapeCharAndSpaceAndThenFourHashs_shouldReturn_oneContext()
            throws IOException {
        // prepare
        String trashText = "foobar\\ ";
        String commentText = "escaped";
        String text = trashText + "#### " + commentText;
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
                SimpleRobotContextType.DECLARED_COMMENT);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new IRobotTokenType[] {
                        MultipleCharTokenType.MANY_COMMENT_HASHS,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.UNKNOWN_WORD }, 4,
                new FilePosition(1,
                        FilePosition.THE_FIRST_COLUMN),
                new String[] { commentText });
    }


    @Test
    public void test_escapeCharAndSpaceAndThenHash_shouldReturn_oneContext()
            throws IOException {
        // prepare
        String trashText = "foobar\\ ";
        String commentText = "escaped";
        String text = trashText + "# " + commentText;
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
                SimpleRobotContextType.DECLARED_COMMENT);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_COMMENT_HASH,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.UNKNOWN_WORD }, 9,
                new FilePosition(1,
                        FilePosition.THE_FIRST_COLUMN),
                new String[] { commentText });
    }


    @Test
    public void test_escapeCharBeforeHash_shouldReturn_anEmptyList()
            throws IOException {
        // prepare
        String text = "foobar\\# escaped";
        TokenOutput tokenOutput = createTokenOutput(text);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertThat(recognize).isEmpty();
    }


    @Test
    public void test_noHashAndCommentWord_shouldReturn_anEmptyList()
            throws IOException {
        // prepare
        String text = "foobar foobar";
        TokenOutput tokenOutput = createTokenOutput(text);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertThat(recognize).isEmpty();
    }


    @Test
    public void test_getContextType() {
        assertThat(context.getContextType()).isEqualTo(
                SimpleRobotContextType.DECLARED_COMMENT);
    }
}
