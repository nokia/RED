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
 *            type of data i.e. bytes, xml elements
 * @param <OutputElementType>
 *            produced output i.e. SettingTable
 */
public interface ITestDataElementParser<InputFormatType, OutputElementType> {

    /**
     * should be called before {@link #parse(Object)}, gives simple information
     * about possibility of parsing this element
     * 
     * @param testData
     *            current data to decode
     * @return an information if we are able to parse
     */
    boolean canParse(InputFormatType testData);


    /**
     * call {@link #canParse(Object)} if you are not ensure if declaration
     * belongs to this element
     * 
     * @param testData
     *            current data to use
     * @return result of parsing, method should not throws any kind of exception
     */
    ParseResult<InputFormatType, OutputElementType> parse(
            InputFormatType testData);
}
