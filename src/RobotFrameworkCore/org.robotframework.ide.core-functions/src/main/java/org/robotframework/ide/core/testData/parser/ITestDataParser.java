package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.model.KeywordTable;
import org.robotframework.ide.core.testData.model.SettingTable;
import org.robotframework.ide.core.testData.model.TestCaseTable;
import org.robotframework.ide.core.testData.model.VariableTable;


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
public interface ITestDataParser<InputFormatType> {

    /**
     * @return parser for {@code Setting} table in {@code InputFormatType}
     */
    ITestDataElementParser<InputFormatType, SettingTable> getSettingTableParser();


    /**
     * @return parser for {@code Variable} table in {@code InputFormatType}
     */
    ITestDataElementParser<InputFormatType, VariableTable> getVariableTableParser();


    /**
     * @return parser for {@code TestCase} table in {@code InputFormatType}
     */
    ITestDataElementParser<InputFormatType, TestCaseTable> getTestCaseTableParser();


    /**
     * @return parser for {@code Keyword} table in {@code InputFormatType}
     */
    ITestDataElementParser<InputFormatType, KeywordTable> getKeywordTableParser();
}
