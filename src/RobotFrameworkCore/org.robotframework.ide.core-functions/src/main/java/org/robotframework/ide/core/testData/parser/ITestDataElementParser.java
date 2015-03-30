package org.robotframework.ide.core.testData.parser;

public interface ITestDataElementParser<InputFormatType, OutputElementType> {

    boolean canDecode(InputFormatType testData);


    ParseResult<InputFormatType, OutputElementType> decode(
            InputFormatType testData);
}
