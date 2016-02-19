/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

public class GeneralSettingsTableValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void unknownSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Unknown Setting")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(GeneralSettingsProblem.UNKNOWN_SETTING, new ProblemPosition(2, Range.closed(17, 32))));
    }

    @Test
    public void emptySettingsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Setup")
                .appendLine("Documentation")
                .appendLine("Metadata")
                .appendLine("Default Tags")
                .appendLine("Test Template")
                .appendLine("Test Timeout")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(6);
        assertThat(reporter.getReportedProblems()).contains(
                new Problem(GeneralSettingsProblem.EMPTY_SETTING, new ProblemPosition(2, Range.closed(17, 27))),
                new Problem(GeneralSettingsProblem.EMPTY_SETTING, new ProblemPosition(3, Range.closed(28, 41))),
                new Problem(GeneralSettingsProblem.EMPTY_SETTING, new ProblemPosition(4, Range.closed(42, 50))),
                new Problem(GeneralSettingsProblem.EMPTY_SETTING, new ProblemPosition(5, Range.closed(51, 63))),
                new Problem(GeneralSettingsProblem.EMPTY_SETTING, new ProblemPosition(6, Range.closed(64, 77))),
                new Problem(GeneralSettingsProblem.EMPTY_SETTING, new ProblemPosition(7, Range.closed(78, 90))));
    }

    @Test
    public void undeclaredVariableAndKeywordInSuiteSetupAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  kw  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(30, 32))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(34, 40))));
    }

    @Test
    public void undeclaredVariableAndKeywordInSuiteTeardownAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Teardown  kw  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(33, 35))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(37, 43))));
    }

    @Test
    public void undeclaredVariableAndKeywordInTestSetupAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Setup  kw  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(29, 31))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(33, 39))));
    }

    @Test
    public void undeclaredVariableAndKeywordInTestTeardownAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Teardown  kw  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(32, 34))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(36, 42))));
    }

    @Test
    public void declaredVariableAndKeywordInSetupsAndTeardownsAreNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  kw  ${var}")
                .appendLine("Suite Teardown  kw  ${var}")
                .appendLine("Test Setup  kw  ${var}")
                .appendLine("Test Teardown  kw  ${var}")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                (Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

        final Set<String> accessibleVariables = new HashSet<>();
        accessibleVariables.add("${var}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
    }

    @Test
    public void test() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Setup  kw")
                .appendLine("Test Template  kw1")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                (Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
    }

    private static KeywordEntity newValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String name, final IPath exposingPath) {
        return new ValidationKeywordEntity(scope, sourceName, name, "", false, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor());
    }

    private static FileValidationContext prepareContext() {
        return prepareContext(new HashMap<String, Collection<KeywordEntity>>());
    }

    private static FileValidationContext prepareContext(final Map<String, Collection<KeywordEntity>> map) {
        return prepareContext(createKeywordsCollector(map), new HashSet<String>());
    }

    private static FileValidationContext prepareContext(final Map<String, Collection<KeywordEntity>> map,
            final Set<String> accessibleVariables) {
        return prepareContext(createKeywordsCollector(map), accessibleVariables);
    }

    private static FileValidationContext prepareContext(final AccessibleKeywordsCollector collector,
            final Set<String> accessibleVariables) {
        final ValidationContext parentContext = new ValidationContext(new RobotModel(), RobotVersion.from("0.0"),
                SuiteExecutor.Python, Maps.<String, LibrarySpecification> newHashMap(),
                Maps.<ReferencedLibrary, LibrarySpecification> newHashMap());
        final IFile file = mock(IFile.class);
        when(file.getFullPath()).thenReturn(new Path("/suite.robot"));
        final FileValidationContext context = new FileValidationContext(parentContext, file, collector,
                accessibleVariables);
        return context;
    }

    private static AccessibleKeywordsCollector createKeywordsCollector(
            final Map<String, Collection<KeywordEntity>> map) {
        return new AccessibleKeywordsCollector() {

            @Override
            public Map<String, Collection<KeywordEntity>> collect() {
                return map;
            }
        };
    }
}
