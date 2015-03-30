package org.robotframework.ide.core.testData.parser.result;

import java.util.LinkedList;
import java.util.List;


/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 * @param <OutputFormatType>
 */
public class ParseResult<InputFormatType, OutputFormatType> {

    private InputFormatType dataConsumed;
    private OutputFormatType producedModelElement;
    private final ParseProcessResult result = ParseProcessResult.NOT_STARTED;
    private final List<ParserMessage> parserMessages = new LinkedList<ParserMessage>();
}
