/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.tasks.Task;

public class RobotTasksSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Tasks";

    RobotTasksSection(final RobotSuiteFile parent, final TaskTable tasksTable) {
        super(parent, SECTION_NAME, tasksTable);
    }

    @Override
    public void link() {
        final TaskTable taskTable = getLinkedElement();
        for (final Task task : taskTable.getTasks()) {
            final RobotTask newTask = new RobotTask(this, task);
            newTask.link();
            elements.add(newTask);
        }
    }

    @Override
    public TaskTable getLinkedElement() {
        return (TaskTable) super.getLinkedElement();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotTask> getChildren() {
        return (List<RobotTask>) super.getChildren();
    }

    @Override
    public String getDefaultChildName() {
        return "task";
    }

    @Override
    public RobotTask createChild(final int index, final String name) {
        final RobotTask task;

        final TaskTable tasksTable = getLinkedElement();
        if (index >= 0 && index < elements.size()) {
            task = new RobotTask(this, tasksTable.createTask(name, index));
            elements.add(index, task);
        } else {
            task = new RobotTask(this, tasksTable.createTask(name));
            elements.add(task);
        }
        return task;
    }

    @Override
    public void insertChild(final int index, final RobotFileInternalElement element) {
        final RobotTask task = (RobotTask) element;
        task.setParent(this);

        final TaskTable tasksTable = getLinkedElement();
        if (index >= 0 && index < elements.size()) {
            getChildren().add(index, task);
            tasksTable.addTask(task.getLinkedElement(), index);
        } else {
            getChildren().add(task);
            tasksTable.addTask(task.getLinkedElement());
        }
    }

    @Override
    public void removeChildren(final List<? extends RobotFileInternalElement> elementsToRemove) {
        getChildren().removeAll(elementsToRemove);

        final TaskTable linkedElement = getLinkedElement();
        for (final RobotFileInternalElement elementToDelete : elementsToRemove) {
            linkedElement.removeTask((Task) elementToDelete.getLinkedElement());
        }
    }

    List<RobotTask> getTasks() {
        return getChildren().stream().map(RobotTask.class::cast).collect(toList());
    }
}
