package org.robotframework.ide.core.testData.parser.result;

/**
 * Informs about final status of parsing from file to memory model of Test Data.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public enum ParseStatus {
    /**
     * parsing was done fully without any problems
     */
    SUCCESSFULY_PARSED,
    /**
     * parsing was done but some optional parts wasn't found
     */
    PARTIAL_PARSED,
    /**
     * parsing failed
     */
    UNSUCCESSFULY_PARSED
}
