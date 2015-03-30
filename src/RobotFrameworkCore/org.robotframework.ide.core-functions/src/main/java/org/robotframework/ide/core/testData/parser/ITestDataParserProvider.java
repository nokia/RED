package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;


/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 */
public interface ITestDataParserProvider<InputFormatType> {

    /**
     * @return
     */
    ITestDataElementParser<InputFormatType, SettingTable> getSettingsTableParser();


    /**
     * @return
     */
    ITestDataElementParser<InputFormatType, TestCaseTable> getTestCasesTableParser();


    /**
     * 
     * @return
     */
    ITestDataElementParser<InputFormatType, VariablesTable> getVariablesTableParser();


    /**
     * 
     * @return
     */
    ITestDataElementParser<InputFormatType, KeywordTable> getKeywordsTableParser();
}
