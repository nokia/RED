package org.robotframework.ide.core.testData.parser.txt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.model.table.IRobotSectionTable;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.ITestDataElementParser;
import org.robotframework.ide.core.testData.parser.MissingParserException;
import org.robotframework.ide.core.testData.parser.ParserResultBuilder;
import org.robotframework.ide.core.testData.parser.result.ParseProcessResult;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.result.TrashData;
import org.robotframework.ide.core.testData.parser.txt.table.TxtKeywordTableParser;
import org.robotframework.ide.core.testData.parser.txt.table.TxtSettingTableParser;
import org.robotframework.ide.core.testData.parser.txt.table.TxtTestCaseTableParser;
import org.robotframework.ide.core.testData.parser.txt.table.TxtVariablesTableParser;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see TxtRobotFrameworkParser#parse(org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream)
 */
public class TestTxtRobotParserOnlyIncorrectDataCase {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> txtParser;


    @Test
    // (timeout = 10000)
    public void test_fileContainsTrash_nextTable_andThenAgainTrash()
            throws UnsupportedEncodingException, IllegalArgumentException,
            MissingParserException {
        // prepare - data
        String fileHeaderTrash = "| *foobar\n";
        String fileSettingTable = "| *** Settings *** |\n";
        String fileTrailerTrash = "trash trash";
        String fileContent = fileHeaderTrash + fileSettingTable
                + fileTrailerTrash;
        byte[] bytes = fileContent.getBytes("UTF-8");
        ByteBufferInputStream testData = new ByteBufferInputStream(
                ByteBuffer.wrap(bytes));

        // prepare - parser
        TxtTestDataParserProvider txtParserProvider = createMockedTxtParserProvider();
        txtParser = new TxtRobotFrameworkParser(txtParserProvider);
        when(txtParserProvider.getSettingsTableParser().canParse(testData))
                .thenReturn(false).thenReturn(true).thenReturn(false);
        ConsumeBytesAnswer<SettingTable> settingTableConsumer = new ConsumeBytesAnswer<SettingTable>(
                fileHeaderTrash.length(), fileSettingTable.length());
        when(txtParserProvider.getSettingsTableParser().parse(testData)).then(
                settingTableConsumer);

        // execute
        ParseResult<ByteBufferInputStream, TestDataFile> parseResult = txtParser
                .parse(testData);

        // verify
        assertThat(parseResult).isNotNull();
        assertThat(parseResult.getResult()).isEqualTo(
                ParseProcessResult.PARSED_WITH_SUCCESS);
        assertThat(settingTableConsumer.meet).isTrue();
        assertThat(parseResult.getDataConsumed()).isEqualTo(testData);
        assertThat(parseResult.getElementLocation()).isNull();
        assertThat(parseResult.getParserMessages()).isEmpty();
        assertThat(parseResult.getProducedModelElement()).isEqualTo(
                new TestDataFile());
        assertThat(parseResult.getTrashData()).hasSize(2);

        TrashData<ByteBufferInputStream> header = parseResult.getTrashData()
                .get(0);

        // header trash
        int headerBegin = 0;
        int headerEnd = fileHeaderTrash.length();
        String headerContent = fileHeaderTrash;
        byte[] expectedContent = bytes;
        TrashData<ByteBufferInputStream> headerTrash = header;
        assertThatTrashIsAsExpected(headerBegin, headerEnd, headerContent,
                expectedContent, headerTrash);

        TrashData<ByteBufferInputStream> trailer = parseResult.getTrashData()
                .get(1);

        // trailer trash
        int trailerBegin = headerEnd + fileSettingTable.length();
        int trailerEnd = trailerBegin + fileTrailerTrash.length();
        String trashContent = fileTrailerTrash;
        TrashData<ByteBufferInputStream> trailerTrash = trailer;
        assertThatTrashIsAsExpected(trailerBegin, trailerEnd, trashContent,
                expectedContent, trailerTrash);
    }


    private TxtTestDataParserProvider createMockedTxtParserProvider() {
        TxtTestDataParserProvider txtParserProvider = spy(new TxtTestDataParserProvider());
        ITestDataElementParser<ByteBufferInputStream, KeywordTable> keywordTableParser = mock(TxtKeywordTableParser.class);
        when(txtParserProvider.getKeywordsTableParser()).thenReturn(
                keywordTableParser);
        when(keywordTableParser.canParse(any(ByteBufferInputStream.class)))
                .thenReturn(false);

        ITestDataElementParser<ByteBufferInputStream, SettingTable> settingTableParser = mock(TxtSettingTableParser.class);
        when(txtParserProvider.getSettingsTableParser()).thenReturn(
                settingTableParser);
        when(settingTableParser.canParse(any(ByteBufferInputStream.class)))
                .thenReturn(false);

        ITestDataElementParser<ByteBufferInputStream, TestCaseTable> testCaseTableParser = mock(TxtTestCaseTableParser.class);
        when(txtParserProvider.getTestCasesTableParser()).thenReturn(
                testCaseTableParser);
        when(testCaseTableParser.canParse(any(ByteBufferInputStream.class)))
                .thenReturn(false);

        ITestDataElementParser<ByteBufferInputStream, VariablesTable> variablesTableParser = mock(TxtVariablesTableParser.class);
        when(txtParserProvider.getVariablesTableParser()).thenReturn(
                variablesTableParser);
        when(variablesTableParser.canParse(any(ByteBufferInputStream.class)))
                .thenReturn(false);

        return txtParserProvider;
    }

