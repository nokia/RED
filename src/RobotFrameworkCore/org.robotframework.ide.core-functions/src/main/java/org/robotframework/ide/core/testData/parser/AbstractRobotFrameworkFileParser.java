package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.parser.result.ParseResult;


/**
 * Parser for single Robot Framework file.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            format of input data i.e. byte buffer or xml
 * @see IParsePositionMarkable
 */
public abstract class AbstractRobotFrameworkFileParser<InputFormatType extends IParsePositionMarkable> {

    protected final ITestDataParserProvider<InputFormatType> parsersProvider;


    public AbstractRobotFrameworkFileParser(
            final ITestDataParserProvider<InputFormatType> parsersProvider)
            throws MissingParserException {
        checkParsersProvider(parsersProvider);
        this.parsersProvider = parsersProvider;
    }


    private void checkParsersProvider(
            final ITestDataParserProvider<InputFormatType> parsersProvider)
            throws MissingParserException {

    }


    public abstract ParseResult<InputFormatType, TestDataFile> parse(
            InputFormatType testData);
}
