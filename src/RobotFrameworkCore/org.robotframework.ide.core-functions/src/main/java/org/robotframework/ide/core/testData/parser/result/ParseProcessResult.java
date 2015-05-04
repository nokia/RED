package org.robotframework.ide.core.testData.parser.result;

/**
 * Represents the status of parsing process.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public enum ParseProcessResult {
    /**
     * Initial state - means nothing was performed
     */
    NOT_STARTED,

    /**
     * Says that everything was happen right, no errors or warnings
     */
    PARSED_WITH_SUCCESS,

    /**
     * Something was wrong, but the problem wasn't so big that data can't be
     * decoded
     */
    PARTIAL_SUCCESS,

    /**
     * Indicate that big problem occurred and user should check output of
     * parsing
     */
    FAILED
}