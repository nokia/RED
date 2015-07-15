package org.robotframework.ide.core.testData.text.context;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.recognizer.ContextElementComparator;


public class PipeSeparableIterator implements Iterator<IContextElement> {

    private final AggregatedOneLineRobotContexts lineContexts;
    private final List<IContextElement> separatorPositionPerpahse;
    private int separatorIndex = 0;


    public PipeSeparableIterator(
            final AggregatedOneLineRobotContexts lineContexts) {
        this.lineContexts = lineContexts;

        this.separatorPositionPerpahse = lineContexts.getSeparators()
                .getPipeSeparators();
        System.out.println(separatorPositionPerpahse);
        List<IContextElement> contextsInLine = lineContexts.getChildContexts();
        Collections.sort(contextsInLine, new ContextElementComparator());

        findNext();
    }


    private void findNext() {

    }


    @Override
    public boolean hasNext() {

        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public IContextElement next() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void remove() {

    }
}
