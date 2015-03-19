package org.robotframework.ide.core.testData.model.parser.result;

/**
 * Informs about status of element from Test Data parsing to model.
 *  
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 */
public enum ParseStatus {
    /**
     * process of parsing was not performed - it is initial status
     * @serial 1.0
     */
    NOT_STARTED,
    /**
     * process was performed and element of model was created
     * @serial 1.0
     */
    SUCCESSFULY_PARSED,
    /**
     * process was performed, but build of model element couldn't be performed
     * @serial 1.0
     */
    UNSUCCESFULY_PARSED
}
