package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokensForUnknownWords;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineRobotContext;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.TxtRobotTestDataLexer;
import org.robotframework.ide.core.testData.text.lexer.helpers.ReadersProvider;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class DeclaredCommentContextTest {

    @ForClean
    @Mock
    private ReadersProvider readersProvider;
    @ForClean
    @Mock
    private Reader reader;
    @ForClean
    @Mock
    private File file;

    @ForClean
    private IContextRecognizer context;


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
        OneLineRobotContext comment = assertAndGetOneLineContext(recognize);

        assertTokensForUnknownWords(
                comment.getContextTokens(),
                new RobotType[] { RobotWordType.COMMENT_FROM_BUILTIN,
                        RobotTokenType.SINGLE_SPACE, RobotWordType.UNKNOWN_WORD },
                0, new LinearPositionMarker(1, 2), new String[] { commentText });
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
        OneLineRobotContext comment = assertAndGetOneLineContext(recognize);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new RobotType[] { RobotTokenType.SINGLE_COMMENT_HASH,
                        RobotWordType.UNKNOWN_WORD }, 0,
                new LinearPositionMarker(1,
                        LinearPositionMarker.THE_FIRST_COLUMN),
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
        OneLineRobotContext comment = assertAndGetOneLineContext(recognize);

        assertTokensForUnknownWords(comment.getContextTokens(),
                new RobotType[] { RobotWordType.COMMENT_FROM_BUILTIN,
                        RobotTokenType.SINGLE_SPACE,
                        RobotTokenType.SINGLE_COMMENT_HASH,
                        RobotWordType.UNKNOWN_WORD }, 4,
                new LinearPositionMarker(1,
                        LinearPositionMarker.THE_FIRST_COLUMN),
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
        OneLineRobotContext comment = assertAndGetOneLineContext(recognize);

        assertTokensForUnknownWords(
                comment.getContextTokens(),
                new RobotType[] { RobotWordType.COMMENT_FROM_BUILTIN,
                        RobotTokenType.SINGLE_SPACE, RobotWordType.UNKNOWN_WORD },
                9, new LinearPositionMarker(1,
                        LinearPositionMarker.THE_FIRST_COLUMN),
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
        OneLineRobotContext comment = assertAndGetOneLineContext(recognize);

        assertTokensForUnknownWords(
                comment.getContextTokens(),
                new RobotType[] { RobotTokenType.SINGLE_COMMENT_HASH,
                        RobotTokenType.SINGLE_SPACE, RobotWordType.UNKNOWN_WORD },
                9, new LinearPositionMarker(1,
                        LinearPositionMarker.THE_FIRST_COLUMN),
                new String[] { commentText });
    }


    private OneLineRobotContext assertAndGetOneLineContext(
            final List<IContextElement> recognize) {
        assertThat(recognize).hasSize(1);
        IContextElement iContextElement = recognize.get(0);
        assertThat(iContextElement).isInstanceOf(OneLineRobotContext.class);
        OneLineRobotContext comment = (OneLineRobotContext) iContextElement;
        assertThat(comment.getLineNumber()).isEqualTo(1);
        assertThat(comment.getParent()).isNull();
        assertThat(comment.getParentContext()).isNull();
        assertThat(comment.getType()).isEqualTo(
                SimpleRobotContextType.DECLARED_COMMENT);

        return comment;
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


    private TokenOutput createTokenOutput(String text)
            throws FileNotFoundException, IOException {
        when(readersProvider.create(file)).thenReturn(new StringReader(text));
        doCallRealMethod().when(readersProvider).newCharBuffer(anyInt());
        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer(readersProvider);
        TokenOutput tokenOutput = lexer.extractTokens(file);
        return tokenOutput;
    }


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        context = new DeclaredCommentContext();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
