/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.prepareContext;

import java.util.Collection;

import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;


public class UnknownTablesValidatorTest {

    static final String[] ALL = new String[] { "cause", "start", "end", "message" };

    @Test
    public void nothingIsReported_whenThereIsKnownTable() {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), file);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenThereIsCommentsTable() {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Comments ***")
                .appendLine("some comment")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), file);
        assertThat(problems).isEmpty();
    }

    @Test
    public void unrecognizedHeaderIsReported_whenUnknownTableHeaderIsDefined_inOlderRf() {
        final RobotVersion version = new RobotVersion(3, 0);
        final RobotSuiteFile file = new RobotSuiteFileCreator(version)
                .appendLine("*** Header ***")
                .appendLine("something")
                .build();

        final Collection<Problem> problems = validate(prepareContext(version), file);
        assertThat(problems).extracting(ALL)
                .containsOnly(problem(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER, 0, 14,
                        "Unrecognized table header: 'Header'"));
    }

    @Test
    public void unrecognizedHeaderIsReported_whenTasksTableIsDefined_inOlderRf() {
        final RobotVersion version = new RobotVersion(3, 0);
        final RobotSuiteFile file = new RobotSuiteFileCreator(version)
                .appendLine("*** Tasks ***")
                .appendLine("task")
                .build();

        final Collection<Problem> problems = validate(prepareContext(version), file);
        assertThat(problems).extracting(ALL)
                .containsOnly(problem(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER, 0, 13,
                        "Unrecognized table header: 'Tasks'. The tasks table is introduced in Robot Framework 3.1. "
                                + "Please verify if your project uses at least that version."));
    }

    @Test
    public void unrecognizedHeaderIsReported_whenUnknownTableHeaderIsDefined_inNewerRf() {
        final RobotVersion version = new RobotVersion(3, 1);
        final RobotSuiteFile file = new RobotSuiteFileCreator(version).appendLine("*** Header ***")
                .appendLine("something")
                .build();

        final Collection<Problem> problems = validate(prepareContext(version), file);
        assertThat(problems).extracting(ALL)
                .containsOnly(problem(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER_RF31, 0, 14,
                        "Unrecognized table header: 'Header'"));
    }

    @Test
    public void nothingIsReported_whenThereIsTasksTableDefined_inNewerRf() {
        final RobotVersion version = new RobotVersion(3, 1);
        final RobotSuiteFile file = new RobotSuiteFileCreator(version).appendLine("*** Tasks ***")
                .appendLine("task")
                .build();

        final Collection<Problem> problems = validate(prepareContext(version), file);
        assertThat(problems).isEmpty();
    }

    @Test
    public void unrecognizedHeaderIsReported_whenUnknownCombinedTableHeaderIsDefined() {
        final RobotVersion version = new RobotVersion(3, 0);
        final RobotSuiteFile file = new RobotSuiteFileCreator(version)
                .appendLine("** * Settings ************* Variables")
                .appendLine("something")
                .build();

        final Collection<Problem> problems = validate(prepareContext(version), file);
        assertThat(problems).extracting(ALL)
                .containsOnly(problem(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER, 0, 37,
                        "Unrecognized table header: 'Settings Variables'"));
    }

    private static Tuple problem(final Object... properties) {
        // adding synonym for better readability
        return tuple(properties);
    }

    private static Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel) {
        final MockReporter reporter = new MockReporter();
        new UnknownTablesValidator(context, fileModel, reporter).validate();
        return reporter.getReportedProblems();
    }
}
