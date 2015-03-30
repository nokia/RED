package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.model.table.SettingTable;


/**
 * 
 * @author wypych
 * 
 * @param <InputFormatType>
 */
public interface ITestDataParserProvider<InputFormatType> {

    /**
     * @return
     */
    ITestDataElementParser<InputFormatType, SettingTable> getSettingsParser();
}
