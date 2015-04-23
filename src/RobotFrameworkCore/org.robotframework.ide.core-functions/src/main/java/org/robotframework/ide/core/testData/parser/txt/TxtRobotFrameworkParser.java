package org.robotframework.ide.core.testData.parser.txt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
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
    private static final List<Character> TABLE_BEGINS = Arrays.asList('|', '*');


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

        int position = 0;
        while(testData.available() > 0) {
            position = testData.getByteBuffer().position();
            ByteArrayOutputStream trashData = consumeUntilTableMarkWillBeFind(testData);

            if (trashData.size() > 0) {
                ByteBufferInputStream garbageData = new ByteBufferInputStream(
                        ByteBuffer.wrap(trashData.toByteArray()));
                ByteLocator location = new ByteLocator(testData, position,
                        trashData.size());
                parseResultBuilder.addTrashDataFound(garbageData, location);
            } else {
                // in case will be not table data eat as much as it can
            }
        }

        return parseResultBuilder.addDataConsumed(testData)
                .addProducedModelElement(testDataFile).build();
    }


    private ByteArrayOutputStream consumeUntilTableMarkWillBeFind(
            ByteBufferInputStream data) {
        ByteArrayOutputStream collectedTrashData = new ByteArrayOutputStream();
        while(data.available() > 0) {
            int currentByte = data.currentByteInBuffer();
            if (!TABLE_BEGINS.contains(currentByte)) {
                collectedTrashData.write(data.read());
            } else {
                break;
            }
        }

        return collectedTrashData;
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
