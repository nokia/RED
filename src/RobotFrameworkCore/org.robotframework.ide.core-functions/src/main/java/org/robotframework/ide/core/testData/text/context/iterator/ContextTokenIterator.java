package org.robotframework.ide.core.testData.text.context.iterator;

import org.robotframework.ide.core.testData.text.lexer.FilePosition;


public interface ContextTokenIterator {

    boolean hasNext(FilePosition currentPositionInLine);


    RobotSeparatorIteratorOutput next();
}
