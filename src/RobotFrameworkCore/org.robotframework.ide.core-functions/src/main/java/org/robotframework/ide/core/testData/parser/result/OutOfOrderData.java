package org.robotframework.ide.core.testData.parser.result;

/**
 * Holder of data, which should be placed in current position.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 */
public class OutOfOrderData<InputFormatType> {

    private final InputFormatType originalWrongData;
    private final ParserMessage message;


    /**
     * @param data
     *            collected and possible to convert to correct format
     * @param localization
     *            where it was found
     * @param message
     *            simple information for user
     */
    public OutOfOrderData(final InputFormatType data, String localization,
            String message) {
        this.originalWrongData = data;
        this.message = new ParserMessage(MessageType.WARN, localization,
                message);
    }


    /**
     * @return data collected
     */
    public InputFormatType getOriginalWrongData() {
        return originalWrongData;
    }


    /**
     * @return information for user
     */
    public ParserMessage getMessage() {
        return message;
    }
}
