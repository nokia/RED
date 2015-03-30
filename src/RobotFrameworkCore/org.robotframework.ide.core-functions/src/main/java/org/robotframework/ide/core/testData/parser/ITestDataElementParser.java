package org.robotframework.ide.core.testData.parser;

/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 * @param <OutputElementType>
 */
public interface ITestDataElementParser<InputFormatType, OutputElementType> {

    /**
     * @param testData
     * @return
     */
    boolean canDecode(InputFormatType testData);


    /**
     * @param testData
     * @return
     */
    ParseResult<InputFormatType, OutputElementType> decode(
            InputFormatType testData);
}
