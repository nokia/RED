package org.robotframework.ide.core.testData.parser.txt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.MissingParserException;
import org.robotframework.ide.core.testData.parser.result.MessageType;
import org.robotframework.ide.core.testData.parser.result.ParseProcessResult;
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
    public void test_zeroLengthFile_shouldGiveAnyInformationAboutThisFact() {
        // prepare
        ByteBufferInputStream testData = spy(new ByteBufferInputStream(
                ByteBuffer.allocate(0)));

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> parseResult = parser
                .parse(testData);

        // verify
        assertThat(parseResult).isNotNull();
        assertThat(parseResult.getDataConsumed()).isEqualTo(testData);
        assertThat(parseResult.getOutOfOrderElementsFound()).isEmpty();
        String localization = "Begin of file.";
        String message = "File is empty.";
        assertThat(parseResult.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.INFO, localization, message));
        assertThat(parseResult.getProducedModelElement()).isEqualTo(
                new TestDataFile());
        assertThat(parseResult.getResult()).isEqualTo(
                ParseProcessResult.PARSED_WITH_SUCCESS);
        ByteBufferInputStream dataTrash = parseResult
                .getTrashBeforeFirstExpectedData();
        assertThat(dataTrash).isNull();
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
