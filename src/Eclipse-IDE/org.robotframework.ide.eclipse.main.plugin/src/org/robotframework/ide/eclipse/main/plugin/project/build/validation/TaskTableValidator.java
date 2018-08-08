/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TasksProblem;

import com.google.common.collect.ImmutableMap;

class TaskTableValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final Optional<RobotTasksSection> taskSection;

    private final ValidationReportingStrategy reporter;

    TaskTableValidator(final FileValidationContext validationContext, final Optional<RobotTasksSection> section,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.taskSection = section;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!taskSection.isPresent()) {
            return;
        }
        final RobotTasksSection robotTasksSection = taskSection.get();
        final TaskTable tasksTable = robotTasksSection.getLinkedElement();
        final List<Task> tasks = tasksTable.getTasks();

        reportDuplicatedTasks(tasks);

        validateTasks(tasks);
    }

    private void reportDuplicatedTasks(final List<Task> tasks) {
        final Set<String> duplicatedNames = newHashSet();

        for (final Task task1 : tasks) {
            for (final Task task2 : tasks) {
                if (task1 != task2) {
                    final String task1Name = task1.getName().getText();
                    final String task2Name = task2.getName().getText();

                    if (task1Name.equalsIgnoreCase(task2Name)) {
                        duplicatedNames.add(task1Name.toLowerCase());
                    }
                }
            }
        }

        for (final Task task : tasks) {
            final RobotToken taskName = task.getName();
            final String name = taskName.getText();

            if (duplicatedNames.contains(name.toLowerCase())) {
                final RobotProblem problem = RobotProblem.causedBy(TasksProblem.DUPLICATED_TASK)
                        .formatMessageWith(name);
                final Map<String, Object> additionalArguments = ImmutableMap.of("name", name);
                reporter.handleProblem(problem, validationContext.getFile(), taskName, additionalArguments);
            }
        }
    }

    private void validateTasks(final List<Task> tasks) {
        for (final Task task : tasks) {
            new TaskValidator(validationContext, task, reporter).validate();
        }
    }
}
