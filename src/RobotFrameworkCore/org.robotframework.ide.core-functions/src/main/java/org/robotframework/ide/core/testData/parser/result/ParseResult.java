package org.robotframework.ide.core.testData.parser.result;

import java.util.LinkedList;
import java.util.List;


/**
 * Result of parsing process including data, which was used and produced output
 * with errors and judgment about final state.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            format of data used
 * @param <OutputFormatType>
 *            format of model object built up
 */
public class ParseResult<InputFormatType, OutputFormatType> {

    private final List<InputFormatType> trashData = new LinkedList<InputFormatType>();
    private List<OutOfOrderData<InputFormatType>> outOfOrderOrConstrainsPossibleElements = new LinkedList<OutOfOrderData<InputFormatType>>();
    private InputFormatType dataConsumed;
    private OutputFormatType producedModelElement;
    private ParseProcessResult result = ParseProcessResult.NOT_STARTED;
    private final List<ParserMessage> parserMessages = new LinkedList<ParserMessage>();


    /**
     * @return data used for build {@link #producedModelElement}
     */
    public InputFormatType getDataConsumed() {
        return this.dataConsumed;
    }


    /**
     * @param dataConsumed
     *            data used for build {@link #producedModelElement}
     */
    public void setDataConsumed(InputFormatType dataConsumed) {
        this.dataConsumed = dataConsumed;
    }


    /**
     * @return element of model created
     */
    public OutputFormatType getProducedModelElement() {
        return this.producedModelElement;
    }


    /**
     * @param producedModelElement
     *            element of model created
     */
    public void setProducedModelElement(OutputFormatType producedModelElement) {
        this.producedModelElement = producedModelElement;
    }


    /**
     * @return parsing result
     */
    public ParseProcessResult getResult() {
        return this.result;
    }


    /**
     * @param result
     *            parsing result
     */
    public void setResult(ParseProcessResult result) {
        this.result = result;
    }


    /**
     * @param message
     *            from parser with proper level
     * 
     * @see MessageType
     */
    public void addParserMessage(ParserMessage message) {
        this.parserMessages.add(message);
    }


    /**
     * @return all messages collected during parsing
     */
    public List<ParserMessage> getParserMessages() {
        return this.parserMessages;
    }


    /**
     * 
     * @param garbageData
     *            data exists before declaration of expected elements
     */
    public void addNextTrashData(InputFormatType garbageData) {
        this.trashData.add(garbageData);
    }


    /**
     * 
     * @return data exists before declaration of expected elements
     */
    public List<InputFormatType> getTrashData() {
        return this.trashData;
    }


    /**
     * 
     * @param outOfOrderElement
     *            found element, but because of some restriction in wrong place
     *            or with some problems to solve with user interaction - data,
     *            which could be recovered
     */
    public void addOutOfOrderElementFound(
            OutOfOrderData<InputFormatType> outOfOrderElement) {
        this.outOfOrderOrConstrainsPossibleElements.add(outOfOrderElement);
    }


    /**
     * 
     * @return all found elements, but because of some restriction in wrong
     *         place or with some problems to solve with user interaction -
     *         data, which could be recovered
     */
    public List<OutOfOrderData<InputFormatType>> getOutOfOrderElementsFound() {
        return this.outOfOrderOrConstrainsPossibleElements;
    }
}
