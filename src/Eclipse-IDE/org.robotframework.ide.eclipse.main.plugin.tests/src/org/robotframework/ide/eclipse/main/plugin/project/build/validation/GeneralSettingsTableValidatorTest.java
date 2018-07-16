/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.newBuiltInKeyword;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.newResourceKeyword;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.prepareContext;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
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

    static final String[] CAUSE_AND_LOCATION = new String[] { "cause", "start", "end" };

    static final String[] ALL = new String[] { "cause", "start", "end", "message" };

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
    
    @Test
    public void noProblemsInNestedKeywordsAreReported_whenUsedProperly_inSuiteSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword", "Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteSetup("Run Keywords", "No Args Kw", "No Args Kw", "No Args Kw"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteSetup("Run Keywords", "Log", "1", "AND", "Log", "1", "AND","Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword If", "cond", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword If", "cond", "Log", "1", "ELSE", "Log", "2"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log", "3"))).isEmpty();
        });
    }
    
    @Test
    public void noProblemsInNestedKeywordsAreReported_whenUsedProperly_inSuiteTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword", "Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keywords", "No Args Kw", "No Args Kw", "No Args Kw"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keywords", "Log", "1", "AND", "Log", "1", "AND","Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword If", "cond", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword If", "cond", "Log", "1", "ELSE", "Log", "2"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log", "3"))).isEmpty();
        });
    }
    
    @Test
    public void noProblemsInNestedKeywordsAreReported_whenUsedProperly_inTestSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keywords", "No Args Kw", "No Args Kw", "No Args Kw"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keywords", "Log", "1", "AND", "Log", "1", "AND","Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log", "1", "ELSE", "Log", "2"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log", "3"))).isEmpty();
        });
    }
    
    @Test
    public void noProblemsInNestedKeywordsAreReported_whenUsedProperly_inTestTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Run Keywords", "No Args Kw", "No Args Kw", "No Args Kw"))).isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Run Keywords", "Log", "1", "AND", "Log", "1", "AND","Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "1", "ELSE", "Log", "2"))).isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log", "3"))).isEmpty();
        });
    }

    @Test
    public void keywordProblemsInNestedKeywordsAreReporter_whenArgumentsAreMissing_inSuiteSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 43, 46));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword", "Run Keyword")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 43, 54));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword", "Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 56, 59));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keywords", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 44, 47));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keywords", "Log", "Log", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 44, 47),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 49, 52),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 54, 57));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keywords", "Log", "1", "AND", "Log", "AND", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 57, 60),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 67, 70));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword If", "cond", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 52, 55));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword If", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 52, 55),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 63, 66));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword If", "cond", "Log", "1", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 66, 69));
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keyword If", "cond", "Log", "ELSE IF", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 52, 55),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 72, 75),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 83, 86));
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 89, 92));
        });
    }

    @Test
    public void keywordProblemsInNestedKeywordsAreReporter_whenArgumentsAreMissing_inSuiteTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 46, 49));
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword", "Run Keyword")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 46, 57));
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword", "Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 59, 62));
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keywords", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 47, 50));
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keywords", "Log", "Log", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 47, 50),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 52, 55),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 57, 60));
            softly.assertThat(
                    problemsOf(context, suiteTeardown("Run Keywords", "Log", "1", "AND", "Log", "AND", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 60, 63),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 70, 73));
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword If", "cond", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 55, 58));
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword If", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 55, 58),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 66, 69));
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword If", "cond", "Log", "1", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 69, 72));
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keyword If", "cond", "Log", "ELSE IF", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 55, 58),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 75, 78),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 86, 89));
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE",
                            "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 92, 95));
        });
    }

    @Test
    public void keywordProblemsInNestedKeywordsAreReporter_whenArgumentsAreMissing_inTestSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 42, 45));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Run Keyword")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 42, 53));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 55, 58));
            softly.assertThat(problemsOf(context, testSetup("Run Keywords", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 43, 46));
            softly.assertThat(problemsOf(context, testSetup("Run Keywords", "Log", "Log", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 43, 46),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 48, 51),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 53, 56));
            softly.assertThat(problemsOf(context, testSetup("Run Keywords", "Log", "1", "AND", "Log", "AND", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 56, 59),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 66, 69));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 51, 54));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 51, 54),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 62, 65));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log", "1", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 65, 68));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "Log", "ELSE IF", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 51, 54),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 71, 74),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 82, 85));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 88, 91));
        });
    }

    @Test
    public void keywordProblemsInNestedKeywordsAreReporter_whenArgumentsAreMissing_inTestTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 45, 48));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Run Keyword")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 45, 56));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 58, 61));
            softly.assertThat(problemsOf(context, testTeardown("Run Keywords", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 46, 49));
            softly.assertThat(problemsOf(context, testTeardown("Run Keywords", "Log", "Log", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 46, 49),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 51, 54),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 56, 59));
            softly.assertThat(
                    problemsOf(context, testTeardown("Run Keywords", "Log", "1", "AND", "Log", "AND", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 59, 62),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 69, 72));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 54, 57));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 54, 57),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 65, 68));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "1", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 68, 71));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "Log", "ELSE IF", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 54, 57),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 74, 77),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 85, 88));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 91, 94));
        });
    }

    @Test
    public void nonExistingVariablesAreReported_inArgumentsNotBelongingToNestedKeyword_inSuiteSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteSetup("Repeat Keyword", "${x}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 46, 50));
            softly.assertThat(
                    problemsOf(context, suiteSetup("Repeat Keyword", "${x}", "Repeat Keyword", "${y}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 46, 50),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 68, 72));
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keyword If", "${x}", "No Args Kw", "ELSE IF", "${y}", "No Args Kw", "ELSE", "Log",
                            "${z}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 46, 50),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 73, 77),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 102, 106));
        });
    }

    @Test
    public void nonExistingVariablesAreReported_inArgumentsNotBelongingToNestedKeyword_inSuiteTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteTeardown("Repeat Keyword", "${x}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 49, 53));
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Repeat Keyword", "${x}", "Repeat Keyword", "${y}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 49, 53),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 71, 75));
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keyword If", "${x}", "No Args Kw", "ELSE IF", "${y}", "No Args Kw", "ELSE",
                            "Log", "${z}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 49, 53),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 76, 80),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 105, 109));
        });
    }

    @Test
    public void nonExistingVariablesAreReported_inArgumentsNotBelongingToNestedKeyword_inTestSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testSetup("Repeat Keyword", "${x}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 45, 49));
            softly.assertThat(
                    problemsOf(context, testSetup("Repeat Keyword", "${x}", "Repeat Keyword", "${y}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 45, 49),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 67, 71));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "${x}", "No Args Kw", "ELSE IF", "${y}", "No Args Kw", "ELSE", "Log",
                            "${z}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 45, 49),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 72, 76),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 101, 105));
        });
    }

    @Test
    public void nonExistingVariablesAreReported_inArgumentsNotBelongingToNestedKeyword_inTestTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testTeardown("Repeat Keyword", "${x}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 48, 52));
            softly.assertThat(problemsOf(context,
                    testTeardown("Repeat Keyword", "${x}", "Repeat Keyword", "${y}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 48, 52),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 70, 74));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "${x}", "No Args Kw", "ELSE IF", "${y}", "No Args Kw", "ELSE",
                            "Log", "${z}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 48, 52),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 75, 79),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 104, 108));
        });
    }

    @Test
    public void nonExistingVariablesAreNotReported_inArgumentsBelongingToNestedKeywordWhichSkipsVarValidation_inSuiteSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Comment", "*msgs"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteSetup("Repeat Keyword", "${x}", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 46, 50));
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keywords", "Comment", "${x}", "AND", "Comment", "${x}", "AND", "Log", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 89, 93));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword If", "cond", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keyword If", "cond", "Comment", "${x}", "ELSE IF", "cond", "Log", "${x}", "ELSE",
                            "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 87, 91));
        });
    }

    @Test
    public void nonExistingVariablesAreNotReported_inArgumentsBelongingToNestedKeywordWhichSkipsVarValidation_inSuiteTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Comment", "*msgs"), newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"), newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context, suiteTeardown("Repeat Keyword", "${x}", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 49, 53));
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keywords", "Comment", "${x}", "AND", "Comment", "${x}", "AND", "Log", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 92, 96));
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword If", "cond", "Comment", "${x}")))
                    .isEmpty();
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keyword If", "cond", "Comment", "${x}", "ELSE IF", "cond", "Log", "${x}",
                            "ELSE", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 90, 94));
        });
    }

    @Test
    public void nonExistingVariablesAreNotReported_inArgumentsBelongingToNestedKeywordWhichSkipsVarValidation_inTestSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Comment", "*msgs"), newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"), newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Repeat Keyword", "${x}", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 45, 49));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keywords", "Comment", "${x}", "AND", "Comment", "${x}", "AND", "Log", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 88, 92));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "Comment", "${x}", "ELSE IF", "cond", "Log", "${x}", "ELSE",
                            "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 86, 90));
        });
    }

    @Test
    public void nonExistingVariablesAreNotReported_inArgumentsBelongingToNestedKeywordWhichSkipsVarValidation_inTestTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Comment", "*msgs"), newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"), newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Repeat Keyword", "${x}", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 48, 52));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keywords", "Comment", "${x}", "AND", "Comment", "${x}", "AND", "Log", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 91, 95));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Comment", "${x}")))
                    .isEmpty();
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "Comment", "${x}", "ELSE IF", "cond", "Log", "${x}", "ELSE",
                            "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 89, 93));
        });
    }

    @Test
    public void variablesSyntaxProblemsAreReported_whenSyntaxCheckingKeywordAreNested_inSuiteSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Variable Should Exist", "var"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 66, 67));
            softly.assertThat(problemsOf(context, suiteSetup("Repeat Keyword", "count", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 76, 77));
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keywords", "Variable Should Exist", "x", "AND", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 67, 68),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 98, 99));
            softly.assertThat(problemsOf(context, suiteSetup("Run Keyword If", "cond", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 75, 76));
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keyword If", "cond", "Variable Should Exist", "x", "ELSE", "Variable Should Exist",
                            "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 75, 76),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 107, 108));
        });
    }

    @Test
    public void variablesSyntaxProblemsAreReported_whenSyntaxCheckingKeywordAreNested_inSuiteTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Variable Should Exist", "var"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, suiteTeardown("Run Keyword", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 69, 70));
            softly.assertThat(
                    problemsOf(context, suiteTeardown("Repeat Keyword", "count", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 79, 80));
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keywords", "Variable Should Exist", "x", "AND", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 70, 71),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 101, 102));
            softly.assertThat(
                    problemsOf(context, suiteTeardown("Run Keyword If", "cond", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 78, 79));
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keyword If", "cond", "Variable Should Exist", "x", "ELSE",
                            "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 78, 79),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 110, 111));
        });
    }

    @Test
    public void variablesSyntaxProblemsAreReported_whenSyntaxCheckingKeywordAreNested_inTestSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Variable Should Exist", "var"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 65, 66));
            softly.assertThat(problemsOf(context, testSetup("Repeat Keyword", "count", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 75, 76));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keywords", "Variable Should Exist", "x", "AND", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 66, 67),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 97, 98));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 74, 75));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "Variable Should Exist", "x", "ELSE", "Variable Should Exist",
                            "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 74, 75),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 106, 107));
        });
    }

    @Test
    public void variablesSyntaxProblemsAreReported_whenSyntaxCheckingKeywordAreNested_inTestTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Variable Should Exist", "var"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 68, 69));
            softly.assertThat(
                    problemsOf(context, testTeardown("Repeat Keyword", "count", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 78, 79));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keywords", "Variable Should Exist", "x", "AND", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 69, 70),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 100, 101));
            softly.assertThat(
                    problemsOf(context, testTeardown("Run Keyword If", "cond", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 77, 78));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "Variable Should Exist", "x", "ELSE",
                            "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 77, 78),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 109, 110));
        });
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inSuiteSetup_1() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  Run Keyword  Set Global Variable  ${a}  1")
                .appendLine("Suite Teardown  Log  ${a}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        assertThat(problemsOf(context, fileModel)).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inSuiteSetup_2() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  Run Keywords  Set Global Variable  ${a}  2  AND  Set Global Variable  ${b}  3")
                .appendLine("Suite Teardown  Log  ${a}, ${b}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        assertThat(problemsOf(context, fileModel)).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inSuiteTeardown_3() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  Run Keyword If  cond  Set Global Variable  ${a}  4")
                .appendLine("Suite Teardown  Log  ${a}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        assertThat(problemsOf(context, fileModel)).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inSuiteTeardown_4() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Settinsg ***")
                .appendLine("Suite Setup  Run Keyword If  cond  Set Global Variable  ${a}  5  ELSE  Set Global Variable  ${b}  6")
                .appendLine("Suite Teardown  Log  ${a}, ${b}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        assertThat(problemsOf(context, fileModel)).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inTestSetup() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Setup  Run Keyword  Set Global Variable  ${a}  1")
                .appendLine("Suite Teardown  Log  ${a}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        assertThat(problemsOf(context, fileModel)).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inTestTeardown() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Teardown  Run Keyword  Set Global Variable  ${a}  1")
                .appendLine("Suite Teardown  Log  ${a}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        assertThat(problemsOf(context, fileModel)).isEmpty();
    }

    @Test
    public void syntaxProblemsInNestedConstructionsAreReported_inSuiteSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE IF", "cond",
                            "No Args Kw")))
                .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 64, 68,
                        "Invalid nested executables syntax. ELSE branch should not be followed by ELSE IF branches"));
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE", "No Args Kw")))
                .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 64, 68,
                            "Invalid nested executables syntax. Multiple ELSE branches are defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 82, 86,
                            "Invalid nested executables syntax. Multiple ELSE branches are defined"));
            softly.assertThat(problemsOf(context,
                    suiteSetup("Run Keyword If", "cond", "No Args Kw", "ELSE IF", "ELSE IF", "cond", "ELSE")))
                .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 64, 71,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 73, 80,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 88, 92,
                            "Invalid nested executables syntax. ELSE branch requires keyword to be defined"));

        });
    }

    @Test
    public void syntaxProblemsInNestedConstructionsAreReported_inSuiteTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE IF", "cond",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 67, 71,
                            "Invalid nested executables syntax. ELSE branch should not be followed by ELSE IF branches"));
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 67, 71,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 85, 89,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"));
            softly.assertThat(problemsOf(context,
                    suiteTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE IF", "ELSE IF", "cond", "ELSE")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 67, 74,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 76, 83,
                                    "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 91, 95,
                                    "Invalid nested executables syntax. ELSE branch requires keyword to be defined"));

        });
    }

    @Test
    public void syntaxProblemsInNestedConstructionsAreReported_inTestSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE IF", "cond",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 63, 67,
                            "Invalid nested executables syntax. ELSE branch should not be followed by ELSE IF branches"));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE", "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 63, 67,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 81, 85,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "No Args Kw", "ELSE IF", "ELSE IF", "cond", "ELSE")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 63, 70,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 72, 79,
                                    "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 87, 91,
                                    "Invalid nested executables syntax. ELSE branch requires keyword to be defined"));

        });
    }

    @Test
    public void syntaxProblemsInNestedConstructionsAreReported_inTestTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE IF", "cond",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 66, 70,
                            "Invalid nested executables syntax. ELSE branch should not be followed by ELSE IF branches"));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE", "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 66, 70,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 84, 88,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE IF", "ELSE IF", "cond", "ELSE")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 66, 73,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 75, 82,
                                    "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 90, 94,
                                    "Invalid nested executables syntax. ELSE branch requires keyword to be defined"));

        });
    }

    private static Collection<Problem> problemsOf(final FileValidationContext context, final RobotSuiteFile fileModel) {
        try {
            return validate(context, fileModel);
        } catch (final CoreException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
            throws CoreException {
        final MockReporter reporter = new MockReporter();
        new GeneralSettingsTableValidator(context, fileModel.findSection(RobotSettingsSection.class), reporter)
                .validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }

    static Tuple problem(final Object... properties) {
        // adding synonym for better readablity
        return tuple(properties);
    }

    private static RobotSuiteFile suiteSetup(final String... callCells) {
        return new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup  " + String.join("  ", callCells))
                .build();
    }

    private static RobotSuiteFile suiteTeardown(final String... callCells) {
        return new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Teardown  " + String.join("  ", callCells))
                .build();
    }

    private static RobotSuiteFile testSetup(final String... callCells) {
        return new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Setup  " + String.join("  ", callCells))
                .build();
    }

    private static RobotSuiteFile testTeardown(final String... callCells) {
        return new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Test Teardown  " + String.join("  ", callCells))
                .build();
    }
}
