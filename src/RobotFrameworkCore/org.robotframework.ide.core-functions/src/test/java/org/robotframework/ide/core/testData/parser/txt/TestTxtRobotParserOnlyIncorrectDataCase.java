package org.robotframework.ide.core.testData.parser.txt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.MissingParserException;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see TxtRobotFrameworkParser#parse(org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream)
 */
public class TestTxtRobotParserOnlyIncorrectDataCase {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> txtParser;


    @Test(timeout = 10000)
    public void test_emptyFile() throws UnsupportedEncodingException {
        // prepare
        String fileContent = "";
        ByteBuffer dataReal = createMockForByteBuffer(fileContent);
        ByteBufferInputStream dataFile = new ByteBufferInputStream(dataReal);
        ByteBufferInputStream dataFileSpied = spy(dataFile);

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> result = txtParser
                .parse(dataFileSpied);

        // VERIFICATION
        // verify - created object
        assertThat(result).isNotNull();
        assertThat(result.getDataConsumed()).isEqualTo(dataFileSpied);
        assertThat(result.getElementLocation()).isNull();
        assertThat(result.getParserMessages()).isEmpty();
        assertThat(result.getProducedModelElement()).isEqualTo(
                new TestDataFile());
        assertThat(result.getResult());
        assertThat(result.getTrashData()).isEmpty();

        // verify - execution order

    }


    private ByteBuffer createMockForByteBuffer(String data)
            throws UnsupportedEncodingException {
        ByteBuffer dataReal = mock(ByteBuffer.class);
        byte[] asByteArray = data.getBytes("UTF-8");
        for (byte b : asByteArray) {
            when(dataReal.get()).thenReturn(b);
        }

        return dataReal;
    }


    @Before
    public void setUp() throws IllegalArgumentException, MissingParserException {
        txtParser = new TxtRobotFrameworkParser(new TxtTestDataParserProvider());
    }


    @After
    public void tearDown() {
        txtParser = null;
    }
}
