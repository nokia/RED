package org.robotframework.ide.core.testData.text.context.iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.TxtRobotTestDataLexer;
import org.robotframework.ide.core.testData.text.lexer.helpers.ReadersProvider;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;
import org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see TokensLineIterator
 */
public class TokensLineIteratorTest {

    @ForClean
    private TokensLineIterator lineIter;
    @ForClean
    @Mock
    private ReadersProvider readersProvider;
    @ForClean
    @Mock
    private File dummyFile;


    @Test(expected = UnsupportedOperationException.class)
    public void test_remove_shouldThrown_UnsupportedOperationException() {
        TokenOutput t = new TokenOutput();
        lineIter = new TokensLineIterator(t);

        // execute
        lineIter.remove();
    }


    @Test
    public void test_forThreeLines_lastLineWithEndOfLine() throws IOException {
        // prepare
        TokenOutput tokenOutput = createFourLinesTokenOutputWithEndOfLine();
        lineIter = new TokensLineIterator(tokenOutput);

        // execute & verify
        assertThat(lineIter.hasNext()).isTrue();
        LineTokenPosition pos = lineIter.next();
        assertThat(pos).isNotNull();
        assertThat(pos.getStart()).isEqualTo(0);
        assertThat(pos.getLineNumber()).isEqualTo(1);
        assertThat(pos.getEnd()).isEqualTo(5);

        assertThat(lineIter.hasNext()).isTrue();
        pos = lineIter.next();
        assertThat(pos).isNotNull();
        assertThat(pos.getStart()).isEqualTo(6);
        assertThat(pos.getLineNumber()).isEqualTo(2);
        assertThat(pos.getEnd()).isEqualTo(11);

        assertThat(lineIter.hasNext()).isTrue();
        pos = lineIter.next();
        assertThat(pos).isNotNull();
        assertThat(pos.getStart()).isEqualTo(12);
        assertThat(pos.getLineNumber()).isEqualTo(3);
        assertThat(pos.getEnd()).isEqualTo(14);

        assertThat(lineIter.hasNext()).isFalse();
        assertThat(lineIter.next()).isNull();
        assertThat(lineIter.hasNext()).isFalse();
        assertThat(lineIter.next()).isNull();
    }


    private TokenOutput createFourLinesTokenOutputWithEndOfLine()
            throws IOException {
        final String fileContent = "* *\r\n* *\r\n\r\n";
        when(readersProvider.create(dummyFile)).thenReturn(
                new StringReader(fileContent));
        doCallRealMethod().when(readersProvider).newCharBuffer(anyInt());
        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer(readersProvider);
        TokenOutput tokenOutput = lexer.extractTokens(dummyFile);

        assertThat(tokenOutput.getTokens().size()).isEqualTo(15);
        assertThat(tokenOutput.getCurrentMarker().getLine()).isEqualTo(4);

        return tokenOutput;
    }


    @Test
    public void test_forTwoLines_lastLineWithEndOfLine() throws IOException {
        // prepare
        TokenOutput tokenOutput = createThreeLinesTokenOutputWithEndOfLine();
        lineIter = new TokensLineIterator(tokenOutput);

        // execute & verify
        assertThat(lineIter.hasNext()).isTrue();
        LineTokenPosition pos = lineIter.next();
        assertThat(pos).isNotNull();
        assertThat(pos.getStart()).isEqualTo(0);
        assertThat(pos.getLineNumber()).isEqualTo(1);
        assertThat(pos.getEnd()).isEqualTo(5);

        assertThat(lineIter.hasNext()).isTrue();
        pos = lineIter.next();
        assertThat(pos).isNotNull();
        assertThat(pos.getStart()).isEqualTo(6);
        assertThat(pos.getLineNumber()).isEqualTo(2);
        assertThat(pos.getEnd()).isEqualTo(11);

        assertThat(lineIter.hasNext()).isFalse();
        assertThat(lineIter.next()).isNull();
        assertThat(lineIter.hasNext()).isFalse();
        assertThat(lineIter.next()).isNull();
    }


    private TokenOutput createThreeLinesTokenOutputWithEndOfLine()
            throws IOException {
        final String fileContent = "* *\r\n* *\r\n";
        when(readersProvider.create(dummyFile)).thenReturn(
                new StringReader(fileContent));
        doCallRealMethod().when(readersProvider).newCharBuffer(anyInt());
        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer(readersProvider);
        TokenOutput tokenOutput = lexer.extractTokens(dummyFile);

        assertThat(tokenOutput.getTokens().size()).isEqualTo(12);
        assertThat(tokenOutput.getCurrentMarker().getLine()).isEqualTo(3);

        return tokenOutput;
    }


    @Test
    public void test_forTwoLines_lastLineWithoutEndOfLine() throws IOException {
        // prepare
        TokenOutput tokenOutput = createTwoLinesTokenOutput();
        lineIter = new TokensLineIterator(tokenOutput);

        // execute & verify
        assertThat(lineIter.hasNext()).isTrue();
        LineTokenPosition pos = lineIter.next();
        assertThat(pos).isNotNull();
        assertThat(pos.getStart()).isEqualTo(0);
        assertThat(pos.getLineNumber()).isEqualTo(1);
        assertThat(pos.getEnd()).isEqualTo(5);

        assertThat(lineIter.hasNext()).isTrue();
        pos = lineIter.next();
        assertThat(pos).isNotNull();
        assertThat(pos.getStart()).isEqualTo(6);
        assertThat(pos.getLineNumber()).isEqualTo(2);
        assertThat(pos.getEnd()).isEqualTo(9);

        assertThat(lineIter.hasNext()).isFalse();
        assertThat(lineIter.next()).isNull();
        assertThat(lineIter.hasNext()).isFalse();
        assertThat(lineIter.next()).isNull();
    }


    private TokenOutput createTwoLinesTokenOutput() throws IOException {
        final String fileContent = "* *\r\n* *";
        when(readersProvider.create(dummyFile)).thenReturn(
                new StringReader(fileContent));
        doCallRealMethod().when(readersProvider).newCharBuffer(anyInt());
        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer(readersProvider);
        TokenOutput tokenOutput = lexer.extractTokens(dummyFile);

        assertThat(tokenOutput.getTokens().size()).isEqualTo(9);
        assertThat(tokenOutput.getCurrentMarker().getLine()).isEqualTo(2);

        return tokenOutput;
    }


    @Test
    public void test_noEndOfLineTokens_butTwoAsterisksTokenExists() {
        // prepare
        TokenOutput tokenOutput = TokenOutputAsserationHelper
                .createTokenOutputWithTwoAsterisksInside();
        lineIter = new TokensLineIterator(tokenOutput);

        // execute & verify
        assertThat(lineIter.hasNext()).isTrue();
        LineTokenPosition pos = lineIter.next();
        assertThat(pos).isNotNull();
        assertThat(pos.getStart()).isEqualTo(0);
        assertThat(pos.getLineNumber()).isEqualTo(1);
        assertThat(pos.getEnd()).isEqualTo(2);

        // second iteration
        assertThat(lineIter.hasNext()).isFalse();
        assertThat(lineIter.next()).isNull();
    }


    @Test
    public void test_emptyFile_shouldReturn_NULL_and_sayThatWeDoNotHaveAnyElements() {
        // prepare
        TokenOutput tokenOutput = new TokenOutput();
        lineIter = new TokensLineIterator(tokenOutput);

        // execute & verify
        assertThat(lineIter.hasNext()).isFalse();
        assertThat(lineIter.next()).isNull();
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
