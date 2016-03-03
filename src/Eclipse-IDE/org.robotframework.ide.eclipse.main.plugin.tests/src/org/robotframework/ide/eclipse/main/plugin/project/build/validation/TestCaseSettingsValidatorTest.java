/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

public class TestCaseSettingsValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void unkownSettingsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [SomeSetting]")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.UNKNOWN_TEST_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 39))));
    }

    @Test
    public void emptyTagsSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Tags]")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 32))));
    }

    @Test
    public void nothingIsReported_whenThereIsATagDefined() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Tags]    tag")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Ignore
    @Test
    public void duplicatedTagsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Tags]    tag1")
                .appendLine("  [Tags]    tag2")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 32))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(43, 49))));
    }

    @Test
    public void emptyDocumentationSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Documentation]")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 41))));
    }

    @Test
    public void nothingIsReported_whenThereIsADocuDefined() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Documentation]    doc")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Ignore
    @Test
    public void duplicatedDocumentationsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Documentation]    tag1")
                .appendLine("  [Documentation]    tag2")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 41))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(52, 67))));
    }

    @Test
    public void emptyTimeoutSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Timeout]")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 35))));
    }

    @Test
    public void nothingIsReported_whenThereIsATimeoutDefined() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Timeout]    1")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Ignore
    @Test
    public void duplicatedTimeoutsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Timeout]    1")
                .appendLine("  [Timeout]    2")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 35))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(43, 52))));
    }

    @Test
    public void emptyTemplateSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Template]")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 36))));
    }

    @Test
    public void nothingIsReported_whenThereIsATemplateDefined() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Template]    Log")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void duplicatedTemplatesAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Template]    Log")
                .appendLine("  [Template]    Log")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 36))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(46, 56))));
    }

    @Test
    public void emptySetupSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Setup]")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 33))));
    }

    @Test
    public void nothingIsReported_whenThereIsASetupDefined() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Setup]    keyword")
                .build();

        final Map<String, Collection<KeywordEntity>> keywords = new HashMap<>();
        keywords.put("keyword",
                newArrayList(newValidationKeywordEntity(KeywordScope.LOCAL, "file", "keyword", new Path("file"))));
        final FileValidationContext context = prepareContext(keywords);
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Ignore
    @Test
    public void duplicatedSetupsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Setup]    keyword")
                .appendLine("  [Setup]    keyword")
                .build();

        final Map<String, Collection<KeywordEntity>> keywords = new HashMap<>();
        keywords.put("keyword",
                newArrayList(newValidationKeywordEntity(KeywordScope.LOCAL, "file", "keyword", new Path("file"))));
        final FileValidationContext context = prepareContext(keywords);
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 33))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(49, 56))));
    }

    @Test
    public void setupArgumentsProblemsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Setup]    keyword")
                .build();

        final Map<String, Collection<KeywordEntity>> keywords = new HashMap<>();
        keywords.put("keyword",
                newArrayList(newValidationKeywordEntity(KeywordScope.LOCAL, "file", "keyword", new Path("file"), "x")));
        final FileValidationContext context = prepareContext(keywords);
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(new Problem(
                ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, new ProblemPosition(3, Range.closed(37, 44))));
    }

    @Test
    public void emptyTeardownSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Teardown]")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 36))));
    }

    @Test
    public void nothingIsReported_whenThereIsATeardownDefined() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Teardown]    keyword")
                .build();

        final Map<String, Collection<KeywordEntity>> keywords = new HashMap<>();
        keywords.put("keyword",
                newArrayList(newValidationKeywordEntity(KeywordScope.LOCAL, "file", "keyword", new Path("file"))));
        final FileValidationContext context = prepareContext(keywords);
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Ignore
    @Test
    public void duplicatedTeardownsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Teardown]    keyword")
                .appendLine("  [Teardown]    keyword")
                .build();

        final Map<String, Collection<KeywordEntity>> keywords = new HashMap<>();
        keywords.put("keyword",
                newArrayList(newValidationKeywordEntity(KeywordScope.LOCAL, "file", "keyword", new Path("file"))));
        final FileValidationContext context = prepareContext(keywords);
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 36))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(52, 62))));
    }

    @Test
    public void teardownArgumentsProblemsAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Teardown]    keyword")
                .build();

        final Map<String, Collection<KeywordEntity>> keywords = new HashMap<>();
        keywords.put("keyword",
                newArrayList(newValidationKeywordEntity(KeywordScope.LOCAL, "file", "keyword", new Path("file"), "x")));
        final FileValidationContext context = prepareContext(keywords);
        final TestCaseSettingsValidator validator = new TestCaseSettingsValidator(context, getTestCase(file), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).containsOnly(new Problem(
                ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, new ProblemPosition(3, Range.closed(40, 47))));
    }

    private static KeywordEntity newValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String name, final IPath exposingPath, final String... args) {
        return new ValidationKeywordEntity(scope, sourceName, name, "", false, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor(args));
    }

    private static TestCase getTestCase(final RobotSuiteFile file) {
        final Optional<RobotCasesSection> section = file.findSection(RobotCasesSection.class);
        return section.get().getChildren().get(0).getLinkedElement();
    }

    private static FileValidationContext prepareContext() {
        return prepareContext(new HashMap<String, Collection<KeywordEntity>>());
    }

    private static FileValidationContext prepareContext(final Map<String, Collection<KeywordEntity>> map) {
        return prepareContext(createKeywordsCollector(map));
    }

    private static FileValidationContext prepareContext(final AccessibleKeywordsCollector collector) {
        final ValidationContext parentContext = new ValidationContext(new RobotModel(), RobotVersion.from("0.0"),
                SuiteExecutor.Python, Maps.<String, LibrarySpecification> newHashMap(),
                Maps.<ReferencedLibrary, LibrarySpecification> newHashMap());
        final FileValidationContext context = new FileValidationContext(parentContext, mock(IFile.class), collector,
                new HashSet<String>());
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
