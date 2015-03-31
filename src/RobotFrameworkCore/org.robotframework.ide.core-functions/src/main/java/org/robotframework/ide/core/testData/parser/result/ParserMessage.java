package org.robotframework.ide.core.testData.parser.result;

/**
 * Message from parser including localization of problem or warning
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class ParserMessage {

    private final MessageType type;
    private final String localization;
    private final String message;


    /**
     * @param type
     * @param localization
     * @param message
     */
    public ParserMessage(MessageType type, String localization, String message) {
        this.type = type;
        this.localization = localization;
        this.message = message;
    }


    /**
     * @return type of event
     */
    public MessageType getType() {
        return type;
    }


    /**
     * @return information where problem or warning occurred
     */
    public String getLocalization() {
        return localization;
    }


    /**
     * @return textual message from parser
     */
    public String getMessage() {
        return message;
    }
}
