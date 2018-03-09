/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Range;

public class VariablesTableValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void nothingIsReported_whenThereIsNoVariablesSection() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("").build();

        final FileValidationContext context = prepareContext();
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.wasProblemReported()).isFalse();
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
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.wasProblemReported()).isFalse();
    }

    @Test
    public void customProblemsAreRaised_whenVersionDependentValidatorsAreUsed() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("${scalar}  1")
                .build();

        final IProblemCause mockedCause = mock(IProblemCause.class);
        final VersionDependentModelUnitValidator alwaysFailingVersionDepValidator_1 = new VersionDependentModelUnitValidator() {
            @Override
            public void validate(final IProgressMonitor monitor) throws CoreException {
                reporter.handleProblem(RobotProblem.causedBy(mockedCause), null,
                        new ProblemPosition(2, Range.closed(18, 27)));
            }

            @Override
            protected Range<RobotVersion> getApplicableVersionRange() {
                return Range.all();
            }
        };
        final VersionDependentModelUnitValidator alwaysFailingVersionDepValidator_2 = new VersionDependentModelUnitValidator() {
            @Override
            public void validate(final IProgressMonitor monitor) throws CoreException {
                reporter.handleProblem(RobotProblem.causedBy(mockedCause), null,
                        new ProblemPosition(2, Range.closed(18, 30)));
            }

            @Override
            protected Range<RobotVersion> getApplicableVersionRange() {
                return Range.all();
            }
        };
        final VersionDependentModelUnitValidator alwaysPassingVersionDepValidator = new VersionDependentModelUnitValidator() {
            @Override
            public void validate(final IProgressMonitor monitor) throws CoreException {
                // that's fine it passes
            }

            @Override
            protected Range<RobotVersion> getApplicableVersionRange() {
                return Range.all();
            }
        };
        final VersionDependentValidators versionValidators = createVersionDependentValidators(
                alwaysFailingVersionDepValidator_1, alwaysFailingVersionDepValidator_2,
                alwaysPassingVersionDepValidator);
        final FileValidationContext context = prepareContext();
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, versionValidators);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(mockedCause, new ProblemPosition(2, Range.closed(18, 27))),
                new Problem(mockedCause, new ProblemPosition(2, Range.closed(18, 30))));
    }

    @Test
    public void unrecognizedVariableIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("var  1")
                .build();

        final FileValidationContext context = prepareContext();
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(VariablesProblem.INVALID_TYPE, new ProblemPosition(2, Range.closed(18, 21))));
    }

    @Test
    public void invalidVariableNameIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("$ {var}  1")
                .build();

        final FileValidationContext context = prepareContext();
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
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
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsOnly(
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
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsOnly(
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
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(5);
        assertThat(reporter.getReportedProblems()).containsOnly(
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
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(4);
        assertThat(reporter.getReportedProblems()).containsOnly(
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
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
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
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.wasProblemReported()).isFalse();

    }

    @Test
    public void variableWithoutNameIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("@{}=  sth")
                .build();

        final FileValidationContext context = prepareContext();
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
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
        final VariablesTableValidator validator = new VariablesTableValidator(context,
                file.findSection(RobotVariablesSection.class), reporter, createVersionDependentValidators());
        validator.validate(null);

        assertThat(reporter.wasProblemReported()).isFalse();
    }

    private static VersionDependentValidators createVersionDependentValidators(
            final VersionDependentModelUnitValidator... validators) {
        return new VersionDependentValidators() {
            @Override
            public Iterable<VersionDependentModelUnitValidator> getVariableValidators(
                    final FileValidationContext validationContext, final IVariableHolder variable,
                    final ValidationReportingStrategy reporter) {
                return newArrayList(validators);
            }
        };
    }

    private static FileValidationContext prepareContext() {
        return prepareContext(new HashSet<>());
    }

    private static FileValidationContext prepareContext(final Set<String> variables) {
        final ValidationContext parentContext = new ValidationContext(null, new RobotModel(), RobotVersion.from("0.0"),
                SuiteExecutor.Python, ArrayListMultimap.create(), new HashMap<>());
        return new FileValidationContext(parentContext, mock(IFile.class), null, variables);
    }
}
