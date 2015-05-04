package org.robotframework.ide.core.testData.parser;

/**
 * Hold position [start;end] in data.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            accepted by parser input format i.e. bytes
 */
public interface IDataLocator<InputTypeFormat extends IParsePositionMarkable> {

    /**
     * put read pointer to begin of data
     */
    void moveToStart();


    /**
     * put read pointer to end of data
     */
    void moveToEnd();


    /**
     * @return data inside this data locator
     */
    InputTypeFormat getData();
}
