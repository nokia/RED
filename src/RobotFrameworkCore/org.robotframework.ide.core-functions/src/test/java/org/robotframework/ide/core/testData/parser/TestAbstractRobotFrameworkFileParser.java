package org.robotframework.ide.core.testData.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
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
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see AbstractRobotFrameworkFileParser
 */
public class TestAbstractRobotFrameworkFileParser {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> parser;

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
    public void test_settingTableIsNull_shouldGetException_MissingParserException() {
        // prepare
        when(parsersProvider.getSettingsTableParser()).thenReturn(null);
        when(parsersProvider.getKeywordsTableParser()).thenReturn(
                keywordsParser);
        when(parsersProvider.getTestCasesTableParser()).thenReturn(
                testCasesParser);
        when(parsersProvider.getVariablesTableParser()).thenReturn(
                variablesParser);

        // execute & verify
        try {
            parser = new DummyRobotParser(parsersProvider);
            fail("Should thrown: " + MissingParserException.class);
        } catch (MissingParserException mpe) {
            assertThat(mpe.getMessage()).isEqualTo(
                    "Missing table [" + new SettingTable().getName()
                            + "] parser");
        }
    }


    @Test
    public void test_all_parsers_areNull_shouldGetException_MissingParserException() {
        // prepare
        when(parsersProvider.getKeywordsTableParser()).thenReturn(null);
        when(parsersProvider.getSettingsTableParser()).thenReturn(null);
        when(parsersProvider.getTestCasesTableParser()).thenReturn(null);
        when(parsersProvider.getVariablesTableParser()).thenReturn(null);

        // execute & verify
        try {
            parser = new DummyRobotParser(parsersProvider);
            fail("Should thrown: " + MissingParserException.class);
        } catch (MissingParserException mpe) {
            assertThat(mpe.getMessage()).isEqualTo(
                    "Missing tables [" + new SettingTable().getName() + ", "
                            + new KeywordTable().getName() + ", "
                            + new TestCaseTable().getName() + ", "
                            + new VariablesTable().getName() + "] parsers");
        }
    }


    @Test
    public void test_parserObjectPassToConstructor_isNull_shouldThrown_IllegalArgumentException()
            throws MissingParserException {
        // execute & verify
        try {
            parser = new DummyRobotParser(null);
            fail("Should thrown: " + IllegalArgumentException.class);
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage()).isEqualTo("Parsers provider is null.");
        }
    }


    @Test
    public void test_checkLogic_ifAskForEachParser_is_invoked()
            throws IllegalArgumentException, MissingParserException {
        // prepare
        InOrder order = inOrder(parsersProvider);
        when(parsersProvider.getSettingsTableParser()).thenReturn(
                settingsParser);
        when(parsersProvider.getKeywordsTableParser()).thenReturn(
                keywordsParser);
        when(parsersProvider.getTestCasesTableParser()).thenReturn(
                testCasesParser);
        when(parsersProvider.getVariablesTableParser()).thenReturn(
                variablesParser);

        // execute
        parser = new DummyRobotParser(parsersProvider);

        // verify
        order.verify(parsersProvider, times(1)).getSettingsTableParser();
        order.verify(parsersProvider, times(1)).getKeywordsTableParser();
        order.verify(parsersProvider, times(1)).getTestCasesTableParser();
        order.verify(parsersProvider, times(1)).getVariablesTableParser();
        order.verifyNoMoreInteractions();
    }


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @After
    public void tearDown() {
        parser = null;
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
