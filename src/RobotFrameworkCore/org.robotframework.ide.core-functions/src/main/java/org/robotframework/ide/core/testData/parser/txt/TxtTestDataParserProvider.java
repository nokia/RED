package org.robotframework.ide.core.testData.parser.txt;

import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;
import org.robotframework.ide.core.testData.parser.ITestDataElementParser;
import org.robotframework.ide.core.testData.parser.ITestDataParserProvider;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * Holder of Textual format parsers for all tables available inside Robot
 * Framework Test Data files.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class TxtTestDataParserProvider implements
        ITestDataParserProvider<ByteBufferInputStream> {

    @Override
    public ITestDataElementParser<ByteBufferInputStream, SettingTable> getSettingsTableParser() {
        return new TxtSettingTableParser();
    }


    @Override
    public ITestDataElementParser<ByteBufferInputStream, TestCaseTable> getTestCasesTableParser() {
        return new TxtTestCaseTableParser();
    }


    @Override
    public ITestDataElementParser<ByteBufferInputStream, VariablesTable> getVariablesTableParser() {
        return new TxtVariablesTableParser();
    }


    @Override
    public ITestDataElementParser<ByteBufferInputStream, KeywordTable> getKeywordsTableParser() {
        return new TxtKeywordsTableParser();
    }
}