    /**
     * Simulating eating of bytes in case buffer is on expected place
     * 
     * @author wypych
     * 
     * @param <Table>
     */
    private static class ConsumeBytesAnswer<Table extends IRobotSectionTable>
            implements Answer<ParseResult<ByteBufferInputStream, Table>> {

        private final int startExpected;
        private final int numBytesToEat;
        private boolean meet;


        public ConsumeBytesAnswer(final int startExpected,
                final int numOfBytesToEat) {
            this.startExpected = startExpected;
            this.numBytesToEat = numOfBytesToEat;
        }


        @Override
        public ParseResult<ByteBufferInputStream, Table> answer(
                InvocationOnMock invocation) throws Throwable {
            ParserResultBuilder<ByteBufferInputStream, Table> parserResultsBuilder = new ParserResultBuilder<ByteBufferInputStream, Table>();
            if (invocation.getArguments().length == 1) {
                ByteBufferInputStream byteStream = invocation.getArgumentAt(0,
                        ByteBufferInputStream.class);
                int currentBufferPosition = byteStream.getByteBuffer()
                        .position();
                if (currentBufferPosition == startExpected) {
                    meet = true;
                    parserResultsBuilder
                            .addParsingStatus(ParseProcessResult.PARSED_WITH_SUCCESS);
                    byteStream.getByteBuffer().position(
                            startExpected + numBytesToEat);
                } else {
                    meet = false;
                    parserResultsBuilder
                            .addParsingStatus(ParseProcessResult.PARTIAL_SUCCESS);
                    throw new IllegalArgumentException(
                            "Expected position in buffer: " + startExpected
                                    + ", but was " + currentBufferPosition);
                }
            } else {
                meet = false;
                parserResultsBuilder
                        .addParsingStatus(ParseProcessResult.FAILED);
            }

            return parserResultsBuilder.build();
        }
    }


    @Test(timeout = 10000)
    public void test_fileWithTrashAtBegin_tableSignPIPE_and_SPACE_andAsterisk_oneTime()
            throws UnsupportedEncodingException {
        // prepare
        String fileBeginTrash = "  \n\r\n";
        String fileContentWithTable = "| *foobar\n";
        String fileContent = fileBeginTrash + fileContentWithTable;

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
        assertThat(result.getTrashData()).hasSize(2);

        TrashData<ByteBufferInputStream> trashData_firstBegin = result
                .getTrashData().get(0);

        // the first trash
        int trashBegin = 0;
        int trashEnd = fileBeginTrash.length();
        String trashContent = fileBeginTrash;
        byte[] expectedContent = bytes;
        TrashData<ByteBufferInputStream> trashData = trashData_firstBegin;
        assertThatTrashIsAsExpected(trashBegin, trashEnd, trashContent,
                expectedContent, trashData);

        // second trash
        trashBegin = fileBeginTrash.length();
        trashEnd = fileContent.length();
        trashContent = fileContentWithTable;
        trashData = result.getTrashData().get(1);
        assertThatTrashIsAsExpected(trashBegin, trashEnd, trashContent,
                expectedContent, trashData);
    }


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

        int trashBegin = 0;
        int trashEnd = fileContent.length();
        String trashContent = fileContent;
        byte[] expectedContent = bytes;
        TrashData<ByteBufferInputStream> trashData = result.getTrashData().get(
                0);

        assertThatTrashIsAsExpected(trashBegin, trashEnd, trashContent,
                expectedContent, trashData);
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

        int trashBegin = 0;
        int trashEnd = fileContent.length();
        String trashContent = fileContent;
        byte[] expectedContent = bytes;
        TrashData<ByteBufferInputStream> trashData = result.getTrashData().get(
                0);

        assertThatTrashIsAsExpected(trashBegin, trashEnd, trashContent,
                expectedContent, trashData);
    }


    private void assertThatTrashIsAsExpected(int trashBegin, int trashEnd,
            String trashContent, byte[] expectedContent,
            TrashData<ByteBufferInputStream> trashData)
            throws UnsupportedEncodingException {
        assertThat(trashData.getTrash()).isNotNull();
        ByteBuffer trashBuffer = trashData.getTrash().getByteBuffer();
        assertThat(trashBuffer).isNotNull();
        assertThat(trashBuffer.array()).isEqualTo(
                trashContent.getBytes("UTF-8"));
        assertThat(trashData.getLocation()).isNotNull();
        ByteLocator locator = (ByteLocator) trashData.getLocation();
        assertThat(locator.getData()).isNotNull();
        ByteBufferInputStream locatorData = locator.getData();
        assertThat(locatorData.getByteBuffer().array()).isEqualTo(
                expectedContent);
        locator.moveToStart();
        assertThat(locator.getData().getByteBuffer().position()).isEqualTo(
                trashBegin);
        locator.moveToEnd();
        assertThat(locator.getData().getByteBuffer().position()).isEqualTo(
                trashEnd);
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
