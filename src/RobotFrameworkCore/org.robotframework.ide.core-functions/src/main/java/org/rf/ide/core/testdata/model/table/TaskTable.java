/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TaskTable extends ARobotSectionTable {

    private final List<Task> tasks = new ArrayList<>();

    public TaskTable(final RobotFile parent) {
        super(parent);
    }

    public Task createTask(final String taskName) {
        return createTask(taskName, tasks.size());
    }

    public Task createTask(final String taskName, final int position) {
        final Task task = new Task(RobotToken.create(taskName));
        addTask(task, position);
        return task;
    }

    public void addTask(final Task task) {
        task.setParent(this);
        tasks.add(task);
    }

    public void addTask(final Task task, final int position) {
        task.setParent(this);
        tasks.add(position, task);
    }

    public void removeTask(final Task task) {
        tasks.remove(task);
    }

    @Override
    public boolean moveUpElement(final AModelElement<? extends ARobotSectionTable> element) {
        return MoveElementHelper.moveUp(tasks, (Task) element);
    }

    @Override
    public boolean moveDownElement(final AModelElement<? extends ARobotSectionTable> element) {
        return MoveElementHelper.moveDown(tasks, (Task) element);
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}
