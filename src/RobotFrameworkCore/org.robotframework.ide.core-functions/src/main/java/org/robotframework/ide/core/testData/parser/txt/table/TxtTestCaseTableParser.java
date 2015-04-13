package org.robotframework.ide.core.testData.parser.txt.table;

import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.parser.ITestDataElementParser;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


public class TxtTestCaseTableParser implements
        ITestDataElementParser<ByteBufferInputStream, TestCaseTable> {

    @Override
    public boolean canParse(ByteBufferInputStream testData) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public ParseResult<ByteBufferInputStream, TestCaseTable> parse(
            ByteBufferInputStream testData) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getName() {
        return TestCaseTable.TABLE_NAME;
    }
}
