package org.robotframework.ide.core.testData.parser.txt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.MissingParserException;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.result.TrashData;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see TxtRobotFrameworkParser#parse(org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream)
 */
public class TestTxtRobotParserOnlyIncorrectDataCase {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> txtParser;


    @Test(timeout = 10000)
    public void test_fileWithOneSpaceEOLonLinuxPlusEOLonWindows()
            throws UnsupportedEncodingException {
        // prepare
        String fileContent = "  \n\r\n";
        byte[] bytes = fileContent.getBytes("UTF-8");
        ByteBuffer data = ByteBuffer.wrap(bytes);
        ByteBufferInputStream dataFile = new ByteBufferInputStream(data);

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> result = txtParser
                .parse(dataFile);

        // verify - created object
        assertThat(result).isNotNull();
        assertThat(result.getDataConsumed()).isEqualTo(dataFile);
        assertThat(result.getElementLocation()).isNull();
        assertThat(result.getParserMessages()).isEmpty();
        assertThat(result.getProducedModelElement()).isEqualTo(
                new TestDataFile());
        assertThat(result.getResult());
        assertThat(result.getTrashData()).hasSize(1);
        TrashData<ByteBufferInputStream> trashData = result.getTrashData().get(
                0);
        assertThat(trashData.getTrash()).isNotNull();
        ByteBuffer trashBuffer = trashData.getTrash().getByteBuffer();
        assertThat(trashBuffer).isNotNull();
        assertThat(trashBuffer.array()).isEqualTo(bytes);
        assertThat(trashData.getLocation()).isNotNull();
        ByteLocator locator = (ByteLocator) trashData.getLocation();
        assertThat(locator.getData()).isNotNull();
        ByteBufferInputStream locatorData = locator.getData();
        assertThat(locatorData.getByteBuffer().array()).isEqualTo(bytes);
    }


    @Test(timeout = 10000)
    public void test_fileWithOneSpace() throws UnsupportedEncodingException {
        // prepare
        String fileContent = " ";
        byte[] bytes = fileContent.getBytes("UTF-8");
        ByteBuffer data = ByteBuffer.wrap(bytes);
        ByteBufferInputStream dataFile = new ByteBufferInputStream(data);

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> result = txtParser
                .parse(dataFile);

        // verify - created object
        assertThat(result).isNotNull();
        assertThat(result.getDataConsumed()).isEqualTo(dataFile);
        assertThat(result.getElementLocation()).isNull();
        assertThat(result.getParserMessages()).isEmpty();
        assertThat(result.getProducedModelElement()).isEqualTo(
                new TestDataFile());
        assertThat(result.getResult());
        assertThat(result.getTrashData()).hasSize(1);
        TrashData<ByteBufferInputStream> trashData = result.getTrashData().get(
                0);
        assertThat(trashData.getTrash()).isNotNull();
        ByteBuffer trashBuffer = trashData.getTrash().getByteBuffer();
        assertThat(trashBuffer).isNotNull();
        assertThat(trashBuffer.array()).isEqualTo(bytes);
        assertThat(trashData.getLocation()).isNotNull();
        ByteLocator locator = (ByteLocator) trashData.getLocation();
        assertThat(locator.getData()).isNotNull();
        ByteBufferInputStream locatorData = locator.getData();
        assertThat(locatorData.getByteBuffer().array()).isEqualTo(bytes);
    }


    @Test(timeout = 10000)
    public void test_emptyFile() throws UnsupportedEncodingException {
        // prepare
        String fileContent = "";
        ByteBuffer dataMock = ByteBuffer.wrap(fileContent.getBytes("UTF-8"));
        ByteBufferInputStream dataFile = new ByteBufferInputStream(dataMock);

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> result = txtParser
                .parse(dataFile);

        // verify - created object
        assertThat(result).isNotNull();
        assertThat(result.getDataConsumed()).isEqualTo(dataFile);
        assertThat(result.getElementLocation()).isNull();
        assertThat(result.getParserMessages()).isEmpty();
        assertThat(result.getProducedModelElement()).isEqualTo(
                new TestDataFile());
        assertThat(result.getResult());
        assertThat(result.getTrashData()).isEmpty();
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
