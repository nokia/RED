package org.robotframework.ide.core.testData.parser;

/**
 * Pushback logic extractor, in case of fails it should be possibility to
 * rollback current read of data.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public interface IParsePositionMarkable {

    /**
     * Sets current position before parsing element
     */
    void mark();


    /**
     * Reset position to last marked position in parsing process
     */
    void reset();
}
