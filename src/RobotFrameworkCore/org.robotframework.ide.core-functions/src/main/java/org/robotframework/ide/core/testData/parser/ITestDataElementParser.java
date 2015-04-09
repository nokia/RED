package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.parser.result.ParseResult;


/**
 * Responsible for decoding simple element - i.e. LibraryReference. It shouldn't
 * be used for decoding whole file - for this please check
 * {@link ITestDataParserProvider}.
 * 
 * NOTE: Invoke {@link #canParse(IParsePositionMarkable)} before real parse
 * method will be invoked.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            type of data i.e. bytes, xml elements, but it should give
 *            possibility to restore position in case parsing fails
 * @param <OutputElementType>
 *            produced output i.e. LibraryReference
 */
public interface ITestDataElementParser<InputFormatType extends IParsePositionMarkable, OutputElementType> {

    /**
     * @param testData
     *            input data get by parser
     * @return an information if current data could be parsed by this parser
     */
    boolean canParse(InputFormatType testData);


    /**
     * @param testData
     *            current data to use
     * @return result of parsing, method should not throws any kind of exception
     */
    ParseResult<InputFormatType, OutputElementType> parse(
            InputFormatType testData);
}
