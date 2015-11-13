/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.KeywordContext;
import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.TestCaseExecutionRowCounter;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;

/**
 * @author mmarzec
 *
 */
public class ForLoopExecutableRowFinder implements IRobotExecutableRowFinder {

    private TestCase currentTestCase;

    private TestCaseExecutionRowCounter testCaseExecutionRowCounter;

    private LinkedList<RobotExecutableRow<?>> forLoopExecutableRows;

    private int forLoopExecutableRowsCounter = 0;
    
    private int currentKeywordsSizeAtLoopStart = 0;

    public ForLoopExecutableRowFinder(final TestCase currentTestCase,
            final TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        this.currentTestCase = currentTestCase;
        this.testCaseExecutionRowCounter = testCaseExecutionRowCounter;
        forLoopExecutableRows = new LinkedList<>();
    }

    @Override
    public RobotExecutableRow<?> findExecutableRow(final LinkedList<KeywordContext> currentKeywords) {

        if (forLoopExecutableRows.isEmpty()) {
            currentKeywordsSizeAtLoopStart = currentKeywords.size();
            KeywordContext parentKeywordContext = null;
            final int forLoopParentKeywordContextPosition = getForLoopParentKeywordContextPosition(currentKeywords);
            if (forLoopParentKeywordContextPosition >= 0) {
                parentKeywordContext = currentKeywords.get(forLoopParentKeywordContextPosition);
            }
            final List<RobotExecutableRow<?>> executableRows = extractAllExecutableRows(parentKeywordContext);
            int forLoopStartPosition = extractForLoopStartPosition(parentKeywordContext);
            forLoopExecutableRows.add(executableRows.get(forLoopStartPosition));
            for (int i = forLoopStartPosition + 1; i < executableRows.size(); i++) {
                if (executableRows.get(i).isExecutable()) {
                    if (isForLoopItem(executableRows.get(i))) {
                        forLoopExecutableRows.add(executableRows.get(i));
                    } else {
                        break;
                    }
                }
                incrementExecutionRowCounterInsideForLoop(parentKeywordContext);
            }
            return forLoopExecutableRows.getFirst();
        }
        
        if(currentKeywords.size() > (currentKeywordsSizeAtLoopStart+1)) {
            return null;    // cannot step into the keyword placed inside a for loop
        }

        forLoopExecutableRowsCounter++;
        if (hasReachedNextForLoopIteration()) {
            forLoopExecutableRowsCounter = 0;
            return forLoopExecutableRows.getFirst();
        }

        return forLoopExecutableRows.get(forLoopExecutableRowsCounter);
    }

    public void clear() {
        forLoopExecutableRows.clear();
        forLoopExecutableRowsCounter = 0;
        currentKeywordsSizeAtLoopStart = 0;
    }

    private void incrementExecutionRowCounterInsideForLoop(final KeywordContext parentKeywordContext) {
        if (parentKeywordContext != null && parentKeywordContext.getUserKeyword() != null)
            parentKeywordContext.incrementKeywordExecutableRowCounter();
        else
            testCaseExecutionRowCounter.increment();
        ;
    }

    private int getForLoopParentKeywordContextPosition(final LinkedList<KeywordContext> currentKeywords) {
        return currentKeywords.size() - 3;
    }

    private List<RobotExecutableRow<?>> extractAllExecutableRows(KeywordContext parentKeywordContext) {
        final List<RobotExecutableRow<?>> executableRows = new ArrayList<>();
        if (parentKeywordContext != null && parentKeywordContext.getUserKeyword() != null) {
            executableRows.addAll(parentKeywordContext.getUserKeyword().getKeywordExecutionRows());
        } else {
            executableRows.addAll(currentTestCase.getTestExecutionRows());
        }
        return executableRows;
    }

    private int extractForLoopStartPosition(KeywordContext parentKeywordContext) {
        int counter;
        if (parentKeywordContext != null && parentKeywordContext.getUserKeyword() != null) {
            counter = parentKeywordContext.getKeywordExecutableRowCounter() - 1;
        } else {
            counter = testCaseExecutionRowCounter.getCounter() - 1;
        }
        return counter;
    }

    private boolean isForLoopItem(RobotExecutableRow<?> executableRow) {
        return executableRow.getAction().getText().toString().equals("\\");
    }

    private boolean hasReachedNextForLoopIteration() {
        return forLoopExecutableRowsCounter == forLoopExecutableRows.size();
    }

    public void setCurrentTestCase(TestCase currentTestCase) {
        this.currentTestCase = currentTestCase;
    }

    public void setTestCaseExecutionRowCounter(TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        this.testCaseExecutionRowCounter = testCaseExecutionRowCounter;
    }

}
