package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.parser.result.ParseResult;


/**
 * Responsible for decoding simple element - i.e. Setting table. It shouldn't be
 * used for decoding whole file - for this please check
 * {@link ITestDataParserProvider} .
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            type of data i.e. bytes, xml elements, but it should give
 *            possibility to restore position in case parsing fails
 * @param <OutputElementType>
 *            produced output i.e. SettingTable
 */
public interface ITestDataElementParser<InputFormatType extends IParsePositionMarkable, OutputElementType> {

    /**
     * @param testData
     *            current data to use
     * @return result of parsing, method should not throws any kind of exception
     */
    ParseResult<InputFormatType, OutputElementType> parse(
            InputFormatType testData);
}
