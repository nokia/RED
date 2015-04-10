package org.robotframework.ide.core.testData.parser.txt;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.ITestDataParserProvider;
import org.robotframework.ide.core.testData.parser.MissingParserException;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see TxtRobotFrameworkFileParser
 */
public class TestTxtParserFileTestsForEmptyFile {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> parser;
    private ITestDataParserProvider<ByteBufferInputStream> parsersProvider;


    @Test
    public void test_totalEmptyFile_should() throws IllegalArgumentException,
            MissingParserException {
        // prepare
        String fileContent = "";

        File f = mock(File.class);
        String g = mock(String.class);
        when(f.getName()).thenReturn(g);
        // execute
        parser.canAcceptFile(f);
        // verify
    }


    @Before
    public void setUp() throws IllegalArgumentException, MissingParserException {
        parsersProvider = new TxtTestDataParserProvider();
        parser = new TxtRobotFrameworkFileParser(parsersProvider);
    }


    @After
    public void tearDown() {
        parser = null;
        parsersProvider = null;
    }
}
