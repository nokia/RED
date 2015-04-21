package org.robotframework.ide.core.testData.parser;

import org.robotframework.ide.core.testData.parser.result.MessageType;
import org.robotframework.ide.core.testData.parser.result.OutOfOrderData;
import org.robotframework.ide.core.testData.parser.result.ParseProcessResult;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.result.ParserMessage;


/**
 * Helper builder for create {@code ParseResult} object.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            accepted by parser input format i.e. bytes
 * @param <OutputFormatType>
 *            produced by parser element of model
 */
public class ParserResultBuilder<InputFormatType, OutputFormatType> {

    private final ParseResult<InputFormatType, OutputFormatType> parseResultBuildUp;
    private boolean wasWarn = false;
    private boolean wasError = false;
    private ParseProcessResult userStatus;


    public ParserResultBuilder() {
        parseResultBuildUp = new ParseResult<InputFormatType, OutputFormatType>();
    }


    /**
     * @param dataUsed
     *            eat data
     * @return current view on builder
     */
    public ParserResultBuilder<InputFormatType, OutputFormatType> addDataConsumed(
            InputFormatType dataUsed) {
        parseResultBuildUp.setDataConsumed(dataUsed);
        return this;
    }


    /**
     * @param producedModelElement
     *            what we achieved after eat data
     * @return current view on builder
     */
    public ParserResultBuilder<InputFormatType, OutputFormatType> addProducedModelElement(
            OutputFormatType producedModelElement) {
        parseResultBuildUp.setProducedModelElement(producedModelElement);
        return this;
    }


    /**
     * @param localization
     *            where the info event occurred
     * @param message
     *            parser information
     * @return current view on builder
     */
    public ParserResultBuilder<InputFormatType, OutputFormatType> addInformationMessage(
            String localization, String message) {
        parseResultBuildUp.addParserMessage(new ParserMessage(MessageType.INFO,
                localization, message));
        return this;
    }


    /**
     * @param localization
     *            where the warning event occurred
     * @param message
     *            parser warning
     * @return current view on builder
     */
    public ParserResultBuilder<InputFormatType, OutputFormatType> addWarningMessage(
            String localization, String message) {
        parseResultBuildUp.addParserMessage(new ParserMessage(MessageType.WARN,
                localization, message));
        this.wasWarn = true;

        return this;
    }


    /**
     * @param localization
     *            where the error event occurred
     * @param message
     *            parser error
     * @return current view on builder
     */
    public ParserResultBuilder<InputFormatType, OutputFormatType> addErrorMessage(
            String localization, String message) {
        parseResultBuildUp.addParserMessage(new ParserMessage(
                MessageType.ERROR, localization, message));
        this.wasError = true;

        return this;
    }


    /**
     * 
     * @param garbageData
     *            data without non-sens for parser
     * @return current view on builder
     */
    public ParserResultBuilder<InputFormatType, OutputFormatType> addTrashDataFound(
            InputFormatType garbageData) {
        parseResultBuildUp.addNextTrashData(garbageData);

        return this;
    }


    /**
     * @param data
     *            which could be possible process by parser, but from some
     *            reason they are treat as unexpected here
     * @return current view on builder
     */
    public ParserResultBuilder<InputFormatType, OutputFormatType> addOutOfOrderElement(
            InputFormatType data, String localization, String message) {
        parseResultBuildUp
                .addOutOfOrderElementFound(new OutOfOrderData<InputFormatType>(
                        data, localization, message));

        return this;
    }


    /**
     * @param userStatus
     *            Explicitly set judgment about parsing
     * @return
     */
    public ParserResultBuilder<InputFormatType, OutputFormatType> addParsingStatus(
            ParseProcessResult userStatus) {
        this.userStatus = userStatus;
        return this;
    }


    /**
     * Performs final computation on build object base on warnings and errors
     * list plus with check if {@link ParseResult#getProducedModelElement()} not
     * returns {@code null}
     * 
     * @return parsing result
     */
    public ParseResult<InputFormatType, OutputFormatType> build() {
        parseResultBuildUp.setResult(compute());
        return parseResultBuildUp;
    }


    /**
     * Main function for judgment about parsing result check base on
     * {@link ParseResult#getProducedModelElement()} is not null and
     * {@link ParseResult#getParserMessages()} doesn't contain {@code WARNINGs}
     * or {@code ERRORs}
     * 
     * @return
     * 
     */
    private ParseProcessResult compute() {
        ParseProcessResult judgmentAboutProcess;
        if (userStatus != null) {
            judgmentAboutProcess = userStatus;
        } else {
            if (wasError
                    || parseResultBuildUp.getProducedModelElement() == null) {
                judgmentAboutProcess = ParseProcessResult.FAILED;
            } else if (wasWarn) {
                judgmentAboutProcess = ParseProcessResult.PARTIAL_SUCCESS;
            } else {
                judgmentAboutProcess = ParseProcessResult.PARSED_WITH_SUCCESS;
            }
        }

        return judgmentAboutProcess;
    }
}
