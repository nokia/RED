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


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((localization == null) ? 0 : localization.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParserMessage other = (ParserMessage) obj;
        if (localization == null) {
            if (other.localization != null)
                return false;
        } else if (!localization.equals(other.localization))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
}
