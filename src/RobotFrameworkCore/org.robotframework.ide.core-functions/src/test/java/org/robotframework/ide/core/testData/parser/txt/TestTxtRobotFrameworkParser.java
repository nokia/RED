package org.robotframework.ide.core.testData.parser.txt;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.ITestDataParserProvider;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


public class TestTxtRobotFrameworkParser {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> txtParser;


    @Test
    public void test_canAcceptFile_fileNotExists_shouldReturnFalse() {

    }


    @Before
    public void setUp() throws Exception {
        ITestDataParserProvider<ByteBufferInputStream> parsersProvider = new TxtTestDataParserProvider();
        txtParser = new TxtRobotFrameworkParser(parsersProvider);
    }


    @After
    public void tearDown() {
        txtParser = null;
    }
}
