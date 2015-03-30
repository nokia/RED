package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.model.table.SettingTable;


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
    ITestDataElementParser<InputFormatType, SettingTable> getSettingsParser();
}
