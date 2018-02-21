/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
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
    public void givenTestCaseWithEnvironmentVariable_whenNoMarkersShouldBeReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup    kw   %{PATH}")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "var");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
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
                new Path("/suite.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final Set<String> accessibleVariables = newHashSet("${var}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
    }

    @Test
    public void declaredVariableAsKeywordInSetupsAndTeardownsIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  ${var}")
                .appendLine("Test Setup  ${var}")
                .appendLine("Suite Teardown  ${var}")
                .appendLine("Test Teardown  ${var}")
                .build();

        final Set<String> accessibleVariables = newHashSet("${var}");

        final FileValidationContext context = prepareContext(new HashMap<>(), accessibleVariables);
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(4);
        assertThat(reporter.getReportedProblems()).contains(
                new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                        new ProblemPosition(2, Range.closed(30, 36))),
                new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                        new ProblemPosition(3, Range.closed(49, 55))),
                new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                        new ProblemPosition(4, Range.closed(72, 78))),
                new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                        new ProblemPosition(5, Range.closed(94, 100))));
    }

    @Test
    public void undeclaredVariableInTagsIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Default Tags  ${var}  ${var1}")
                .appendLine("Force Tags  ${var}  ${var2}")
                .build();

        final Set<String> accessibleVariables = newHashSet("${var}");

        final FileValidationContext context = prepareContext(new HashMap<>(), accessibleVariables);
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).contains(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(39, 46))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(67, 74))));
    }

    @Test
    public void undeclaredKeywordInTemplateIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Template  kw1 ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems())
                .contains(new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(32, 42))));
    }

    @Test
    public void declaredKeywordInTemplateIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Template  kw1 ${arg}")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw1 ${arg}",
                new Path("/suite.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw1 ${arg}",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
    }

    @Test
    public void undeclaredVariableInTimeoutIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Timeout  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).contains(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(31, 37))));
    }

    @Test
    public void undefinedTimeFormatInTimeoutIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Timeout  time")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).contains(
                new Problem(ArgumentProblem.INVALID_TIME_FORMAT, new ProblemPosition(2, Range.closed(31, 35))));
    }

    @Test
    public void definedTimeFormatInTimeoutIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Timeout  3 seconds")
                .build();

        final FileValidationContext context = prepareContext();
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
    }

    @Test
    public void declaredVariableInTimeoutIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Timeout  ${var}")
                .build();

        final Set<String> accessibleVariables = newHashSet("${var}");

        final FileValidationContext context = prepareContext(new HashMap<>(), accessibleVariables);
        final GeneralSettingsTableValidator validator = new GeneralSettingsTableValidator(context,
                file.findSection(RobotSettingsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
    }

    private static KeywordEntity newValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String name, final IPath exposingPath, final String... args) {
        return new ValidationKeywordEntity(scope, sourceName, name, Optional.empty(), false, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor(args));
    }

    private static FileValidationContext prepareContext() {
        return prepareContext(new HashMap<>());
    }

    private static FileValidationContext prepareContext(final Map<String, Collection<KeywordEntity>> accessibleKws) {
        return prepareContext(() -> accessibleKws, new HashSet<>());
    }

    private static FileValidationContext prepareContext(final Map<String, Collection<KeywordEntity>> accessibleKws,
            final Set<String> accessibleVariables) {
        return prepareContext(() -> accessibleKws, accessibleVariables);
    }

    private static FileValidationContext prepareContext(final AccessibleKeywordsCollector collector,
            final Set<String> accessibleVariables) {
        final ValidationContext parentContext = new ValidationContext(new RobotModel(), RobotVersion.from("0.0"),
                SuiteExecutor.Python, ArrayListMultimap.create(), new HashMap<>());
        final IFile file = mock(IFile.class);
        when(file.getFullPath()).thenReturn(new Path("/suite.robot"));
        return new FileValidationContext(parentContext, file, collector, accessibleVariables);
    }
}
