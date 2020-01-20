/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

public class DeprecatedSuiteFileExtensionValidatorTest {

    @Test
    public void validatorIsNotApplicableForVersions31Till32() {
        final DeprecatedSuiteFileExtensionValidator validator = new DeprecatedSuiteFileExtensionValidator(null, null,
                null, null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.0"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.7"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.0"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.0.10"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.1"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.1.5"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.2"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.5"))).isFalse();
    }

    @Test
    public void problemIsReportedInTestsSuiteUsingTsvExtension() {
        final Collection<Problem> problems = validate("tsv", createModel("Test Cases"), RobotCasesSection.class);
        assertThat(problems).containsExactly(new Problem(SuiteFileProblem.DEPRECATED_SUITE_FILE_EXTENSION, 1, 0, 18));
    }

    @Test
    public void problemIsReportedInTestsSuiteUsingTxtExtension() {
        final Collection<Problem> problems = validate("txt", createModel("Test Cases"), RobotCasesSection.class);
        assertThat(problems).containsExactly(new Problem(SuiteFileProblem.DEPRECATED_SUITE_FILE_EXTENSION, 1, 0, 18));
    }

    @Test
    public void problemIsReportedInTasksSuiteUsingTsvExtension() {
        final Collection<Problem> problems = validate("tsv", createModel("Tasks"), RobotTasksSection.class);
        assertThat(problems).containsExactly(new Problem(SuiteFileProblem.DEPRECATED_SUITE_FILE_EXTENSION, 1, 0, 13));
    }

    @Test
    public void problemIsReportedInTasksSuiteUsingTxtExtension() {
        final Collection<Problem> problems = validate("txt", createModel("Tasks"), RobotTasksSection.class);
        assertThat(problems).containsExactly(new Problem(SuiteFileProblem.DEPRECATED_SUITE_FILE_EXTENSION, 1, 0, 13));
    }

    @Test
    public void problemIsNotReportedInTestsSuiteUsingRobotExtension() {
        assertThat(validate("robot", createModel("Test Cases"), RobotCasesSection.class)).isEmpty();
    }

    @Test
    public void problemIsNotReportedInTasksSuiteUsingRobotExtension() {
        assertThat(validate("robot", createModel("Tasks"), RobotTasksSection.class)).isEmpty();
    }

    @Test
    public void problemIsNotReportedInResourceFile() {
        assertThat(validate("robot", createModel("Keywords"), RobotTasksSection.class)).isEmpty();
        assertThat(validate("robot", createModel("Keywords"), RobotCasesSection.class)).isEmpty();
        assertThat(validate("tsv", createModel("Keywords"), RobotTasksSection.class)).isEmpty();
        assertThat(validate("tsv", createModel("Keywords"), RobotCasesSection.class)).isEmpty();
        assertThat(validate("txt", createModel("Keywords"), RobotTasksSection.class)).isEmpty();
        assertThat(validate("txt", createModel("Keywords"), RobotCasesSection.class)).isEmpty();
    }

    private static RobotSuiteFile createModel(final String theOnlyTableHeader) {
        return new RobotSuiteFileCreator(new RobotVersion(3, 1)).appendLine("*** " + theOnlyTableHeader + " ***")
                .build();
    }

    private static Collection<Problem> validate(final String extension, final RobotSuiteFile fileModel,
            final Class<? extends RobotSuiteFileSection> suiteSectionClass) {
        final IFile file = mock(IFile.class);
        when(file.getFileExtension()).thenReturn(extension);
        
        final MockReporter reporter = new MockReporter();
        new DeprecatedSuiteFileExtensionValidator(file, fileModel, suiteSectionClass, reporter).validate();
        
        return reporter.getReportedProblems();
    }
}
