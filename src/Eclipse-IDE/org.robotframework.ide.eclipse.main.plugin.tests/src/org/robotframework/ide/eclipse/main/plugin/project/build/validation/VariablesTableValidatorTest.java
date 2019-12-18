/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.prepareContext;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class VariablesTableValidatorTest {

    @Test
    public void nothingIsReported_whenThereIsNoVariablesSection() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("").build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenValidVariablesAreDefined() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("${scalar}  1")
                .appendLine("@{list}  1")
                .appendLine("&{dict}  k=v")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
    }


    @Test
    public void unrecognizedVariableIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("var  1")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.INVALID_TYPE, new ProblemPosition(2, Range.closed(18, 21))));
    }

    @Test
    public void invalidVariableNameIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("$ {var}  1")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.INVALID_NAME, new ProblemPosition(2, Range.closed(18, 25))));
    }

    @Test
    public void duplicatedVariablesAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("${var}  1")
                .appendLine("@{var}  2")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.DUPLICATED_VARIABLE, new ProblemPosition(2, Range.closed(18, 24))),
                new Problem(VariablesProblem.DUPLICATED_VARIABLE, new ProblemPosition(3, Range.closed(28, 34))));
    }

    @Test
    public void invalidDictionaryItemsAreReported() throws Exception {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("&{dict}  a  ${b}  c=  d=1")
                .build();

        final FileValidationContext context = prepareContext(newHashSet("${b}"));

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.INVALID_DICTIONARY_ELEMENT_SYNTAX,
                        new ProblemPosition(2, Range.closed(27, 28))),
                new Problem(VariablesProblem.INVALID_DICTIONARY_ELEMENT_SYNTAX,
                        new ProblemPosition(2, Range.closed(30, 34))));
    }

    @Test
    public void unknownVariablesAreReportedInValues() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("${scalar}  ${a}")
                .appendLine("@{list}  ${b}  ${c}")
                .appendLine("&{dict}  k1=${d}  k2=${e}")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(29, 33))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(43, 47))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(49, 53))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(4, Range.closed(66, 70))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(4, Range.closed(75, 79))));
    }

    @Test
    public void multipleProblemsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("scalar  1")
                .appendLine("$ {x}  1")
                .appendLine("${var}  1")
                .appendLine("@{var}  2")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.INVALID_TYPE, new ProblemPosition(2, Range.closed(18, 24))),
                new Problem(VariablesProblem.INVALID_NAME, new ProblemPosition(3, Range.closed(28, 33))),
                new Problem(VariablesProblem.DUPLICATED_VARIABLE, new ProblemPosition(4, Range.closed(37, 43))),
                new Problem(VariablesProblem.DUPLICATED_VARIABLE, new ProblemPosition(5, Range.closed(47, 53))));

    }

    @Test
    public void variableWithoutAssignmentIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${var1}")
                .appendLine("@{var2}=")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.VARIABLE_DECLARATION_WITHOUT_ASSIGNMENT,
                        new ProblemPosition(2, Range.closed(18, 25))),
                new Problem(VariablesProblem.VARIABLE_DECLARATION_WITHOUT_ASSIGNMENT,
                        new ProblemPosition(3, Range.closed(26, 33))));

    }

    @Test
    public void variableWithAssignmentIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${var1}=    variable1")
                .appendLine("@{var2}=    1    2    3    4")
                .appendLine("&{var3}=    d1=1    d2=2    d3=3    d4=4")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();

    }

    @Test
    public void variableWithoutNameIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("@{}=  sth")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.VARIABLE_DECLARATION_WITHOUT_NAME,
                        new ProblemPosition(2, Range.closed(18, 22))));
    }

    @Test
    public void commentedVariablesAreNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("# @{}")
                .appendLine("# &{var3}")
                .appendLine("# & {var1}")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
    }

    private Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
            throws CoreException {
        final MockReporter reporter = new MockReporter();
        new VariablesTableValidator(context, fileModel.findSection(RobotVariablesSection.class), reporter)
                .validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }
}
