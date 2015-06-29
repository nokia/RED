package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineRobotContext;
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


    protected OneLineRobotContext assertAndGetOneLineContext(
            final List<IContextElement> recognize) {
        assertThat(recognize).hasSize(1);
        IContextElement iContextElement = recognize.get(0);
        assertThat(iContextElement).isInstanceOf(OneLineRobotContext.class);
        OneLineRobotContext comment = (OneLineRobotContext) iContextElement;
        assertThat(comment.getLineNumber()).isEqualTo(1);
        assertThat(comment.getParent()).isNull();
        assertThat(comment.getParentContext()).isNull();
        assertThat(comment.getType()).isEqualTo(context.getContextType());

        return comment;
    }


    protected TokenOutput createTokenOutput(String text)
            throws FileNotFoundException, IOException {
        when(readersProvider.create(file)).thenReturn(new StringReader(text));
        doCallRealMethod().when(readersProvider).newCharBuffer(anyInt());
        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer(readersProvider);
        TokenOutput tokenOutput = lexer.extractTokens(file);
        return tokenOutput;
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
