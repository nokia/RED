package org.robotframework.ide.core.testData.parser.txt.table;

import org.robotframework.ide.core.testData.model.table.VariablesTable;
import org.robotframework.ide.core.testData.parser.ITestDataElementParser;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class TxtVariablesTableParser implements
        ITestDataElementParser<ByteBufferInputStream, VariablesTable> {

    @Override
    public boolean canParse(ByteBufferInputStream testData) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public ParseResult<ByteBufferInputStream, VariablesTable> parse(
            ByteBufferInputStream testData) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getName() {
        return VariablesTable.TABLE_NAME;
    }
}
