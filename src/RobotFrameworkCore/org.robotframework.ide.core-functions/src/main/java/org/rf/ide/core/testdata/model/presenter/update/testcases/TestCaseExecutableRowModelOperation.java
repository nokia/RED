/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesStepsHolderElementOperation;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TestCaseExecutableRowModelOperation implements IExecutablesStepsHolderElementOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_EXECUTABLE_ROW;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_ACTION_NAME;
    }

    @Override
    public AModelElement<TestCase> create(final TestCase testCase, final int index, final String actionName,
            final List<String> args, final String comment) {
        final RobotExecutableRow<TestCase> row = new RobotExecutableRow<>();
        row.setParent(testCase);

        row.setAction(RobotToken.create(actionName));
        for (final String argument : args) {
            row.addArgument(RobotToken.create(argument));
        }
        if (comment != null && !comment.isEmpty()) {
            row.setComment(comment);
        }
        return row;
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        @SuppressWarnings("unchecked")
        final RobotExecutableRow<TestCase> executableRow = (RobotExecutableRow<TestCase>) modelElement;

        testCase.addElement(executableRow, index);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final RobotExecutableRow<?> row = (RobotExecutableRow<?>) modelElement;

        if (value != null) {
            row.setArgument(index, value);
        } else if (index < row.getArguments().size()) {
            row.removeElementToken(index);
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final RobotExecutableRow<?> row = (RobotExecutableRow<?>) modelElement;

        final int elementsToRemove = row.getArguments().size();
        for (int i = 0; i < elementsToRemove; i++) {
            row.removeElementToken(0);
        }
        for (int i = 0; i < newArguments.size(); i++) {
            row.setArgument(i, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeElement((RobotExecutableRow<TestCase>) modelElement);
    }
}
