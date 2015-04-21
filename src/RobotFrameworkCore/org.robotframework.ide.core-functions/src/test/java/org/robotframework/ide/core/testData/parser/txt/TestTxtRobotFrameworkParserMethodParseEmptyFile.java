package org.robotframework.ide.core.testData.parser.txt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.MissingParserException;
import org.robotframework.ide.core.testData.parser.result.MessageType;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.result.ParserMessage;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * Tests for empty file
 * 
 * @author wypych
 * @see TxtRobotFrameworkParser#parse(org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream)
 */
public class TestTxtRobotFrameworkParserMethodParseEmptyFile {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> parser;


    @Test
    public void test_oneSpaceInsideFile_oneLetterA_and_newLineInWindowsAndLinux() {
        // prepare
        String fileContentAsText = " A\r\n\n";
        byte[] content = fileContentAsText.getBytes();
        ByteBufferInputStream dataInStreamReal = new ByteBufferInputStream(
                ByteBuffer.wrap(content));
        ByteBufferInputStream dataInStreamSpied = spy(dataInStreamReal);

        InOrder order = inOrder(dataInStreamSpied);

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> parseResult = parser
                .parse(dataInStreamSpied);

        // verify - execution
        order.verify(dataInStreamSpied, times(3)).available();
        for (int i = 0; i < fileContentAsText.length() - 1; i++) {
            order.verify(dataInStreamSpied, times(1)).currentByteInBuffer();
            order.verify(dataInStreamSpied, times(1)).read();
            order.verify(dataInStreamSpied, times(1)).available();
        }
        order.verify(dataInStreamSpied, times(1)).currentByteInBuffer();
        order.verify(dataInStreamSpied, times(1)).read();
        order.verify(dataInStreamSpied, times(2)).available();

        order.verifyNoMoreInteractions();

        // verify
        assertThat(parseResult).isNotNull();
        assertThat(parseResult.getDataConsumed()).isNotNull();
        assertThat(parseResult.getDataConsumed().getByteBuffer()).isNotNull();
        assertThat(parseResult.getOutOfOrderElementsFound()).isEmpty();
        String localization = "byte: 0";
        String message = "Unrecognized data found: '" + fileContentAsText
                + "'.";
        assertThat(parseResult.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.WARN, localization, message));
        assertThat(parseResult.getProducedModelElement()).isEqualTo(
                new TestDataFile());
        assertThat(parseResult.getTrashData()).hasSize(1);
        // get array because buffer was move own position
        assertThat(parseResult.getTrashData().get(0).getByteBuffer().array())
                .isEqualTo(dataInStreamReal.getByteBuffer().array());
    }


    @Test
    public void test_oneSpaceInsideFile_shouldReturnProperInformation() {
        // prepare
        String fileContentAsText = " ";
        byte[] content = fileContentAsText.getBytes();
        ByteBufferInputStream dataInStreamReal = new ByteBufferInputStream(
                ByteBuffer.wrap(content));
        ByteBufferInputStream dataInStreamSpied = spy(dataInStreamReal);

        InOrder order = inOrder(dataInStreamSpied);

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> parseResult = parser
                .parse(dataInStreamSpied);

        // verify - execution
        order.verify(dataInStreamSpied, times(3)).available();
        order.verify(dataInStreamSpied, times(1)).currentByteInBuffer();
        order.verify(dataInStreamSpied, times(1)).read();
        order.verify(dataInStreamSpied, times(2)).available();

        order.verifyNoMoreInteractions();

        // verify
        assertThat(parseResult).isNotNull();
        assertThat(parseResult.getDataConsumed()).isNotNull();
        assertThat(parseResult.getDataConsumed().getByteBuffer()).isNotNull();
        assertThat(parseResult.getOutOfOrderElementsFound()).isEmpty();
        String localization = "byte: 0";
        String message = "Unrecognized data found: '" + fileContentAsText
                + "'.";
        assertThat(parseResult.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.WARN, localization, message));
        assertThat(parseResult.getProducedModelElement()).isEqualTo(
                new TestDataFile());
        assertThat(parseResult.getTrashData()).hasSize(1);
        // get array because buffer was move own position
        assertThat(parseResult.getTrashData().get(0).getByteBuffer().array())
                .isEqualTo(dataInStreamReal.getByteBuffer().array());
    }


    @Test
    public void test_zeroLengthFile_shouldReturnProperInformation() {
        // prepare
        ByteBufferInputStream dataInStreamReal = new ByteBufferInputStream(
                ByteBuffer.allocate(0));
        ByteBufferInputStream dataInStreamSpied = spy(dataInStreamReal);

        InOrder order = inOrder(dataInStreamSpied);

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> parseResult = parser
                .parse(dataInStreamSpied);

        // verify - execution
        order.verify(dataInStreamSpied, times(1)).available();
        order.verifyNoMoreInteractions();

        // verify
        assertThat(parseResult).isNotNull();
        assertThat(parseResult.getDataConsumed()).isNotNull();
        assertThat(parseResult.getDataConsumed().getByteBuffer()).isNotNull();
        assertThat(parseResult.getDataConsumed().getByteBuffer()).isEqualTo(
                dataInStreamReal.getByteBuffer());
        assertThat(parseResult.getOutOfOrderElementsFound()).isEmpty();
        String localization = "line: 0, column: 0";
        String message = "Empty file.";
        assertThat(parseResult.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.INFO, localization, message));
        assertThat(parseResult.getProducedModelElement()).isEqualTo(
                new TestDataFile());
        assertThat(parseResult.getTrashData()).isEmpty();
    }


    @Before
    public void setUp() throws IllegalArgumentException, MissingParserException {
        parser = new TxtRobotFrameworkParser(new TxtTestDataParserProvider());
    }


    @After
    public void tearDown() {
        parser = null;
    }
}
