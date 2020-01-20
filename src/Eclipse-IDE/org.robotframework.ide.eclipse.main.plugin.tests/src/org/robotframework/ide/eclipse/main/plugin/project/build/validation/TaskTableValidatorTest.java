/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.newResourceKeyword;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.prepareContext;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TasksProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;


public class TaskTableValidatorTest {

    @Test
    public void nothingIsReported_whenThereIsNoTasksSection() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
    }

    @Test
    public void duplicatedTasksAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task")
                .appendLine("    kw")
                .appendLine("task")
                .appendLine("    kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(TasksProblem.DUPLICATED_TASK, new ProblemPosition(2, Range.closed(14, 18))),
                new Problem(TasksProblem.DUPLICATED_TASK, new ProblemPosition(4, Range.closed(26, 30))));
    }

    @Test
    public void nothingIsReported_whenTasksAreFine() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task")
                .appendLine("    kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
    }

    @Test
    public void innerTaskProblemsAreReported() throws CoreException {
        // just to check if there is also the validation of single tasks
        final RobotSuiteFile file = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isNotEmpty();
    }

    private Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
            throws CoreException {
        final MockReporter reporter = new MockReporter();
        new TaskTableValidator(context, fileModel.findSection(RobotTasksSection.class), reporter)
                .validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }
}
