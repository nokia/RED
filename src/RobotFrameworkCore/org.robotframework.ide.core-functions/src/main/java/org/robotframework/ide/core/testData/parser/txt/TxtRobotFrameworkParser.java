package org.robotframework.ide.core.testData.parser.txt;

import java.io.File;
import java.nio.ByteBuffer;

import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.model.table.IRobotSectionTable;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.ITestDataElementParser;
import org.robotframework.ide.core.testData.parser.ITestDataParserProvider;
import org.robotframework.ide.core.testData.parser.MissingParserException;
import org.robotframework.ide.core.testData.parser.ParserResultBuilder;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


public class TxtRobotFrameworkParser extends
        AbstractRobotFrameworkFileParser<ByteBufferInputStream> {

    private static final String FILE_EXTENSION = ".txt";


    public TxtRobotFrameworkParser(
            ITestDataParserProvider<ByteBufferInputStream> parsersProvider)
            throws MissingParserException, IllegalArgumentException {
        super(parsersProvider);
    }


    @Override
    public ParseResult<ByteBufferInputStream, TestDataFile> parse(
            ByteBufferInputStream testData) {
        ParserResultBuilder<ByteBufferInputStream, TestDataFile> outputBuilder = new ParserResultBuilder<ByteBufferInputStream, TestDataFile>();

        TestDataFile currentFile = new TestDataFile();
        if (testData.available() > 0) {
            while(testData.available() > 0) {
                if (findTableSectionDeclaration(testData)) {
                    ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> tablePraser = findMatchTableSectionParser(testData);
                    // TODO: check if is correct place means starting position
                    // new line etc.
                    if (tablePraser != null) {
                        if (isTableSectionStartsFromNewLine(testData)) {
                            ParseResult<ByteBufferInputStream, ? extends IRobotSectionTable> createdRobotFrameworkElement = tablePraser
                                    .parse(testData);
                        } else {

                        }
                    } else {
                        outputBuilder
                                .addInformationMessage(
                                        "Position in buffer: "
                                                + testData.getByteBuffer()
                                                        .position(),
                                        "Found possible '*', which indicate begin of table, but "
                                                + " non of parsers for section was interested");
                    }
                }
            }
        } else {
            outputBuilder.addDataConsumed(testData)
                    .addProducedModelElement(currentFile)
                    .addInformationMessage("begin", "empty file");
        }

        return outputBuilder.build();
    }


    /**
     * Check if is correct place means starting position for table is new line
     * or begin of file
     * 
     * @param content
     *            current test data file data
     * @return says if table could exists here
     */
    private boolean isTableSectionStartsFromNewLine(
            ByteBufferInputStream content) {
        boolean isCorrect = false;

        ByteBuffer byteBuffer = content.getByteBuffer();
        if (byteBuffer.position() == 0) {
            isCorrect = true;
        } else {
            isCorrect = (byteBuffer.get(byteBuffer.position() - 1) == '\n');
        }

        return isCorrect;
    }


    private ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> findMatchTableSectionParser(
            ByteBufferInputStream currentData) {
        ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> foundParser = null;
        currentData.mark();
        if (parsersProvider.getSettingsTableParser().canParse(currentData)) {
            foundParser = parsersProvider.getSettingsTableParser();
        }
        currentData.reset();
        if (foundParser == null
                && parsersProvider.getVariablesTableParser().canParse(
                        currentData)) {
            foundParser = parsersProvider.getVariablesTableParser();
        }
        currentData.reset();
        if (foundParser == null
                && parsersProvider.getKeywordsTableParser().canParse(
                        currentData)) {
            foundParser = parsersProvider.getKeywordsTableParser();
        }
        currentData.reset();
        if (foundParser == null
                && parsersProvider.getTestCasesTableParser().canParse(
                        currentData)) {
            foundParser = parsersProvider.getTestCasesTableParser();
        }
        currentData.reset();

        return foundParser;
    }


    private boolean findTableSectionDeclaration(
            ByteBufferInputStream fileContent) {
        boolean wasFound = false;

        while(fileContent.available() > 0) {
            if (fileContent.currentByteInBuffer() == '*') {
                wasFound = true;
                break;
            } else {
                // move to next position with ignore current char
                fileContent.read();
            }
        }

        return wasFound;
    }


    @Override
    public boolean canAcceptFile(File file) {
        boolean isAcceptable = false;
        if (file.exists() && file.canRead() && file.isFile()) {
            // according to Robot Framework User Guide documentation:

            // "Robot Framework selects a parser for the test data based on the
            // file extension. The extension is case-insensitive, and the
            // recognized extensions are .html, .htm and .xhtml for HTML, .tsv
            // for TSV, .txt and special .robot for plain text, and .rst and
            // .rest for reStructuredText." [chapter 2.1.2 Supported file
            // formats]
            String fileName = file.getName().toLowerCase();
            isAcceptable = (fileName.length() > FILE_EXTENSION.length() && fileName
                    .endsWith(FILE_EXTENSION));
        }

        return isAcceptable;
    }
}
