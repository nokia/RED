/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.tasks;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesStepsHolderElementOperation;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TaskExecutableRowModelOperation implements IExecutablesStepsHolderElementOperation<Task> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TASK_EXECUTABLE_ROW;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TASK_ACTION_NAME;
    }

    @Override
    public AModelElement<Task> create(final Task task, final int index, final String actionName,
            final List<String> args, final String comment) {
        final RobotExecutableRow<Task> row = new RobotExecutableRow<>();
        row.setParent(task);

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
    public AModelElement<?> insert(final Task task, final int index, final AModelElement<?> modelElement) {
        return task.addElement(index, modelElement);
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
}
