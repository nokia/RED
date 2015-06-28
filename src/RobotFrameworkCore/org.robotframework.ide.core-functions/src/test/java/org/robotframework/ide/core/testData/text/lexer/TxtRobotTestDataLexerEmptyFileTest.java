package org.robotframework.ide.core.testData.text.lexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.invocation.InvocationOnMock;
import org.robotframework.ide.core.testData.text.lexer.helpers.ReadersProvider;
import org.robotframework.ide.core.testHelpers.AnswerRecorder;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see TxtRobotTestDataLexer
 */
public class TxtRobotTestDataLexerEmptyFileTest {

    @ForClean
    @Mock
    private ReadersProvider readersProvider;
    @ForClean
    @Mock
    private Reader reader;
    @ForClean
    @Mock
    private File file;


    @Test
    public void test_ifReaderCharBufferHasCorrectSize() throws IOException {
        // prepare
        AnswerRecorder<Object> recorder = new AnswerRecorder<>(new Returns(-1));

        when(readersProvider.create(any(File.class))).thenReturn(reader);
        doCallRealMethod().when(readersProvider).newCharBuffer(
                any(Integer.class));
        when(reader.read(any(CharBuffer.class))).then(recorder);
        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer(readersProvider);

        // execute
        lexer.extractTokens(file);

        // verify
        List<InvocationOnMock> invocations = recorder.getInvocations();
        assertThat(invocations).hasSize(1);
        InvocationOnMock invocation = invocations.get(0);
        assertThat(invocation.getArguments().length).isEqualTo(1);

        Object o = invocation.getArguments()[0];
        assertThat(o).isInstanceOf(CharBuffer.class);
        CharBuffer c = (CharBuffer) o;
        assertThat(c.limit()).isGreaterThan(0);
    }


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
