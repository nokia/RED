package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.parser.table.ITestDataKeywordTableParser;
import org.robotframework.ide.core.testData.parser.table.ITestDataSettingTableParser;
import org.robotframework.ide.core.testData.parser.table.ITestDataTestCaseTableParser;
import org.robotframework.ide.core.testData.parser.table.ITestDataVariableTableParser;


/**
 * Take responsibility for providing parsers to parse test data tables.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            input type i.e. xml doc
 * 
 */
public interface ITestDataParsersProvider<InputFormatType> {

    /**
     * @return parser for {@code Setting} table in {@code InputFormatType}
     */
    ITestDataSettingTableParser<InputFormatType> getSettingTableParser();


    /**
     * @return parser for {@code Variable} table in {@code InputFormatType}
     */
    ITestDataVariableTableParser<InputFormatType> getVariableTableParser();


    /**
     * @return parser for {@code TestCase} table in {@code InputFormatType}
     */
    ITestDataTestCaseTableParser<InputFormatType> getTestCaseTableParser();


    /**
     * @return parser for {@code Keyword} table in {@code InputFormatType}
     */
    ITestDataKeywordTableParser<InputFormatType> getKeywordTableParser();
}
