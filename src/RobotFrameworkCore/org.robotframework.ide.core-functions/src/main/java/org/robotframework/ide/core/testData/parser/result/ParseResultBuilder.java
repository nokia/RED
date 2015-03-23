package org.robotframework.ide.core.testData.parser.result;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.parser.result.ParseResult.ParseResultMessage;
import org.robotframework.ide.core.testData.parser.result.ParseResult.ParseResultMessage.ParseResultMessageType;


/**
 * Helper builder for coherent with common logic, build of parsing status.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * @see ParseResult
 */
public class ParseResultBuilder<InputFormatType, OutputModelElementType> {

    private boolean explicitySetByUser;
    private boolean wasAnyError;
    private boolean wasAnyWarn;
    private ParseStatus status;

    private InputFormatType originalDataUsedForBuild;

    private OutputModelElementType producedElementModel;

    private final List<ParseResultMessage> parserMessages = new LinkedList<ParseResultMessage>();


    public ParseResultBuilder() {
    }


    public ParseResultBuilder<InputFormatType, OutputModelElementType> addExplicityStatus(
            ParseStatus status) {
        this.status = status;
        this.explicitySetByUser = true;

        return this;
    }


    public ParseResultBuilder<InputFormatType, OutputModelElementType> addProducedModelElement(
            OutputModelElementType modelElement) {
        this.producedElementModel = modelElement;
        return this;
    }


    public ParseResultBuilder<InputFormatType, OutputModelElementType> addParsedData(
            InputFormatType testData) {
        this.originalDataUsedForBuild = testData;
        return this;
    }


    public ParseResultBuilder<InputFormatType, OutputModelElementType> addInformationMessage(
            String localization, String message) {
        this.parserMessages.add(new ParseResultMessage(
                ParseResultMessageType.INFO, message, localization));
        return this;
    }


    public ParseResultBuilder<InputFormatType, OutputModelElementType> addWarningMessage(
            String localization, String message) {
        this.parserMessages.add(new ParseResultMessage(
                ParseResultMessageType.WARN, message, localization));
        this.wasAnyWarn = true;
        return this;
    }


    public ParseResultBuilder<InputFormatType, OutputModelElementType> addErrorMessage(
            String localization, String message) {
        this.parserMessages.add(new ParseResultMessage(
                ParseResultMessageType.ERROR, message, localization));
        this.wasAnyError = true;
        return this;
    }


    public ParseResult<InputFormatType, OutputModelElementType> build() {
        ParseResult<InputFormatType, OutputModelElementType> builded = new ParseResult<InputFormatType, OutputModelElementType>(
                computeStatus(), originalDataUsedForBuild, producedElementModel);
        builded.addAllParsedMessages(parserMessages);

        return builded;
    }


    private ParseStatus computeStatus() {
        ParseStatus computeStatus;
        if (explicitySetByUser) {
            computeStatus = this.status;
        } else {
            if (wasAnyError || this.producedElementModel == null) {
                computeStatus = ParseStatus.UNSUCESSFULY_PARSED;
            } else {
                if (wasAnyWarn) {
                    computeStatus = ParseStatus.PARTIAL_PARSED;
                } else {
                    computeStatus = ParseStatus.SUCCESSFULY_PARSED;
                }
            }
        }

        return computeStatus;
    }
}
