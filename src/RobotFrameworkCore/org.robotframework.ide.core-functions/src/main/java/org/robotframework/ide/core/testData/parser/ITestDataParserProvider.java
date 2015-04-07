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
    ITestDataElementParser<InputFormatType, SettingTable> getSettingsTableParser();


    /**
     * @return parser for Test Case table
     */
    ITestDataElementParser<InputFormatType, TestCaseTable> getTestCasesTableParser();


    /**
     * @return parser for Variables table
     */
    ITestDataElementParser<InputFormatType, VariablesTable> getVariablesTableParser();


    /**
     * @return parser for Keyword table
     */
    ITestDataElementParser<InputFormatType, KeywordTable> getKeywordsTableParser();
}
