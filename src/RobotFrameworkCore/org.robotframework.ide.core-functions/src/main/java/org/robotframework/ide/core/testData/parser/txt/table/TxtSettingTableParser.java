package org.robotframework.ide.core.testData.parser.txt.table;

import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.parser.ITestDataElementParser;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.txt.table.TxtTableHeaderHelper.TableHeader;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class TxtSettingTableParser implements
        ITestDataElementParser<ByteBufferInputStream, SettingTable> {

    @Override
    public boolean canParse(ByteBufferInputStream testData) {
        TxtTableHeaderHelper tableHelper = new TxtTableHeaderHelper();

        return tableHelper.createTableHeader("setting", "s", testData)
                .computeFinalResult();
    }


    @Override
    public ParseResult<ByteBufferInputStream, SettingTable> parse(
            ByteBufferInputStream testData) {
        TxtTableHeaderHelper tableHelper = new TxtTableHeaderHelper();

        TableHeader createTableHeader = tableHelper.createTableHeader(
                "setting", "s", testData);
        return null;
    }


    @Override
    public String getName() {
        return SettingTable.TABLE_NAME;
    }
}
