package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.model.table.SettingTable;

public interface ITestDataParserProvider<InputFormatType> {

    ITestDataElementParser<InputFormatType, SettingTable> getSettingsParser();
}
