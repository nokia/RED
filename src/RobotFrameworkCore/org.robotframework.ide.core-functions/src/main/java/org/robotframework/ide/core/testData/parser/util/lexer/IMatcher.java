package org.robotframework.ide.core.testData.parser.util.lexer;

/**
 * Take responsibility for perform matching process base on own wrote logic.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public interface IMatcher {

    /**
     * check if current data contains data expected
     * 
     * @param data
     *            to check
     * @param dataWindowAllowed
     *            give information from where we should start and where last
     *            position to check
     * @return
     */
    MatchResult match(byte[] data, Position dataWindowAllowed);
}
