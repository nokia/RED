/*
 * Copyright 2016 Nokia Solutions and Networks
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
import org.junit.Ignore;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class TestCaseSettingsValidatorTest {

    @Test
    public void unknownSettingsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [SomeSetting]")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.UNKNOWN_TEST_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 39))));
    }

    @Test
    public void emptyTagsSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Tags]")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 32))));
    }

    @Test
    public void nothingIsReported_whenThereIsATagDefined() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Tags]    tag")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Ignore("see RED-1036")
    @Test
    public void duplicatedTagsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Tags]    tag1")
                .appendLine("  [Tags]    tag2")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 32))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(43, 49))));
    }

    @Test
    public void undeclaredVariableInTestCaseTagsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Tags]    ${var}")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(36, 42))));
    }

    @Test
    public void emptyDocumentationSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Documentation]")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 41))));
    }

    @Test
    public void nothingIsReported_whenThereIsADocumentationDefined() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Documentation]    doc")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Ignore("see RED-1036")
    @Test
    public void duplicatedDocumentationsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Documentation]    tag1")
                .appendLine("  [Documentation]    tag2")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 41))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(52, 67))));
    }

    @Test
    public void outdatedDocumentationSynonymIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Document]    doc1")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.DOCUMENT_SYNONYM, new ProblemPosition(3, Range.closed(26, 36))));
    }

    @Test
    public void emptyTimeoutSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Timeout]")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 35))));
    }

    @Test
    public void nothingIsReported_whenThereIsATimeoutDefined() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Timeout]    1")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Ignore("see RED-1036")
    @Test
    public void duplicatedTimeoutsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Timeout]    1")
                .appendLine("  [Timeout]    2")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 35))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(43, 52))));
    }

    @Test
    public void undeclaredVariableInTestCaseTimeoutIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Timeout]    ${var}")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(39, 45))));
    }

    @Test
    public void invalidSyntaxInTestCaseTimeoutIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Timeout]  something")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_TIME_FORMAT, new ProblemPosition(3, Range.closed(37, 46))));
    }

    @Test
    public void emptyTemplateSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Template]")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 36))));
    }

    @Test
    public void nothingIsReported_whenThereIsATemplateDefined() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Template]    kw1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw1", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void duplicatedTemplatesAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Template]    kw1")
                .appendLine("  [Template]    kw1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw1", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 36))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(46, 56))));
    }

    @Test
    public void undeclaredKeywordInTestCaseTemplateIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Template]  kw")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(38, 40))));
    }

    @Test
    public void unexpectedArgumentsInTestCaseTemplateAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Template]  kw  1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsExactly(new Problem(GeneralSettingsProblem.SETTING_ARGUMENTS_NOT_APPLICABLE,
                new ProblemPosition(3, Range.closed(26, 36))));
    }

    @Test
    public void declaredKeywordInTestTemplateIsNotReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Template]  kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void noneInTestTemplateIsNotReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Template]  None")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void emptySetupSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Setup]")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 33))));
    }

    @Test
    public void nothingIsReported_whenThereIsASetupDefined() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Setup]    keyword")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("keyword", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Ignore("see RED-1036")
    @Test
    public void duplicatedSetupsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Setup]    keyword")
                .appendLine("  [Setup]    keyword")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("keyword", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 33))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(47, 54))));
    }

    @Test
    public void outdatedSetupSynonymIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Precondition]    keyword")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("keyword", new Path("/file.robot")));

        final Collection<Problem> problems = validate(prepareContext(accessibleKws), fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.PRECONDITION_SYNONYM, new ProblemPosition(3, Range.closed(26, 40))));
    }

    @Test
    public void emptyTeardownSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Teardown]")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 36))));
    }

    @Test
    public void nothingIsReported_whenThereIsATeardownDefined() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Teardown]    keyword")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("keyword", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Ignore("see RED-1036")
    @Test
    public void duplicatedTeardownsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Teardown]    keyword")
                .appendLine("  [Teardown]    keyword")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("keyword", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 36))),
                new Problem(TestCasesProblem.DUPLICATED_CASE_SETTING, new ProblemPosition(4, Range.closed(50, 60))));
    }

    @Test
    public void outdatedTeardownSynonymIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Postcondition]    keyword")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("keyword", new Path("/file.robot")));

        final Collection<Problem> problems = validate(prepareContext(accessibleKws), fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.POSTCONDITION_SYNONYM, new ProblemPosition(3, Range.closed(26, 41))));
    }

    private Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
            throws CoreException {
        final MockReporter reporter = new MockReporter();
        final TestCase testCase = fileModel.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getLinkedElement();
        new TestCaseSettingsValidator(context, testCase, reporter).validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }
}
