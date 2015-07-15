package org.robotframework.ide.core.testData.text.context.iterator;

import org.robotframework.ide.core.testData.text.context.AggregatedOneLineRobotContexts;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;


public class WhitespaceSeparableIterator implements ContextTokenIterator {

    private final AggregatedOneLineRobotContexts ctx;


    public WhitespaceSeparableIterator(final AggregatedOneLineRobotContexts ctx) {
        this.ctx = ctx;
    }


    @Override
    public boolean hasNext(FilePosition currentPositionInLine) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public RobotSeparatorIteratorOutput next() {
        // TODO Auto-generated method stub
        return null;
    }

}
