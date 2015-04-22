package org.robotframework.ide.core.testData.parser.txt;

import static org.mockito.Mockito.spy;

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


    @Test(timeout = 100)
    public void test_emptyFile() throws UnsupportedEncodingException {
        // prepare
        String fileContent = "";
        ByteBuffer dataReal = ByteBuffer.wrap(fileContent.getBytes("UTF-8"));
        ByteBuffer dataSpied = spy(dataReal);
        ByteBufferInputStream dataFile = new ByteBufferInputStream(dataSpied);
        ByteBufferInputStream dataFileSpied = spy(dataFile);

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> result = txtParser
                .parse(dataFileSpied);

        // VERIFICATION
        // verify - created object

        // verify - execution order

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
