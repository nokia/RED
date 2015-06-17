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
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
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


    @Test
    public void test_extractTokens_emptyFile_shouldReturn_onlyTwoTokensStartLineAndEndOfFileTokens()
            throws IOException {
        // prepare
        when(readersProvider.create(any(File.class))).thenReturn(reader);
        when(reader.read(any(CharBuffer.class))).thenReturn(-1);
        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer(readersProvider);

        // execute
        TokenOutput out = lexer.extractTokens(file);

        // verify
    }


    private void assertThatTokenIsAsExpected(RobotToken tokenToExaminate,
            RobotTokenType type, int startLine, int startColumn, int endLine,
            int endColumn, String text) {
        assertThat(tokenToExaminate).isNotNull();
        assertThat(tokenToExaminate.getType()).isEqualTo(type);

        LinearPositionMarker startPosition = tokenToExaminate
                .getStartPosition();
        assertLinearPositionMarker(startPosition, startLine, startColumn);
        LinearPositionMarker endPosition = tokenToExaminate.getEndPosition();
        assertLinearPositionMarker(endPosition, endLine, endColumn);

        if (text == null) {
            assertThat(tokenToExaminate.getText()).isEqualTo(null);
        } else {
            assertThat(tokenToExaminate.getText()).isNotNull();
            assertThat(tokenToExaminate.getText().toString()).isEqualTo(text);
        }
    }


    private void assertLinearPositionMarker(LinearPositionMarker marker,
            int line, int column) {
        assertThat(marker).isNotNull();
        assertThat(marker.getLine()).isEqualTo(line);
        assertThat(marker.getColumn()).isEqualTo(column);
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
