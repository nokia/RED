package org.robotframework.ide.core.testData.text.lexer;

/**
 * Declares generic type for single character robot tokens and more complex like
 * reserved words.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public interface RobotType {

    /**
     * @return an information if token can be directly write to output stream
     */
    boolean isWriteable();


    /**
     * @return text for this token type to write to output stream - this is only
     *         usefull for tokens like 'Test', asterisks '*'
     */
    String toWrite();
}
