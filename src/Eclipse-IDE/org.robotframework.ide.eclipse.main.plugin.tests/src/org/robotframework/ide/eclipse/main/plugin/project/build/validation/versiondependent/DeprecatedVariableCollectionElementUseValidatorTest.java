/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class DeprecatedVariableCollectionElementUseValidatorTest {

    @Test
    public void outdatedVariableUsageIsReported_forSimpleListInTestCases() throws CoreException {
        final RobotVersion version = new RobotVersion(3, 2);
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().setVersion(version)
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  @{list}=  Create List   1  2  3")
                .appendLine("  Log  @{list}[1]")
                .build();

        final Collection<Problem> problems = validate(fileModel);
        assertThat(problems).containsExactly(
                new Problem(VariablesProblem.VARIABLE_ELEMENT_OLD_USE, new ProblemPosition(4, Range.closed(65, 75))));
    }

    @Test
    public void outdatedVariableUsageIsReported_forSimpleDictInTasks() throws CoreException {
        final RobotVersion version = new RobotVersion(3, 2);
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().setVersion(version)
                .appendLine("*** Tasks ***")
                .appendLine("task")
                .appendLine("  &{dict}=  Create Dictionary   a=1")
                .appendLine("  Log  &{dict}[a]")
                .build();

        final Collection<Problem> problems = validate(fileModel);
        assertThat(problems).containsExactly(
                new Problem(VariablesProblem.VARIABLE_ELEMENT_OLD_USE, new ProblemPosition(4, Range.closed(62, 72))));
    }

    @Test
    public void outdatedVariableUsageIsReported_whenInsideKeywordCall() throws CoreException {
        final RobotVersion version = new RobotVersion(3, 2);
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().setVersion(version)
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  @{list}=  Create List   1  2  3")
                .appendLine("  Keyword@{list}[1]call")
                .build();

        final Collection<Problem> problems = validate(fileModel);
        assertThat(problems).containsExactly(
                new Problem(VariablesProblem.VARIABLE_ELEMENT_OLD_USE, new ProblemPosition(4, Range.closed(67, 77))));
    }

    @Test
    public void noProblemIsReported_whenThereIsNoIndexedVariable() throws CoreException {
        final RobotVersion version = new RobotVersion(3, 2);
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().setVersion(version)
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  @{list}=  Create List   1  2  3")
                .appendLine("  Log  @{list}")
                .build();

        final Collection<Problem> problems = validate(fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void noProblemIsReported_whenThereIsOnlyVariableAccessedThePropperWay() throws CoreException {
        final RobotVersion version = new RobotVersion(3, 2);
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().setVersion(version)
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  @{list}=  Create List   1  2  3")
                .appendLine("  Log  ${list}[1]")
                .build();

        final Collection<Problem> problems = validate(fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void allOutdatedVariablesUsageIsReported_forMultipleVariablesInSameToken1() throws CoreException {
        final RobotVersion version = new RobotVersion(3, 2);
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().setVersion(version)
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  @{list}=  Create List   1  2  3")
                .appendLine("  &{dict}=  Create Dictionary   a=1")
                .appendLine("  Keyword@{list}[1]call${var}[1]with&{dict}[a]variables")
                .build();

        final Collection<Problem> problems = validate(fileModel);
        assertThat(problems).containsExactly(
                new Problem(VariablesProblem.VARIABLE_ELEMENT_OLD_USE, new ProblemPosition(5, Range.closed(103, 113))),
                new Problem(VariablesProblem.VARIABLE_ELEMENT_OLD_USE, new ProblemPosition(5, Range.closed(130, 140))));
    }

    @Test
    public void allOutdatedVariablesUsageIsReported_forMultipleVariablesInSameToken2() throws CoreException {
        final RobotVersion version = new RobotVersion(3, 2);
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().setVersion(version)
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  @{list}=  Create List   1  2  3")
                .appendLine("  &{dict}=  Create Dictionary   a=1")
                .appendLine("  @{list}[1]call${var}[1]with@{this_is_correct_list[1]}variables&{dict}[a]")
                .build();

        final Collection<Problem> problems = validate(fileModel);
        assertThat(problems).containsExactly(
                new Problem(VariablesProblem.VARIABLE_ELEMENT_OLD_USE, new ProblemPosition(5, Range.closed(96, 106))),
                new Problem(VariablesProblem.VARIABLE_ELEMENT_OLD_USE, new ProblemPosition(5, Range.closed(158, 168))));
    }

    private Collection<Problem> validate(final RobotSuiteFile fileModel) throws CoreException {
        final MockReporter reporter = new MockReporter();
        new DeprecatedVariableCollectionElementUseValidator(fileModel.getFile(), fileModel, reporter)
                .validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }
}
