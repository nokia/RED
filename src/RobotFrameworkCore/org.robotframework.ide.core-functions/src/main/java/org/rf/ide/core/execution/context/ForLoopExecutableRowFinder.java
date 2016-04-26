/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.execution.context.RobotDebugExecutionContext.KeywordContext;
import org.rf.ide.core.execution.context.RobotDebugExecutionContext.TestCaseExecutionRowCounter;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;


/**
 * @author mmarzec
 * 
 */
public class ForLoopExecutableRowFinder implements IRobotExecutableRowFinder {

    private TestCase currentTestCase;

    private TestCaseExecutionRowCounter testCaseExecutionRowCounter;
    
    private IRobotExecutableRowFinder userKeywordExecutableRowFinder;
    
    private IRobotExecutableRowFinder nestedForLoopExecutableRowFinder;

    private final List<RobotExecutableRow<?>> forLoopExecutableRows;

    private int forLoopExecutableRowsCounter = 0;

    private int currentKeywordsSizeAtLoopStart = 0;


    public ForLoopExecutableRowFinder(final TestCase currentTestCase,
            final TestCaseExecutionRowCounter testCaseExecutionRowCounter,
            final IRobotExecutableRowFinder userKeywordExecutableRowFinder) {
        this.currentTestCase = currentTestCase;
        this.testCaseExecutionRowCounter = testCaseExecutionRowCounter;
        this.userKeywordExecutableRowFinder = userKeywordExecutableRowFinder;
        forLoopExecutableRows = new ArrayList<>();
    }

    @Override
    public RobotExecutableRow<?> findExecutableRow(final List<KeywordContext> currentKeywords) {

        if (forLoopExecutableRows.isEmpty()) {
            final KeywordContext parentKeywordContext = getForLoopParentKeywordContext(currentKeywords);
            fillForLoopExecutableRows(parentKeywordContext);
            return forLoopExecutableRows.isEmpty() ? null : forLoopExecutableRows.get(0);
        }

        if (currentKeywords.size() > (currentKeywordsSizeAtLoopStart + 1)) { // step into the keyword placed inside a for loop
            return findExecutableRowInUserKeywordNestedInForLoop(currentKeywords);
        }
        resetNestedForLoopExecutableRowFinder();

        forLoopExecutableRowsCounter++;
        if (hasReachedNextForLoopIteration()) {
            forLoopExecutableRowsCounter = 0;
            return forLoopExecutableRows.get(0);
        }

        return forLoopExecutableRows.get(forLoopExecutableRowsCounter);
    }

    public void clear() {
        forLoopExecutableRows.clear();
        forLoopExecutableRowsCounter = 0;
        currentKeywordsSizeAtLoopStart = 0;
    }
    
    private KeywordContext getForLoopParentKeywordContext(final List<KeywordContext> currentKeywords) {
        currentKeywordsSizeAtLoopStart = currentKeywords.size();
        final int forLoopParentKeywordContextPosition = getForLoopParentKeywordContextPosition(currentKeywords);
        if (forLoopParentKeywordContextPosition >= 0) {
            return currentKeywords.get(forLoopParentKeywordContextPosition);
        }
        return null;
    }
    
    private int getForLoopParentKeywordContextPosition(
            final List<KeywordContext> currentKeywords) {
        return currentKeywords.size() - 3;
    }

    private void fillForLoopExecutableRows(final KeywordContext parentKeywordContext) {
        final List<RobotExecutableRow<?>> executableRows = extractAllExecutableRows(parentKeywordContext);
        final int forLoopStartPosition = extractForLoopStartPosition(parentKeywordContext);
        if (forLoopStartPosition >= 0) {
            forLoopExecutableRows.add(executableRows.get(forLoopStartPosition));
            for (int i = forLoopStartPosition + 1; i < executableRows.size(); i++) {
                if (executableRows.get(i).isExecutable()) {
                    if (isForLoopItem(executableRows.get(i))) {
                        forLoopExecutableRows.add(executableRows.get(i));
                    } else {
                        return;
                    }
                }
                
                incrementExecutionRowCounterInsideForLoop(parentKeywordContext);
            }
        }
    }
    
    private List<RobotExecutableRow<?>> extractAllExecutableRows(final KeywordContext parentKeywordContext) {
        final List<RobotExecutableRow<?>> executableRows = new ArrayList<>();
        if (parentKeywordContext != null && parentKeywordContext.getUserKeyword() != null) {
            executableRows.addAll(parentKeywordContext.getUserKeyword().getKeywordExecutionRows());
        } else if (currentTestCase != null) {
            executableRows.addAll(currentTestCase.getTestExecutionRows());
        }
        return executableRows;
    }
    
    private int extractForLoopStartPosition(final KeywordContext parentKeywordContext) {
        int counter;
        if (parentKeywordContext != null && parentKeywordContext.getUserKeyword() != null) {
            counter = parentKeywordContext.getKeywordExecutableRowCounter() - 1;
        } else {
            counter = testCaseExecutionRowCounter.getCounter() - 1;
        }
        return counter;
    }

    private void incrementExecutionRowCounterInsideForLoop(final KeywordContext parentKeywordContext) {
        if (parentKeywordContext != null && parentKeywordContext.getUserKeyword() != null) {
            parentKeywordContext.incrementKeywordExecutableRowCounter();
        } else {
            testCaseExecutionRowCounter.increment();
        }
    }
    
    private boolean isForLoopItem(final RobotExecutableRow<?> executableRow) {
        return executableRow.buildLineDescription().getRowType() == ERowType.FOR_CONTINUE;
    }

    private RobotExecutableRow<?> findExecutableRowInUserKeywordNestedInForLoop(final List<KeywordContext> currentKeywords) {
        RobotExecutableRow<?> executableRow = null;
        if (userKeywordExecutableRowFinder != null) {
            executableRow = userKeywordExecutableRowFinder.findExecutableRow(currentKeywords);
            if (executableRow == null) {
                // for loop nested in another for loop
                if (nestedForLoopExecutableRowFinder == null) {
                    nestedForLoopExecutableRowFinder = new ForLoopExecutableRowFinder(currentTestCase,
                            new TestCaseExecutionRowCounter(), userKeywordExecutableRowFinder);
                }
                executableRow = nestedForLoopExecutableRowFinder.findExecutableRow(currentKeywords);
            }
        }
        return executableRow;
    }
    
    private boolean hasReachedNextForLoopIteration() {
        return forLoopExecutableRowsCounter == forLoopExecutableRows.size();
    }
    
    private void resetNestedForLoopExecutableRowFinder() {
        nestedForLoopExecutableRowFinder = null;
    }

    public void setCurrentTestCase(final TestCase currentTestCase) {
        this.currentTestCase = currentTestCase;
    }


    public void setTestCaseExecutionRowCounter(final TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        this.testCaseExecutionRowCounter = testCaseExecutionRowCounter;
    }

}
