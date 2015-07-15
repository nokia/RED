package org.robotframework.ide.core.testData.text.context;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.recognizer.ContextElementComparator;


public class WhitespaceSeparableIterator implements Iterator<IContextElement> {

    private final AggregatedOneLineRobotContexts lineContexts;
    private final List<IContextElement> separatorPositionPerpahse;
    private int separatorIndex = 0;


    public WhitespaceSeparableIterator(
            final AggregatedOneLineRobotContexts lineContexts) {
        this.lineContexts = lineContexts;

        this.separatorPositionPerpahse = lineContexts.getSeparators()
                .getPipeSeparators();
        Collections.sort(lineContexts.getChildContexts(),
                new ContextElementComparator());
        System.out
                .println(lineContexts
                        .getSeparators()
                        .getFoundSeperatorsExcludeType()
                        .get(SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED));
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
        // TODO Auto-generated method stub

    }
}
