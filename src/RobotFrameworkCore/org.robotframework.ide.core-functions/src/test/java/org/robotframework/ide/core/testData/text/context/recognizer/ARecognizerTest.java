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
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.TxtRobotTestDataLexer;
import org.robotframework.ide.core.testData.text.lexer.helpers.ReadersProvider;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * Helper class for recognizing tests.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * 
 */
public abstract class ARecognizerTest {

    @ForClean
    @Mock
    protected ReadersProvider readersProvider;

    @ForClean
    @Mock
    protected Reader reader;

    @ForClean
    @Mock
    protected File file;

    @ForClean
    protected IContextRecognizer context;

    @ForClean
    private final Class<? extends IContextRecognizer> recognizerClass;


    protected ARecognizerTest(
            final Class<? extends IContextRecognizer> recognizerClass) {
        this.recognizerClass = recognizerClass;
    }


    protected void assertTheSameLinesContext(
            final List<IContextElement> recognize,
            Class<? extends IContextElement> aClass, int expectedSize) {
        assertThat(recognize).hasSize(expectedSize);

        for (int i = 0; i < expectedSize; i++) {
            IContextElement iContextElement = recognize.get(i);
            assertThat(iContextElement).isInstanceOf(aClass);
            OneLineSingleRobotContextPart line = (OneLineSingleRobotContextPart) iContextElement;
            assertThat(line.getLineNumber()).isEqualTo(1);
            assertThat(line.getParent()).isNull();
            assertThat(line.getType()).isEqualTo(context.getContextType());
        }
    }


    protected void assertThatIsExepectedContext(final String prefix,
            final String text, final String suffix,
            final IRobotTokenType[] types) throws FileNotFoundException,
            IOException {
        // prepare
        String toTest = "";
        if (prefix != null) {
            toTest += prefix;
        }
        int column = toTest.length() + 1;
        toTest += text;
        if (suffix != null) {
            toTest += suffix;
        }

        TokenOutput tokenOutput = createTokenOutput(toTest);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        if (line == null) {
            line = new LineTokenPosition(0, 0, FilePosition.THE_FIRST_LINE);
        }
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        OneLineSingleRobotContextPart header = assertAndGetOneLineContext(recognize);

        assertTokensForUnknownWords(header.getContextTokens(), types, 0,
                new FilePosition(1, column), new String[] {});
    }


    protected OneLineSingleRobotContextPart assertAndGetOneLineContext(
            final List<IContextElement> recognize) {
        assertThat(recognize).hasSize(1);
        IContextElement iContextElement = recognize.get(0);
        assertThat(iContextElement).isInstanceOf(
                OneLineSingleRobotContextPart.class);
        OneLineSingleRobotContextPart line = (OneLineSingleRobotContextPart) iContextElement;
        assertThat(line.getLineNumber()).isEqualTo(1);
        assertThat(line.getParent()).isNull();
        assertThat(line.getType()).isEqualTo(context.getContextType());

        return line;
    }


    protected void assertForIncorrectData(String text)
            throws FileNotFoundException, IOException {
        // prepare
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


    protected TokenOutput createTokenOutput(String text)
            throws FileNotFoundException, IOException {
        when(readersProvider.create(file)).thenReturn(new StringReader(text));
        doCallRealMethod().when(readersProvider).newCharBuffer(anyInt());
        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer(readersProvider);
        TokenOutput tokenOutput = lexer.extractTokens(file);
        return tokenOutput;
    }


    protected void assertExpectedSequenceAllMandatory(IRobotTokenType... types) {
        List<ExpectedSequenceElement> sequence = new LinkedList<>();
        for (IRobotTokenType t : types) {
            sequence.add(ExpectedSequenceElement.buildMandatory(t));
        }

        assertThat(((ATableElementRecognizer) context).getExpectedElements())
                .containsExactlyElementsOf(sequence);
    }


    @Before
    public void setUp() throws InstantiationException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        context = recognizerClass.newInstance();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
