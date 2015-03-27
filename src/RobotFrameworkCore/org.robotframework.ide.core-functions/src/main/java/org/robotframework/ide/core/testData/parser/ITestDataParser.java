package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.model.KeywordTable;
import org.robotframework.ide.core.testData.model.SettingTable;
import org.robotframework.ide.core.testData.model.TestCaseTable;
import org.robotframework.ide.core.testData.model.VariableTable;


/**
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

    ITestDataElementParser<InputFormatType, SettingTable> getSettingTableParser();


    ITestDataElementParser<InputFormatType, VariableTable> getVariableTableParser();


    ITestDataElementParser<InputFormatType, TestCaseTable> getTestCaseTableParser();


    ITestDataElementParser<InputFormatType, KeywordTable> getKeywordTableParser();
}
