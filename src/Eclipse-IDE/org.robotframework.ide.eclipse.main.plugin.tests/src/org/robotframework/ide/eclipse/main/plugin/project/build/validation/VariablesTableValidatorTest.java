/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.collect.Maps;
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
                .appendLine("&{dict}  1")
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

    private static VersionDependentValidators createVersionDependentValidators(
            final VersionDependentModelUnitValidator... validators) {
        return new VersionDependentValidators() {
            @Override
            public Iterable<VersionDependentModelUnitValidator> getVariableValidators(
                    final FileValidationContext validationContext, final IVariableHolder variable,
                    final ProblemsReportingStrategy reporter) {
                return newArrayList(validators);
            }
        };
    }

    private static FileValidationContext prepareContext() {
        final ValidationContext parentContext = new ValidationContext(new RobotModel(), RobotVersion.from("0.0"),
                SuiteExecutor.Python, Maps.<String, LibrarySpecification> newHashMap(),
                Maps.<ReferencedLibrary, LibrarySpecification> newHashMap());
        final FileValidationContext context = new FileValidationContext(parentContext, mock(IFile.class));
        return context;
    }
}
