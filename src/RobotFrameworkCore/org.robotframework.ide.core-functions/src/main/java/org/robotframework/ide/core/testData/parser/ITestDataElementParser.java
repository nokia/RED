package org.robotframework.ide.core.testData.parser;

/**
 * 
 * @author wypych
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
