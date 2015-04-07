package org.robotframework.ide.core.testData.parser.txt;

import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.parser.ITestDataElementParser;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


public class TxtSettingTableParser implements
        ITestDataElementParser<ByteBufferInputStream, SettingTable> {

    @Override
    public ParseResult<ByteBufferInputStream, SettingTable> parse(
            ByteBufferInputStream testData) {
        // TODO: everything

        return null;
    }
}
