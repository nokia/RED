package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;


/**
 * Gives all parsers for every Test Data section available in Robot Framework
 * files.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            format of accepted data
 */
public interface ITestDataParserProvider<InputFormatType extends IParsePositionMarkable> {

    /**
     * @return parser for Settings table
     */
    ITableTestDataElementParser<InputFormatType, SettingTable> getSettingsTableParser();


    /**
     * @return parser for Test Case table
     */
    ITableTestDataElementParser<InputFormatType, TestCaseTable> getTestCasesTableParser();


    /**
     * @return parser for Variables table
     */
    ITableTestDataElementParser<InputFormatType, VariablesTable> getVariablesTableParser();


    /**
     * @return parser for Keyword table
     */
    ITableTestDataElementParser<InputFormatType, KeywordTable> getKeywordsTableParser();
}
