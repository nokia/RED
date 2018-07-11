/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.newBuiltInKeyword;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.newResourceKeyword;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.prepareContext;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Test;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class GeneralSettingsTableValidatorTest {

    @Test
    public void unknownSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Unknown Setting")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).containsOnly(
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
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).contains(
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
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(30, 32))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(34, 40))));
    }

    @Test
    public void undeclaredVariableAndKeywordInSuiteTeardownAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Teardown  kw  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(33, 35))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(37, 43))));
    }

    @Test
    public void undeclaredVariableAndKeywordInTestSetupAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Setup  kw  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(29, 31))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(33, 39))));
    }

    @Test
    public void undeclaredVariableAndKeywordInTestTeardownAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Teardown  kw  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(32, 34))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(36, 42))));
    }

    @Test
    public void givenTestCaseWithEnvironmentVariable_whenNoMarkersShouldBeReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup    kw   %{PATH}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).isEmpty();
    }

    @Test
    public void declaredVariableAndKeywordInSetupsAndTeardownsAreNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  kw  ${var}")
                .appendLine("Suite Teardown  kw  ${var}")
                .appendLine("Test Setup  kw  ${var}")
                .appendLine("Test Teardown  kw  ${var}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final Set<String> accessibleVariables = newHashSet("${var}");
        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).isEmpty();
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
        final FileValidationContext context = prepareContext(accessibleVariables);
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).containsOnly(
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
    public void variablesSyntaxOfSpecialKeywordsIsReportedInSetupAndTeardowns() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  Set Global Variable     a1   1")
                .appendLine("Test Setup  Set Global Variable      a2   2")
                .appendLine("Suite Teardown  Set Global Variable  a3   3")
                .appendLine("Test Teardown  Set Global Variable   a4   4")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Set Global Variable", "var", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(2, Range.closed(54, 56))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(98, 100))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(4, Range.closed(142, 144))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(5, Range.closed(186, 188))));
    }

    @Test
    public void variablesCreatedInExecutablesAreVisibleInNextOnes() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  Set Global Variable     ${a1}   1")
                .appendLine("Test Setup  Set Global Variable      ${a2}   ${a1}")
                .appendLine("Test Teardown  Set Global Variable   ${a3}   ${a2}")
                .appendLine("Suite Teardown  Set Global Variable  ${a4}   ${a3}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Set Global Variable", "var", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).isEmpty();
    }

    @Test
    public void undeclaredVariableInTagsIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Default Tags  ${var}  ${var1}")
                .appendLine("Force Tags  ${var}  ${var2}")
                .build();

        final Set<String> accessibleVariables = newHashSet("${var}");
        final FileValidationContext context = prepareContext(accessibleVariables);
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).contains(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(39, 46))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(67, 74))));
    }

    @Test
    public void undeclaredKeywordInTemplateIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Template  kw1  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems)
                .contains(new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(2, Range.closed(32, 35))));
    }

    @Test
    public void unexpectedArgumentsInTemplateAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Template  kw  1  2")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).containsOnly(new Problem(GeneralSettingsProblem.SETTING_ARGUMENTS_NOT_APPLICABLE,
                new ProblemPosition(2, Range.closed(17, 30))));
    }

    @Test
    public void declaredKeywordInTemplateIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Template  kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).isEmpty();
    }

    @Test
    public void undeclaredVariableInTimeoutIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Timeout  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).contains(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(2, Range.closed(31, 37))));
    }

    @Test
    public void undefinedTimeFormatInTimeoutIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Timeout  time")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).contains(
                new Problem(ArgumentProblem.INVALID_TIME_FORMAT, new ProblemPosition(2, Range.closed(31, 35))));
    }

    @Test
    public void definedTimeFormatInTimeoutIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Timeout  3 seconds")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).isEmpty();
    }

    @Test
    public void declaredVariableInTimeoutIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Timeout  ${var}")
                .build();

        final Set<String> accessibleVariables = newHashSet("${var}");

        final FileValidationContext context = prepareContext(accessibleVariables);
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).isEmpty();
    }

    @Test
    public void outdatedDocumentationSyntaxIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Document  doc")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).contains(
                new Problem(GeneralSettingsProblem.DOCUMENT_SYNONYM, new ProblemPosition(2, Range.closed(17, 25))));
    }

    @Test
    public void outdatedSetupAndTeardownSyntaxAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Precondition  kw")
                .appendLine("Suite Postcondition  kw")
                .appendLine("Test Precondition  kw")
                .appendLine("Test Postcondition  kw")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, file);

        assertThat(problems).contains(
                new Problem(GeneralSettingsProblem.SUITE_PRECONDITION_SYNONYM,
                        new ProblemPosition(2, Range.closed(17, 35))),
                new Problem(GeneralSettingsProblem.SUITE_POSTCONDITION_SYNONYM,
                        new ProblemPosition(3, Range.closed(40, 59))),
                new Problem(GeneralSettingsProblem.TEST_PRECONDITION_SYNONYM,
                        new ProblemPosition(4, Range.closed(64, 81))),
                new Problem(GeneralSettingsProblem.TEST_POSTCONDITION_SYNONYM,
                        new ProblemPosition(5, Range.closed(86, 104))));
    }

    private Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
            throws CoreException {
        final MockReporter reporter = new MockReporter();
        new GeneralSettingsTableValidator(context, fileModel.findSection(RobotSettingsSection.class), reporter)
                .validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }
}
