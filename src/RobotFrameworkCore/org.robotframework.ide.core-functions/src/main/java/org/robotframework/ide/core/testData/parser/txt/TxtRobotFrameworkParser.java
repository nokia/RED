package org.robotframework.ide.core.testData.parser.txt;

import java.io.ByteArrayOutputStream;
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


/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
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
        ParserResultBuilder<ByteBufferInputStream, TestDataFile> parseResultBuilder = new ParserResultBuilder<ByteBufferInputStream, TestDataFile>();
        TestDataFile testDataFile = new TestDataFile();

        int startPositionByteNr = 0;
        if (testData.available() > 0) {
            while(testData.available() > 0) {
                ByteArrayOutputStream trashData = consumeTrashData(testData);

                if (trashData.size() > 0) {
                    parseResultBuilder.addWarningMessage("byte: "
                            + startPositionByteNr, "Unrecognized data found: '"
                            + new String(trashData.toByteArray()) + "'.");
                    parseResultBuilder
                            .addTrashDataFound(new ByteBufferInputStream(
                                    ByteBuffer.wrap(trashData.toByteArray())));
                } else {
                    // normal processing - table section found
                    ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> tableParser = findParserToUse(testData);

                    if (tableParser != null) {

                    } else {
                        // do sth with data and give possibility to search for
                        // new
                    }
                }

                startPositionByteNr = testData.getByteBuffer().position();
            }

        } else {
            // handling for empty file - just simple information
            parseResultBuilder.addInformationMessage("line: 0, column: 0",
                    "Empty file.");
        }

        return parseResultBuilder.addDataConsumed(testData)
                .addProducedModelElement(testDataFile).build();
    }


    private ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> findParserToUse(
            ByteBufferInputStream testData) {
        ITestDataParserProvider<ByteBufferInputStream> parsersProvider = this.parsersProvider;
        ITestDataElementParser<ByteBufferInputStream, ? extends IRobotSectionTable> tableParser = null;
        testData.mark();
        if (parsersProvider.getSettingsTableParser().canParse(testData)) {
            tableParser = parsersProvider.getSettingsTableParser();
        } else {
            testData.reset();
        }
        if (tableParser == null
                && parsersProvider.getVariablesTableParser().canParse(testData)) {
            tableParser = parsersProvider.getVariablesTableParser();
        } else {
            testData.reset();
        }
        if (tableParser == null
                && parsersProvider.getTestCasesTableParser().canParse(testData)) {
            tableParser = parsersProvider.getTestCasesTableParser();
        } else {
            testData.reset();
        }
        if (tableParser == null
                && parsersProvider.getKeywordsTableParser().canParse(testData)) {
            tableParser = parsersProvider.getKeywordsTableParser();
        }

        testData.reset();

        return tableParser;
    }


    private ByteArrayOutputStream consumeTrashData(
            ByteBufferInputStream testData) {
        ByteArrayOutputStream trashData = new ByteArrayOutputStream();
        while(testData.available() > 0) {
            int currentByteInBuffer = testData.currentByteInBuffer();
            if (currentByteInBuffer != '|' && currentByteInBuffer != '*') {
                trashData.write(testData.read());
            } else {
                break;
            }
        }

        return trashData;
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
