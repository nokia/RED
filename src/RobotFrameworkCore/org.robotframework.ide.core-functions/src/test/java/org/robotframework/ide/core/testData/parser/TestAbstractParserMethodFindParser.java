package org.robotframework.ide.core.testData.parser;

import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see AbstractRobotFrameworkFileParser#findParser(IParsePositionMarkable)
 */
public class TestAbstractParserMethodFindParser {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> robotParser;
    @Mock
    private ITestDataParserProvider<ByteBufferInputStream> parsersProvider;
    @Mock
    private ITestDataElementParser<ByteBufferInputStream, SettingTable> settingsParser;
    @Mock
    private ITestDataElementParser<ByteBufferInputStream, KeywordTable> keywordsParser;
    @Mock
    private ITestDataElementParser<ByteBufferInputStream, TestCaseTable> testCasesParser;
    @Mock
    private ITestDataElementParser<ByteBufferInputStream, VariablesTable> variablesParser;


    @Test
    public void test_settingTableParser_sayThatDataCouldBeParsed_noMoreInteractionShouldBePerformed() {

    }


    @Before
    public void setUp() throws IllegalArgumentException, MissingParserException {
        MockitoAnnotations.initMocks(this);
        when(parsersProvider.getSettingsTableParser()).thenReturn(
                settingsParser);
        when(parsersProvider.getVariablesTableParser()).thenReturn(
                variablesParser);
        when(parsersProvider.getTestCasesTableParser()).thenReturn(
                testCasesParser);
        when(parsersProvider.getKeywordsTableParser()).thenReturn(
                keywordsParser);
        robotParser = new DummyRobotParser(parsersProvider);
    }


    @After
    public void tearDown() {
        settingsParser = null;
        variablesParser = null;
        testCasesParser = null;
        keywordsParser = null;
        parsersProvider = null;
        robotParser = null;
    }

    private class DummyRobotParser extends
            AbstractRobotFrameworkFileParser<ByteBufferInputStream> {

        public DummyRobotParser(
                ITestDataParserProvider<ByteBufferInputStream> parsersProvider)
                throws MissingParserException, IllegalArgumentException {
            super(parsersProvider);
        }


        @Override
        public ParseResult<ByteBufferInputStream, TestDataFile> parse(
                ByteBufferInputStream testData) {
            return null;
        }


        @Override
        public boolean canAcceptFile(File file) {
            return false;
        }
    }
}
