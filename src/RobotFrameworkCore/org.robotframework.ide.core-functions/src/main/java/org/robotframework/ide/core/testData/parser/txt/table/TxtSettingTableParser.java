package org.robotframework.ide.core.testData.parser.txt.table;

import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.parser.ITestDataElementParser;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


public class TxtSettingTableParser implements
        ITestDataElementParser<ByteBufferInputStream, SettingTable> {

    @Override
    public boolean canParse(ByteBufferInputStream testData) {
        boolean shouldParse = false;

        return shouldParse;
    }


    @Override
    public ParseResult<ByteBufferInputStream, SettingTable> parse(
            ByteBufferInputStream testData) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getName() {
        return SettingTable.TABLE_NAME;
    }
}
