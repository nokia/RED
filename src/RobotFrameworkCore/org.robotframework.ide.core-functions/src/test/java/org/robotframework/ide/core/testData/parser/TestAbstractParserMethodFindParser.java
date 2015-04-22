package org.robotframework.ide.core.testData.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.model.table.IRobotSectionTable;
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
    @Mock
    private ByteBufferInputStream dataInStream;


    @Test
    public void test_noTableParserFound_sayThatDataCouldNotBeParsed_noMoreInteractionShouldBePerformed() {
        // prepare
        when(settingsParser.canParse(dataInStream)).thenReturn(false);
        when(variablesParser.canParse(dataInStream)).thenReturn(false);
        when(testCasesParser.canParse(dataInStream)).thenReturn(false);
        when(keywordsParser.canParse(dataInStream)).thenReturn(false);

        InOrder orderMethodExec = inOrder(parsersProvider, settingsParser,
                variablesParser, testCasesParser, keywordsParser, dataInStream);

        // execute
        ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> parserFound = robotParser
                .findParser(dataInStream);

        // verify
        assertThat(parserFound).isNull();

        orderMethodExec.verify(dataInStream, times(1)).mark();
        orderMethodExec.verify(parsersProvider, times(1))
                .getSettingsTableParser();
        orderMethodExec.verify(settingsParser, times(1)).canParse(dataInStream);
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verify(parsersProvider, times(1))
                .getVariablesTableParser();
        orderMethodExec.verify(variablesParser, times(1))
                .canParse(dataInStream);
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verify(parsersProvider, times(1))
                .getTestCasesTableParser();
        orderMethodExec.verify(testCasesParser, times(1))
                .canParse(dataInStream);
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verify(parsersProvider, times(1))
                .getKeywordsTableParser();
        orderMethodExec.verify(keywordsParser, times(1)).canParse(dataInStream);

        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verifyNoMoreInteractions();
    }


    @Test
    public void test_keywordsTableParser_sayThatDataCouldBeParsed_noMoreInteractionShouldBePerformed() {
        // prepare
        when(settingsParser.canParse(dataInStream)).thenReturn(false);
        when(variablesParser.canParse(dataInStream)).thenReturn(false);
        when(testCasesParser.canParse(dataInStream)).thenReturn(false);
        when(keywordsParser.canParse(dataInStream)).thenReturn(true);

        InOrder orderMethodExec = inOrder(parsersProvider, settingsParser,
                variablesParser, testCasesParser, keywordsParser, dataInStream);

        // execute
        ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> parserFound = robotParser
                .findParser(dataInStream);

        // verify
        assertThat(parserFound).isNotNull();
        assertThat(parserFound).isEqualTo(keywordsParser);

        orderMethodExec.verify(dataInStream, times(1)).mark();
        orderMethodExec.verify(parsersProvider, times(1))
                .getSettingsTableParser();
        orderMethodExec.verify(settingsParser, times(1)).canParse(dataInStream);
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verify(parsersProvider, times(1))
                .getVariablesTableParser();
        orderMethodExec.verify(variablesParser, times(1))
                .canParse(dataInStream);
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verify(parsersProvider, times(1))
                .getTestCasesTableParser();
        orderMethodExec.verify(testCasesParser, times(1))
                .canParse(dataInStream);
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verify(parsersProvider, times(1))
                .getKeywordsTableParser();
        orderMethodExec.verify(keywordsParser, times(1)).canParse(dataInStream);

        // for return propose
        orderMethodExec.verify(parsersProvider, times(1))
                .getKeywordsTableParser();
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verifyNoMoreInteractions();
    }


    @Test
    public void test_testCaseTableParser_sayThatDataCouldBeParsed_noMoreInteractionShouldBePerformed() {
        // prepare
        when(settingsParser.canParse(dataInStream)).thenReturn(false);
        when(variablesParser.canParse(dataInStream)).thenReturn(false);
        when(testCasesParser.canParse(dataInStream)).thenReturn(true);
        when(keywordsParser.canParse(dataInStream)).thenReturn(false);

        InOrder orderMethodExec = inOrder(parsersProvider, settingsParser,
                variablesParser, testCasesParser, keywordsParser, dataInStream);

        // execute
        ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> parserFound = robotParser
                .findParser(dataInStream);

        // verify
        assertThat(parserFound).isNotNull();
        assertThat(parserFound).isEqualTo(testCasesParser);

        orderMethodExec.verify(dataInStream, times(1)).mark();
        orderMethodExec.verify(parsersProvider, times(1))
                .getSettingsTableParser();
        orderMethodExec.verify(settingsParser, times(1)).canParse(dataInStream);
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verify(parsersProvider, times(1))
                .getVariablesTableParser();
        orderMethodExec.verify(variablesParser, times(1))
                .canParse(dataInStream);
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verify(parsersProvider, times(1))
                .getTestCasesTableParser();
        orderMethodExec.verify(testCasesParser, times(1))
                .canParse(dataInStream);

        // for return propose
        orderMethodExec.verify(parsersProvider, times(1))
                .getTestCasesTableParser();
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verifyNoMoreInteractions();
    }


    @Test
    public void test_variablesTableParser_sayThatDataCouldBeParsed_noMoreInteractionShouldBePerformed() {
        // prepare
        when(settingsParser.canParse(dataInStream)).thenReturn(false);
        when(variablesParser.canParse(dataInStream)).thenReturn(true);
        when(testCasesParser.canParse(dataInStream)).thenReturn(false);
        when(keywordsParser.canParse(dataInStream)).thenReturn(false);

        InOrder orderMethodExec = inOrder(parsersProvider, settingsParser,
                variablesParser, testCasesParser, keywordsParser, dataInStream);

        // execute
        ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> parserFound = robotParser
                .findParser(dataInStream);

        // verify
        assertThat(parserFound).isNotNull();
        assertThat(parserFound).isEqualTo(variablesParser);

        orderMethodExec.verify(dataInStream, times(1)).mark();
        orderMethodExec.verify(parsersProvider, times(1))
                .getSettingsTableParser();
        orderMethodExec.verify(settingsParser, times(1)).canParse(dataInStream);
        orderMethodExec.verify(dataInStream, times(1)).reset();
        orderMethodExec.verify(parsersProvider, times(1))
                .getVariablesTableParser();
        orderMethodExec.verify(variablesParser, times(1))
                .canParse(dataInStream);

        // for return propose
        orderMethodExec.verify(parsersProvider, times(1))
                .getVariablesTableParser();
        orderMethodExec.verify(dataInStream, times(2)).reset();
        orderMethodExec.verifyNoMoreInteractions();
    }


    @Test
    public void test_settingTableParser_sayThatDataCouldBeParsed_noMoreInteractionShouldBePerformed() {
        // prepare
        when(settingsParser.canParse(dataInStream)).thenReturn(true);
        when(variablesParser.canParse(dataInStream)).thenReturn(false);
        when(testCasesParser.canParse(dataInStream)).thenReturn(false);
        when(keywordsParser.canParse(dataInStream)).thenReturn(false);

        InOrder orderMethodExec = inOrder(parsersProvider, settingsParser,
                variablesParser, testCasesParser, keywordsParser, dataInStream);

        // execute
        ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> parserFound = robotParser
                .findParser(dataInStream);

        // verify
        assertThat(parserFound).isNotNull();
        assertThat(parserFound).isEqualTo(settingsParser);

        orderMethodExec.verify(dataInStream, times(1)).mark();
        orderMethodExec.verify(parsersProvider, times(1))
                .getSettingsTableParser();
        orderMethodExec.verify(settingsParser, times(1)).canParse(dataInStream);
        // for return propose
        orderMethodExec.verify(parsersProvider, times(1))
                .getSettingsTableParser();
        orderMethodExec.verify(dataInStream, times(3)).reset();
        orderMethodExec.verifyNoMoreInteractions();
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
