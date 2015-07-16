package org.robotframework.ide.core.testData.text.context.iterator;

import org.robotframework.ide.core.testData.text.lexer.FilePosition;


public interface ContextTokenIterator {

    boolean hasNext(FilePosition currentPositionInLine);


    /**
     * 
     * @param currentPositionInLine
     * @return {@code null} in case token position is out of current line
     *         column, empty object (without any data inside) in case in current
     *         position is no separators
     */
    RobotSeparatorIteratorOutput next(FilePosition currentPositionInLine);
}
