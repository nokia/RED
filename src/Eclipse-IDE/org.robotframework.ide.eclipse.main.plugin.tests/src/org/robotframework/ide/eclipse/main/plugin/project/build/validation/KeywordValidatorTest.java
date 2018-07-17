/*
* Copyright 2018 Nokia Solutions and Networks
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
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;


public class KeywordValidatorTest {

    static final String[] CAUSE_AND_LOCATION = new String[] { "cause", "start", "end" };

    static final String[] ALL = new String[] { "cause", "start", "end", "message" };

    @Test
    public void keywordNamesWithVariablesOnlyAreReported_1() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("${var1}")
                .appendLine("    [Return]  1")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), file);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.VARIABLE_AS_KEYWORD_NAME, new ProblemPosition(2, Range.closed(17, 24))));
    }

    @Test
    public void keywordNamesWithVariablesOnlyAreReported_2() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("@{var1}")
                .appendLine("    [Return]  1")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), file);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.VARIABLE_AS_KEYWORD_NAME, new ProblemPosition(2, Range.closed(17, 24))));
    }

    @Test
    public void keywordNamesWithVariablesOnlyAreReported_3() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("&{var1}")
                .appendLine("    [Return]  1")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), file);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.VARIABLE_AS_KEYWORD_NAME, new ProblemPosition(2, Range.closed(17, 24))));
    }

    @Test
    public void keywordNamesWithVariablesOnlyAreReported_4() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("${var1}${var2}")
                .appendLine("    [Return]  1")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), file);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.VARIABLE_AS_KEYWORD_NAME, new ProblemPosition(2, Range.closed(17, 31))));
    }

    @Test
    public void keywordNamesWithEmbeddedVariablesAreNotReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("${var1}_part")
                .appendLine("    kw  11")
                .appendLine("part_${var2}")
                .appendLine("    kw  22")
                .appendLine("${var3}_part_${var4}")
                .appendLine("    kw  33")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void keywordDefinitionWithDotsIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword.1")
                .appendLine("    kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.KEYWORD_NAME_WITH_DOTS, new ProblemPosition(2, Range.closed(17, 26))));
    }

    @Test
    public void keywordOverridingOtherImportedKeywordIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newResourceKeyword("keyword", new Path("/res.robot")),
                newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.KEYWORD_MASKS_OTHER_KEYWORD, new ProblemPosition(2, Range.closed(17, 24))));
    }

    @Test
    public void emptyKeywordIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD, new ProblemPosition(2, Range.closed(17, 24))));
    }

    @Test
    public void emptyKeywordIsReported_whenThereIsAnEmptyReturn() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Return]")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems)
                .contains(new Problem(KeywordsProblem.EMPTY_KEYWORD, new ProblemPosition(2, Range.closed(17, 24))));
    }

    @Test
    public void emptyKeywordIsReported_whenThereIsACommentedLineOnly() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  # kw")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD, new ProblemPosition(2, Range.closed(17, 24))));
    }

    @Test
    public void nothingIsReported_whenThereIsNonEmptyReturn() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Return]  42")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenThereIsAnExecutableRow() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void keywordOccurrenceWithDotsAndSourceIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    res.k.w")
                .appendLine("    res1.kw")
                .appendLine("    res.kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newResourceKeyword("k.w", new Path("/res.robot")),
                newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
                        new ProblemPosition(3, Range.closed(29, 36))),
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(4, Range.closed(41, 48))));
    }

    @Test
    public void undeclaredVariableInExecutableRowIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  kw  ${var}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newResourceKeyword("kw", new Path("/res.robot"), "arg"));

        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(31, 37))));
    }

    @Test
    public void nothingIsReported_whenExecutableRowContainsUndeclaredVariablesButInSpecialKeyword()
            throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  Comment  ${x}  ${y}  ${z}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Comment", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).isEmpty();
    }

    @Test
    public void undeclaredKeywordInExecutableRowIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  kw  1  2")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(27, 29))));
    }

    @Test
    public void keywordProblemsInExecutableRowAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  kw  1  2")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(3, Range.closed(27, 29))));
    }

    @Test
    public void variableSyntaxProblemIsReportedInExecutableRowWithSpecialKeyword() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  Set Global Variable  x  1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Set Global Variable", "var", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(48, 49))));
    }

    @Test
    public void undeclaredVariableInTeardownIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]  kw  ${var}")
                .appendLine("  [Return]  42")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(43, 49))));
    }

    @Test
    public void nothingIsReported_whenTeardownContainsUndeclaredVariablesButInSpecialKeyword() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]  Comment  ${var}")
                .appendLine("  [Return]  42")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Comment", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void undeclaredKeywordInTeardownIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]  kw  1  2")
                .appendLine("  [Return]  42")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(39, 41))));
    }

    @Test
    public void keywordProblemsInTeardownAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]  kw  1  2")
                .appendLine("  [Return]  42")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(3, Range.closed(39, 41))));
    }

    @Test
    public void variableSyntaxProblemIsReportedInSpecialKeywords() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]  Set Global Variable  x  1")
                .appendLine("  [Return]  42")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Set Global Variable", "var", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(60, 61))));
    }

    @Test
    public void declaredVariableAsKeywordInTeardownIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]  ${var}   1")
                .appendLine("  [Return]  42")
                .build();

        final Set<String> accessibleVariables = newHashSet("${var}");
        final FileValidationContext context = prepareContext(accessibleVariables);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                new ProblemPosition(3, Range.closed(39, 45))));
    }

    @Test
    public void undeclaredVariableInForLoopRowIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  :FOR  ${x}  IN RANGE  1  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(52, 58))));
    }

    @Test
    public void invalidForLoopRowSyntaxIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  :FOR  ${x}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.INVALID_FOR_KEYWORD, new ProblemPosition(3, Range.closed(27, 31))));
    }

    @Test
    public void undeclaredVariableInForContinueExecutableRowIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  :FOR  ${x}  IN RANGE  1  2")
                .appendLine("  \\  kw  ${var}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));

        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(4, Range.closed(63, 69))));
    }

    @Test
    public void nothingIsReported_whenForContinueExecutableRowContainsUndeclaredVariablesButInSpecialKeyword()
            throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  :FOR  ${x}  IN RANGE  1  2")
                .appendLine("  \\  Comment  ${x}  ${y}  ${z}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Comment", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).isEmpty();
    }

    @Test
    public void undeclaredKeywordInForContinueExecutableRowIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  :FOR  ${x}  IN RANGE  1  2")
                .appendLine("  \\  kw  1  2")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(4, Range.closed(59, 61))));
    }

    @Test
    public void keywordProblemsInForContinueExecutableRowAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  :FOR  ${x}  IN RANGE  1  2")
                .appendLine("  \\  kw  1  2")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(4, Range.closed(59, 61))));
    }

    @Test
    public void variableSyntaxProblemIsReportedInForContinueExecutableRowWithSpecialKeyword() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  :FOR  ${x}  IN RANGE  1  2")
                .appendLine("  \\  Set Global Variable  x  1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Set Global Variable", "var", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(4, Range.closed(80, 81))));
    }

    @Test
    public void undeclaredVariableInReturnIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Return]  ${var}")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(37, 43))));
    }

    @Test
    public void nothingIsReported_whenKeywordUsesEnvironmentVariables() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("Key")
                .appendLine("    [Arguments]  ${arg}=%{HOME}")
                .appendLine("    [Teardown]    kw    %{PATH}")
                .appendLine("    kw    %{PATH}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenArgumentsArePassedInEmbeddedWay() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword using ${var_a} and ${var_b}")
                .appendLine("    use  ${var_a}  ${var_b}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newResourceKeyword("use", new Path("/res.robot"), "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedFromLineToLine() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    [Return]  ${a}  ${b}  ${c}")
                .appendLine("    [Teardown]  use  ${a}  ${b}  ${c}")
                .appendLine("    use  ${arg1}  ${arg2}  ${varargs}  ${kwargs}")
                .appendLine("    ${a}=  kw")
                .appendLine("    use  ${a}")
                .appendLine("    ${b}  ${c}=  kw")
                .appendLine("    use  ${a}  ${b}  ${c}")
                .appendLine("    :FOR  ${d}  IN RANGE  ${a}  ${b}")
                .appendLine("    \\  use  ${a}  ${b}  ${c}  ${d}")
                .appendLine("    \\  ${e}=  kw")
                .appendLine("    \\  use  ${a}  ${b}  ${c}  ${d}  ${e}")
                .appendLine("    use  ${a}  ${b}  ${c}  ${d}  ${e}")
                .appendLine("    [Arguments]  ${arg1}  ${arg2}=def  @{varargs}  &{kwargs}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newResourceKeyword("kw", new Path("/res.robot")),
                newResourceKeyword("use", new Path("/res.robot"), "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedFromLineToLineUsingSpecialKeywords() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    [Return]  ${a}  ${b}  ${c}")
                .appendLine("    [Teardown]  use  ${a}  ${b}  ${c}")
                .appendLine("    use  ${arg1}  ${arg2}  ${varargs}  ${kwargs}")
                .appendLine("    Set Global Variable  ${a}  1")
                .appendLine("    use  ${a}")
                .appendLine("    Set Suite Variable  ${b}  2")
                .appendLine("    use  ${a}  ${b}")
                .appendLine("    Set Test Variable  ${c}  3")
                .appendLine("    use  ${a}  ${b}  ${c}")
                .appendLine("    :FOR  ${d}  IN RANGE  ${a}  ${b}")
                .appendLine("    \\  use  ${a}  ${b}  ${c}  ${d}")
                .appendLine("    \\  Set Suite Variable  ${e}  4")
                .appendLine("    \\  use  ${a}  ${b}  ${c}  ${d}  ${e}")
                .appendLine("    use  ${a}  ${b}  ${c}  ${d}  ${e}")
                .appendLine("    [Arguments]  ${arg1}  ${arg2}=def  @{varargs}  &{kwargs}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Set Global Variable", "var", "*values"),
                newBuiltInKeyword("Set Suite Variable", "var", "*values"),
                newBuiltInKeyword("Set Test Variable", "var", "*values"),
                newResourceKeyword("use", new Path("/res.robot"), "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesInGeneralSettingsSetups() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    use  ${a}")
                .appendLine("*** Settings ***")
                .appendLine("Suite Setup    Set Global Variable    ${a}    1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Set Global Variable", "var", "*values"),
                newResourceKeyword("use", new Path("/res.robot"), "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }
    
    @Test
    public void noProblemsInNestedKeywordsAreReported_whenUsedProperly() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keywords", "No Args Kw", "No Args Kw", "No Args Kw"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keywords", "Log", "1", "AND", "Log", "1", "AND","Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log", "1", "ELSE", "Log", "2"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log", "3"))).isEmpty();
        });
    }
    
    @Test
    public void noProblemsInNestedKeywordsAreReported_whenUsedProperly_inKeywordTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword", "Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keywords", "No Args Kw", "No Args Kw", "No Args Kw"))).isEmpty();
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keywords", "Log", "1", "AND", "Log", "1", "AND","Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword If", "cond", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword If", "cond", "Log", "1", "ELSE", "Log", "2"))).isEmpty();
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log", "3"))).isEmpty();
        });
    }

    @Test
    public void keywordProblemsInNestedKeywordsAreReporter_whenArgumentsAreMissing() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 44, 47));
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Run Keyword")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 44, 55));
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 59, 62));
            softly.assertThat(problemsOf(context, theCall("Run Keywords", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 45, 48));
            softly.assertThat(problemsOf(context, theCall("Run Keywords", "Log", "Log", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 45, 48),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 52, 55),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 59, 62));
            softly.assertThat(problemsOf(context, theCall("Run Keywords", "Log", "1", "AND", "Log", "AND", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 64, 67),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 78, 81));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 55, 58));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 55, 58),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 70, 73));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log", "1", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 75, 78));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "Log", "ELSE IF", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 55, 58),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 81, 84),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 96, 99));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 106, 109));
        });
    }

    @Test
    public void keywordProblemsInNestedKeywordsAreReporter_whenArgumentsAreMissing_inKeywordTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 58, 61));
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword", "Run Keyword")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 58, 69));
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword", "Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 73, 76));
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keywords", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 59, 62));
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keywords", "Log", "Log", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 59, 62),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 66, 69),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 73, 76));
            softly.assertThat(
                    problemsOf(context, keywordTeardown("Run Keywords", "Log", "1", "AND", "Log", "AND", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 78, 81),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 92, 95));
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword If", "cond", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 69, 72));
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword If", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 69, 72),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 84, 87));
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword If", "cond", "Log", "1", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 89, 92));
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keyword If", "cond", "Log", "ELSE IF", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 69, 72),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 95, 98),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 110, 113));
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE",
                            "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 120, 123));
        });
    }

    @Test
    public void nonExistingVariablesAreReported_inArgumentsNotBelongingToNestedKeyword() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Repeat Keyword", "${x}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 47, 51));
            softly.assertThat(
                    problemsOf(context, theCall("Repeat Keyword", "${x}", "Repeat Keyword", "${y}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 47, 51),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 73, 77));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "${x}", "No Args Kw", "ELSE IF", "${y}", "No Args Kw", "ELSE", "Log",
                            "${z}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 47, 51),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 80, 84),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 117, 121));
        });
    }

    @Test
    public void nonExistingVariablesAreReported_inArgumentsNotBelongingToNestedKeyword_inKeywordTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, keywordTeardown("Repeat Keyword", "${x}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 61, 65));
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Repeat Keyword", "${x}", "Repeat Keyword", "${y}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 61, 65),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 87, 91));
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keyword If", "${x}", "No Args Kw", "ELSE IF", "${y}", "No Args Kw", "ELSE",
                            "Log", "${z}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 61, 65),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 94, 98),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 131, 135));
        });
    }

    @Test
    public void nonExistingVariablesAreNotReported_inArgumentsBelongingToNestedKeywordWhichSkipsVarValidation() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Comment", "*msgs"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Repeat Keyword", "${x}", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 47, 51));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keywords", "Comment", "${x}", "AND", "Comment", "${x}", "AND", "Log", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 104, 108));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "Comment", "${x}", "ELSE IF", "cond", "Log", "${x}", "ELSE",
                            "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 100, 104));
        });
    }

    @Test
    public void nonExistingVariablesAreNotReported_inArgumentsBelongingToNestedKeywordWhichSkipsVarValidation_inKeywordTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Comment", "*msgs"), newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"), newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context, keywordTeardown("Repeat Keyword", "${x}", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 61, 65));
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keywords", "Comment", "${x}", "AND", "Comment", "${x}", "AND", "Log", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 118, 122));
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword If", "cond", "Comment", "${x}")))
                    .isEmpty();
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keyword If", "cond", "Comment", "${x}", "ELSE IF", "cond", "Log", "${x}",
                            "ELSE", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 114, 118));
        });
    }

    @Test
    public void variablesSyntaxProblemsAreReported_whenSyntaxCheckingKeywordAreNested() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Variable Should Exist", "var"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 69, 70));
            softly.assertThat(problemsOf(context, theCall("Repeat Keyword", "count", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 81, 82));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keywords", "Variable Should Exist", "x", "AND", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 70, 71),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 107, 108));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 80, 81));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "Variable Should Exist", "x", "ELSE", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 80, 81),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 118, 119));
        });
    }

    @Test
    public void variablesSyntaxProblemsAreReported_whenSyntaxCheckingKeywordAreNested_inKeywordTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Variable Should Exist", "var"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, keywordTeardown("Run Keyword", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 83, 84));
            softly.assertThat(
                    problemsOf(context, keywordTeardown("Repeat Keyword", "count", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 95, 96));
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keywords", "Variable Should Exist", "x", "AND", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 84, 85),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 121, 122));
            softly.assertThat(
                    problemsOf(context, keywordTeardown("Run Keyword If", "cond", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 94, 95));
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keyword If", "cond", "Variable Should Exist", "x", "ELSE",
                            "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 94, 95),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 132, 133));
        });
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    Run Keyword  Set Global Variable  ${a}  1")
                .appendLine("    Log  ${a}")
                .appendLine("    Run Keywords  Set Global Variable  ${b}  2  AND  Set Global Variable  ${c}  3")
                .appendLine("    Log  ${b}, ${c}")
                .appendLine("    Run Keyword If  cond  Set Global Variable  ${d}  4")
                .appendLine("    Log  ${d}")
                .appendLine("    Run Keyword If  cond  Set Global Variable  ${e}  5  ELSE  Set Global Variable  ${f}  6")
                .appendLine("    Log  ${e}, ${f}")
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
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inKeywordTeardown_1() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    [Teardown]  Run Keyword  Set Global Variable  ${a}  1")
                .appendLine("    [Return]  ${a}")
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
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inKeywordTeardown_2() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    [Teardown]  Run Keywords  Set Global Variable  ${a}  2  AND  Set Global Variable  ${b}  3")
                .appendLine("    [Return]  ${a}, ${b}")
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
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inKeywordTeardown_3() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    [Teardown]  Run Keyword If  cond  Set Global Variable  ${a}  4")
                .appendLine("    [Return]  ${a}")
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
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inKeywordTeardown_4() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    [Teardown]  Run Keyword If  cond  Set Global Variable  ${a}  5  ELSE  Set Global Variable  ${b}  6")
                .appendLine("    [Return]  ${a}, ${b}")
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
    public void syntaxProblemsInNestedConstructionsAreReported() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE IF", "cond", "No Args Kw")))
                .extracting(ALL)
                .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 69, 73,
                        "Invalid nested executables syntax. ELSE branch should not be followed by ELSE IF branches"));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE", "No Args Kw")))
                .extracting(ALL)
                .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 69, 73,
                            "Invalid nested executables syntax. Multiple ELSE branches are defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 91, 95,
                            "Invalid nested executables syntax. Multiple ELSE branches are defined"));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "No Args Kw", "ELSE IF", "ELSE IF", "cond", "ELSE")))
                .extracting(ALL)
                .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 69, 76,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 80, 87,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 99, 103,
                            "Invalid nested executables syntax. ELSE branch requires keyword to be defined"));

        });
    }

    @Test
    public void syntaxProblemsInNestedConstructionsAreReported_inKeywordTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE IF", "cond",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 83, 87,
                            "Invalid nested executables syntax. ELSE branch should not be followed by ELSE IF branches"));
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 83, 87,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 105, 109,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"));
            softly.assertThat(problemsOf(context,
                    keywordTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE IF", "ELSE IF", "cond", "ELSE")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 83, 90,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 94, 101,
                                    "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 113, 117,
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
        final UserKeyword keyword = fileModel.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getLinkedElement();
        final MockReporter reporter = new MockReporter();
        new KeywordValidator(context, keyword, reporter).validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }

    static Tuple problem(final Object... properties) {
        // adding synonym for better readablity
        return tuple(properties);
    }

    private static RobotSuiteFile theCall(final String... callCells) {
        return new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    " + String.join("    ", callCells))
                .build();
    }

    private static RobotSuiteFile keywordTeardown(final String... callCells) {
        return new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("    [Teardown]    " + String.join("    ", callCells))
                .appendLine("    [Return]    1")
                .build();
    }
}
