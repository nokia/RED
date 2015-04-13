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
        if (hasRemainingData(testData)) {
            int previousTableFoundPosition = 0;
            while(hasRemainingData(testData)) {
                if (findTableSectionDeclaration(testData)) {
                    ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> tablePraser = findMatchTableSectionParser(testData);
                    if (tablePraser != null) {
                        if (isTableSectionStartsFromNewLine(testData)) {
                            ParseResult<ByteBufferInputStream, ? extends IRobotSectionTable> createdRobotFrameworkElement = tablePraser
                                    .parse(testData);
                            // TODO: checks of object
                        } else {
                            if (previousTableFoundPosition == 0) {

                            }
                            addWarningAboutIncorrectTablePlace(testData,
                                    outputBuilder, tablePraser);
                        }
                    } else {
                        addInformationAboutTableMarkFound(testData,
                                outputBuilder);
                    }
                }
            }
        } else {
            addInformationAboutEmptyFile(testData, outputBuilder, currentFile);
        }

        return outputBuilder.build();
    }


    private boolean hasRemainingData(ByteBufferInputStream testData) {
        return testData.available() > 0;
    }


    private void addInformationAboutEmptyFile(
            ByteBufferInputStream testData,
            ParserResultBuilder<ByteBufferInputStream, TestDataFile> outputBuilder,
            TestDataFile currentFile) {
        outputBuilder.addDataConsumed(testData)
                .addProducedModelElement(currentFile)
                .addInformationMessage("begin", "empty file");
    }


    private void addInformationAboutTableMarkFound(
            ByteBufferInputStream testData,
            ParserResultBuilder<ByteBufferInputStream, TestDataFile> outputBuilder) {
        outputBuilder.addInformationMessage("Position in buffer: "
                + testData.getByteBuffer().position(),
                "Found possible '*', which indicate begin of table, but "
                        + " non of parsers for section was interested");
    }


    private void addWarningAboutIncorrectTablePlace(
            ByteBufferInputStream testData,
            ParserResultBuilder<ByteBufferInputStream, TestDataFile> outputBuilder,
            ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> tablePraser) {
        outputBuilder.addWarningMessage("Position in buffer: "
                + testData.getByteBuffer().position(), "Found table "
                + tablePraser.getName() + ", but it was not in correct place. "
                + "According to Robot Framework specification "
                + "tables should starts from begin of line/file.");
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

        while(hasRemainingData(fileContent)) {
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
