package org.robotframework.ide.core.testData.parser;

/**
 * Parser for Table sections in Robot Framework test data formats.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            type of data i.e. bytes, xml elements, but it should give
 *            possibility to restore position in case parsing fails
 * @param <OutputElementType>
 *            produced output - table section i.e. SettingTable
 * @see ITestDataParserProvider
 */
public interface ITableTestDataElementParser<InputFormatType extends IParsePositionMarkable, OutputElementType>
        extends ITestDataElementParser<InputFormatType, OutputElementType> {

    /**
     * @param ignore
     *            give for parser information if stupid data before table begins
     *            should be ignored, otherwise some error could be put as parser
     *            result
     */
    void setIgnoreUnexpectedData(boolean ignore);
}
