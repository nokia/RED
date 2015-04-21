package org.robotframework.ide.core.testData.parser.txt;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.ITestDataParserProvider;
import org.robotframework.ide.core.testData.parser.MissingParserException;
import org.robotframework.ide.core.testData.parser.txt.table.TxtKeywordTableParser;
import org.robotframework.ide.core.testData.parser.txt.table.TxtSettingTableParser;
import org.robotframework.ide.core.testData.parser.txt.table.TxtTestCaseTableParser;
import org.robotframework.ide.core.testData.parser.txt.table.TxtVariablesTableParser;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * Tests for trash data.
 * 
 * @author wypych
 * @see TxtRobotFrameworkParser#parse(org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream)
 */
public class TestTxtParsingCombinationWithNoRobotDomainData {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> parser;


    @Test
    public void test_oneSpace_andAfter_tableSectionBegin_butNoParserAvailableForIt() {
        // prepare
        TxtSettingTableParser settingTableParser = mock(TxtSettingTableParser.class);
        TxtVariablesTableParser variablesTableParser = mock(TxtVariablesTableParser.class);
        TxtTestCaseTableParser testCaseTableParser = mock(TxtTestCaseTableParser.class);
        TxtKeywordTableParser keywordTableParser = mock(TxtKeywordTableParser.class);
        @SuppressWarnings("unchecked")
        ITestDataParserProvider<ByteBufferInputStream> txtParserProvider = mock(ITestDataParserProvider.class);
        when(txtParserProvider.getSettingsTableParser()).thenReturn(
                settingTableParser);
        when(txtParserProvider.getVariablesTableParser()).thenReturn(
                variablesTableParser);
        when(txtParserProvider.getTestCasesTableParser()).thenReturn(
                testCaseTableParser);
        when(txtParserProvider.getKeywordsTableParser()).thenReturn(
                keywordTableParser);

        // give command that no exist parser for current data
        when(settingTableParser.canParse(any(ByteBufferInputStream.class)))
                .thenReturn(false);
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
