package org.robotframework.ide.core.testData.parser.txt;

import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.model.table.IRobotSectionTable;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.ITestDataParserProvider;
import org.robotframework.ide.core.testData.parser.MissingParserException;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * Robot Framework test data files parser: __init__.txt, ${file_name}.txt mostly
 * resources, test cases and test suites in TXT format.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @see ByteBufferInputStream
 * @see IRobotSectionTable
 * @see KeywordTable
 * @see SettingTable
 * @see TestCaseTable
 * @see VariablesTable
 */
public class TxtRobotFrameworkFileParser extends
        AbstractRobotFrameworkFileParser<ByteBufferInputStream> {

    /**
     * @param parsersProvider
     *            providers of parsers of table for current input format
     * @throws MissingParserException
     *             in case at least one of table parser is not declared
     * @throws IllegalArgumentException
     *             when parsers provider is null
     */
    public TxtRobotFrameworkFileParser(
            ITestDataParserProvider<ByteBufferInputStream> parsersProvider)
            throws MissingParserException, IllegalArgumentException {
        super(parsersProvider);
    }


    @Override
    public ParseResult<ByteBufferInputStream, TestDataFile> parse(
            ByteBufferInputStream testData) {
        return null;
    }
}
