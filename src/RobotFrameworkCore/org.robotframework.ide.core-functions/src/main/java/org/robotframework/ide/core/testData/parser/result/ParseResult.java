package org.robotframework.ide.core.testData.parser.result;

import java.util.LinkedList;
import java.util.List;


/**
 * Represents parsing result status. It holds all data related to computation of
 * model object. It is recommend to use {@link ParseResultBuilder} to builds up
 * this object.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @see ParseResultBuilder
 * 
 * @param <InputFormatType>
 * @param <OutputModelElementType>
 */
public class ParseResult<InputFormatType, OutputModelElementType> {

    private final ParseStatus status;

    private final InputFormatType originalDataUsedForBuild;

    private final OutputModelElementType producedElementModel;

    private final List<ParseResultMessage> parserMessages = new LinkedList<ParseResultMessage>();


    /**
     * 
     * @param status
     * @param originalData
     * @param builtElement
     */
    public ParseResult(final ParseStatus status,
            final InputFormatType originalData,
            final OutputModelElementType builtElement) {
        this.status = status;
        this.originalDataUsedForBuild = originalData;
        this.producedElementModel = builtElement;
    }


    public ParseStatus getStatus() {
        return status;
    }


    public InputFormatType getOriginalDataUsedForBuild() {
        return originalDataUsedForBuild;
    }


    public OutputModelElementType getProducedElementModel() {
        return producedElementModel;
    }


    public List<ParseResultMessage> getParserMessages() {
        return parserMessages;
    }


    public void addAllParsedMessages(final List<ParseResultMessage> messages) {
        this.parserMessages.addAll(messages);
    }

    /**
     * Single localizated message from parser
     * 
     * @author wypych
     * @serial RobotFramework 2.8.6
     * @serial 1.0
     * 
     */
    public static class ParseResultMessage {

        private final ParseResultMessageType type;
        /**
         * Simple message about parsing event
         */
        private final String message;
        /**
         * Because in some source of data giving line number is not possible,
         * this field is textual instead of numerical
         */
        private final String localizationOfProblem;


        public ParseResultMessage(final ParseResultMessageType type,
                final String message, final String localization) {
            this.type = type;
            this.message = message;
            this.localizationOfProblem = localization;
        }


        public ParseResultMessageType getType() {
            return type;
        }


        public String getMessage() {
            return message;
        }


        public String getLocalizationOfProblem() {
            return localizationOfProblem;
        }

        /**
         * Priority of message
         * 
         * @author wypych
         * @serial RobotFramework 2.8.6
         * @serial 1.0
         * 
         */
        public static enum ParseResultMessageType {
            INFO, WARN, ERROR
        }
    }
}
