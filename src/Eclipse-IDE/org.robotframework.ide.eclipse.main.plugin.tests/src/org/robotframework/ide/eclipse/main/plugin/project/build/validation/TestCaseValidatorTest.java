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


public class TestCaseValidatorTest {

    static final String[] CAUSE_AND_LOCATION = new String[] { "cause", "start", "end" };

    static final String[] ALL = new String[] { "cause", "start", "end", "message" };

    @Test
    public void emptyNameIsReported_1() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("")
                .appendLine("  kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_NAME, new ProblemPosition(2, Range.closed(19, 19))));
    }

    @Test
    public void emptyNameIsReported_2() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("  kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_NAME, new ProblemPosition(2, Range.closed(19, 19))));
    }

    @Test
    public void emptyNameIsReported_inTsvFile() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("    ")
                .appendLine("\tkw")
                .buildTsv();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.EMPTY_CASE_NAME, new ProblemPosition(2, Range.closed(19, 19))));
    }

    @Test
    public void emptyTestCaseIsReported_1() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems)
                .containsOnly(new Problem(TestCasesProblem.EMPTY_CASE, new ProblemPosition(2, Range.closed(19, 23))));
    }

    @Test
    public void emptyTestCaseIsReported_2() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  # comment")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems)
                .containsOnly(new Problem(TestCasesProblem.EMPTY_CASE, new ProblemPosition(2, Range.closed(19, 23))));
    }

    @Test
    public void nothingIsReported_whenThereIsAnExecutableRow() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  kw")
                .appendLine("  kw  ${2}")
                .appendLine("  kw  ${var-2}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newResourceKeyword("kw", new Path("/res.robot"), "*values"));
        final Set<String> accessibleVars = newHashSet("${var}");
        final FileValidationContext context = prepareContext(accessibleKws, accessibleVars);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void undeclaredVariableInExecutableRowIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  kw  ${var}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(30, 36))));
    }

    @Test
    public void nothingIsReported_whenExecutableRowContainsUndeclaredVariablesButInSpecialKeyword()
            throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  Comment  ${a}  ${b}  ${c}")
                .appendLine("  Get Variable Value  ${d}  1")
                .appendLine("  Set Global Variable  ${e}  2")
                .appendLine("  Set Suite Variable  ${e}  3")
                .appendLine("  Set Test Variable  ${e}  4")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Comment", "*values"),
                newBuiltInKeyword("Get Variable Value", "var", "def"),
                newBuiltInKeyword("Set Global Variable", "var", "*values"),
                newBuiltInKeyword("Set Suite Variable", "var", "*values"),
                newBuiltInKeyword("Set Test Variable", "var", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void undeclaredKeywordInExecutableRowIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  kw  1  2")
                .build();

        final FileValidationContext context = prepareContext();

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(26, 28))));
    }

    @Test
    public void keywordProblemsInExecutableRowAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  kw  1  2")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(3, Range.closed(26, 28))));
    }

    @Test
    public void variableSyntaxProblemIsReportedInExecutableRowWithSpecialKeyword() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  Get Variable Value  x  1")
                .appendLine("  Get Variable Value  %{x}  2")
                .appendLine("  Set Global Variable  x  3")
                .appendLine("  Set Global Variable  %{x}  4")
                .appendLine("  Set Suite Variable  x  5")
                .appendLine("  Set Suite Variable  %{x}  6")
                .appendLine("  Set Test Variable  x  7")
                .appendLine("  Set Test Variable  %{x}  8")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Get Variable Value", "var", "def"),
                newBuiltInKeyword("Set Global Variable", "var", "*values"),
                newBuiltInKeyword("Set Suite Variable", "var", "*values"),
                newBuiltInKeyword("Set Test Variable", "var", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(46, 47))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(4, Range.closed(73, 77))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(5, Range.closed(104, 105))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(6, Range.closed(132, 136))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(7, Range.closed(162, 163))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(8, Range.closed(189, 193))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(9, Range.closed(218, 219))),
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(10, Range.closed(244, 248))));
    }

    @Test
    public void undeclaredVariableInTestSetupIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  kw  ${var}")
                .appendLine("  kw  1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(39, 45))));
    }


    @Test
    public void nothingIsReported_whenTestSetupContainsUndeclaredVariablesButInSpecialKeyword()
            throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  Comment  ${a}  ${b}  ${c}")
                .appendLine("  Comment")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Comment", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }


    @Test
    public void undeclaredKeywordInTestSetupRowIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  kw  1  2")
                .appendLine("  kw2")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw2", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(35, 37))));
    }

    @Test
    public void keywordProblemsInTestSetupAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  kw  1  2")
                .appendLine("  kw  1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(3, Range.closed(35, 37))));
    }

    @Test
    public void variableSyntaxProblemIsReportedInTestSetupWithSpecialKeyword() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  Get Variable Value  %{x}  2")
                .appendLine("  kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Get Variable Value", "var", "def"),
                newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(55, 59))));
    }

    @Test
    public void declaredVariableAsKeywordInSetupIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  ${var}   1")
                .appendLine("  kw")
                .build();

        final Collection<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final Set<String> accessibleVariables = newHashSet("${var}");
        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                new ProblemPosition(3, Range.closed(35, 41))));
    }

    @Test
    public void undeclaredVariableInTestTeardownIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Teardown]  kw  ${var}")
                .appendLine("  kw  1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(42, 48))));
    }

    @Test
    public void nothingIsReported_whenTestTeardownContainsUndeclaredVariablesButInSpecialKeyword()
            throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Teardown]  Comment  ${a}  ${b}  ${c}")
                .appendLine("  Comment")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Comment", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void undeclaredKeywordInTestTeardownIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Teardown]  kw  1  2")
                .appendLine("  kw2")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw2", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(38, 40))));
    }

    @Test
    public void keywordProblemsInTestTeardownAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Teardown]  kw  1  2")
                .appendLine("  kw  1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(3, Range.closed(38, 40))));
    }

    @Test
    public void variableSyntaxProblemIsReportedInTestTeardownWithSpecialKeyword() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Teardown]  Get Variable Value  %{x}  2")
                .appendLine("  kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Get Variable Value", "var", "def"),
                newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(58, 62))));
    }

    @Test
    public void declaredVariableAsKeywordInTeardownIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Teardown]  ${var}   1")
                .appendLine("  kw")
                .build();

        final Collection<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final Set<String> accessibleVariables = newHashSet("${var}");
        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).containsOnly(new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                new ProblemPosition(3, Range.closed(38, 44))));
    }

    @Test
    public void undeclaredVariableInForLoopRowIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  :FOR  ${x}  IN RANGE  1  ${var}")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(51, 57))));
    }

    @Test
    public void invalidForLoopRowSyntaxIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  :FOR  ${x}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.INVALID_FOR_KEYWORD, new ProblemPosition(3, Range.closed(26, 30))));
    }

    @Test
    public void nothingIsReported_whenForContinueExecutableRowContainsUndeclaredVariablesButInSpecialKeyword()
            throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
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
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  :FOR  ${x}  IN RANGE  1  2")
                .appendLine("  \\  kw  1  2")
                .build();

        final FileValidationContext context = prepareContext();
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(4, Range.closed(58, 60))));
    }

    @Test
    public void keywordProblemsInForContinueExecutableRowAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  :FOR  ${x}  IN RANGE  1  2")
                .appendLine("  \\  kw  1  2")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(4, Range.closed(58, 60))));
    }

    @Test
    public void variableSyntaxProblemIsReportedInForContinueExecutableRowWithSpecialKeyword() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  :FOR  ${x}  IN RANGE  1  2")
                .appendLine("  \\  Set Global Variable  x  1")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newBuiltInKeyword("Set Global Variable", "var", "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);
        final Collection<Problem> problems = validate(context, fileModel);

        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(4, Range.closed(79, 80))));
    }

    @Test
    public void nothingIsReported_whenKeywordUsesEnvironmentVariables() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    [Setup]  kw  %{foobar}")
                .appendLine("    [Teardown]    kw    %{PATH}")
                .appendLine("    [Tags]    tag_with_%{HOME}")
                .appendLine("    kw    %{bar}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot"), "arg"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedFromLineToLine() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    ${a}=  kw")
                .appendLine("    use  ${a}")
                .appendLine("    ${b}  ${c}=  kw")
                .appendLine("    use  ${a}  ${b}  ${c}")
                .appendLine("    :FOR  ${d}  IN RANGE  ${a}  ${b}")
                .appendLine("    \\  use  ${a}  ${b}  ${c}  ${d}")
                .appendLine("    \\  ${e}=  kw")
                .appendLine("    \\  use  ${a}  ${b}  ${c}  ${d}  ${e}")
                .appendLine("    use  ${a}  ${b}  ${c}  ${d}  ${e}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")),
                newResourceKeyword("use", new Path("/res.robot"), "*values"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedFromLineToLineUsingSpecialKeywords() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    [Setup]  Set Suite Variable  ${a}")
                .appendLine("    [Teardown]  Set Suite Variable  ${g}  ${f}")
                .appendLine("    use  ${a}")
                .appendLine("    Set Global Variable  ${b}  1")
                .appendLine("    use  ${a}  ${b}")
                .appendLine("    Set Suite Variable  ${c}  2")
                .appendLine("    use  ${a}  ${b}  ${c}")
                .appendLine("    Set Test Variable  ${d}  3")
                .appendLine("    use  ${a}  ${b}  ${c}  ${d}")
                .appendLine("    :FOR  ${e}  IN RANGE  ${a}  ${b}")
                .appendLine("    \\  use  ${a}  ${b}  ${c}  ${d}  ${e}")
                .appendLine("    \\  Set Suite Variable  ${f}  4")
                .appendLine("    \\  use  ${a}  ${b}  ${c}  ${d}  ${e}  ${f}")
                .appendLine("    use  ${a}  ${b}  ${c}  ${d}  ${e}  ${f}")
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
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    use  ${a}  ${b}")
                .appendLine("*** Settings ***")
                .appendLine("Suite Setup    Set Global Variable    ${a}    1")
                .appendLine("Test Setup    Set Global Variable    ${b}    2")
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
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keywords", "No Args Kw", "No Args Kw", "No Args Kw")))
                    .isEmpty();
            softly.assertThat(
                    problemsOf(context, theCall("Run Keywords", "Log", "1", "AND", "Log", "1", "AND", "Log", "1")))
                    .isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log", "1", "ELSE", "Log", "2")))
                    .isEmpty();
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log", "3")))
                    .isEmpty();
        });
    }

    @Test
    public void noProblemsInNestedKeywordsAreReported_whenUsedProperly_inTestSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keywords", "No Args Kw", "No Args Kw", "No Args Kw")))
                    .isEmpty();
            softly.assertThat(
                    problemsOf(context, testSetup("Run Keywords", "Log", "1", "AND", "Log", "1", "AND", "Log", "1")))
                    .isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log", "1", "ELSE", "Log", "2")))
                    .isEmpty();
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log", "3")))
                    .isEmpty();
        });
    }

    @Test
    public void noProblemsInNestedKeywordsAreReported_whenUsedProperly_inTestTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Run Keyword", "Log", "1"))).isEmpty();
            softly.assertThat(
                    problemsOf(context, testTeardown("Run Keywords", "No Args Kw", "No Args Kw", "No Args Kw")))
                    .isEmpty();
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keywords", "Log", "1", "AND", "Log", "1", "AND", "Log", "1"))).isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "1"))).isEmpty();
            softly.assertThat(
                    problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "1", "ELSE", "Log", "2")))
                    .isEmpty();
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "1", "ELSE IF",
                    "cond", "Log", "2", "ELSE", "Log", "3"))).isEmpty();
        });
    }

    @Test
    public void keywordProblemsInNestedKeywordsAreReporter_whenArgumentsAreMissing() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*keywords"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 43, 46));
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Run Keyword")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 43, 54));
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 58, 61));
            softly.assertThat(problemsOf(context, theCall("Run Keywords", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 44, 47));
            softly.assertThat(problemsOf(context, theCall("Run Keywords", "Log", "Log", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 44, 47),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 51, 54),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 58, 61));
            softly.assertThat(problemsOf(context, theCall("Run Keywords", "Log", "1", "AND", "Log", "AND", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 63, 66),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 77, 80));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 54, 57));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 54, 57),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 69, 72));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Log", "1", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 74, 77));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "Log", "ELSE IF", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 54, 57),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 80, 83),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 95, 98));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 105, 108));
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
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 54, 57));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Run Keyword")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 54, 65));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 69, 72));
            softly.assertThat(problemsOf(context, testSetup("Run Keywords", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 55, 58));
            softly.assertThat(problemsOf(context, testSetup("Run Keywords", "Log", "Log", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 55, 58),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 62, 65),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 69, 72));
            softly.assertThat(problemsOf(context, testSetup("Run Keywords", "Log", "1", "AND", "Log", "AND", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 74, 77),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 88, 91));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 65, 68));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 65, 68),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 80, 83));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Log", "1", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 85, 88));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "Log", "ELSE IF", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 65, 68),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 91, 94),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 106, 109));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 116, 119));
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
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 57, 60));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Run Keyword")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 57, 68));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Run Keyword", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 72, 75));
            softly.assertThat(problemsOf(context, testTeardown("Run Keywords", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 58, 61));
            softly.assertThat(problemsOf(context, testTeardown("Run Keywords", "Log", "Log", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 58, 61),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 65, 68),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 72, 75));
            softly.assertThat(
                    problemsOf(context, testTeardown("Run Keywords", "Log", "1", "AND", "Log", "AND", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 77, 80),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 91, 94));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 68, 71));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 68, 71),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 83, 86));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Log", "1", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 88, 91));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "Log", "ELSE IF", "cond", "Log", "ELSE", "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 68, 71),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 94, 97),
                            problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 109, 112));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "Log", "1", "ELSE IF", "cond", "Log", "2", "ELSE",
                            "Log")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, 119, 122));
        });
    }

    @Test
    public void nonExistingVariablesAreReported_inArgumentsNotBelongingToNestedKeyword() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Repeat Keyword", "${x}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 46, 50));
            softly.assertThat(
                    problemsOf(context, theCall("Repeat Keyword", "${x}", "Repeat Keyword", "${y}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 46, 50),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 72, 76));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "${x}", "No Args Kw", "ELSE IF", "${y}", "No Args Kw", "ELSE", "Log",
                            "${z}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 46, 50),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 79, 83),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 116, 120));
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
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 57, 61));
            softly.assertThat(
                    problemsOf(context, testSetup("Repeat Keyword", "${x}", "Repeat Keyword", "${y}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 57, 61),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 83, 87));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "${x}", "No Args Kw", "ELSE IF", "${y}", "No Args Kw", "ELSE", "Log",
                            "${z}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 57, 61),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 90, 94),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 127, 131));
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
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 60, 64));
            softly.assertThat(problemsOf(context,
                    testTeardown("Repeat Keyword", "${x}", "Repeat Keyword", "${y}", "No Args Kw")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 60, 64),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 86, 90));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "${x}", "No Args Kw", "ELSE IF", "${y}", "No Args Kw", "ELSE",
                            "Log", "${z}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 60, 64),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 93, 97),
                            problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 130, 134));
        });
    }

    @Test
    public void nonExistingVariablesAreNotReported_inArgumentsBelongingToNestedKeywordWhichSkipsVarValidation() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Comment", "*msgs"), newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"), newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context, theCall("Repeat Keyword", "${x}", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 46, 50));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keywords", "Comment", "${x}", "AND", "Comment", "${x}", "AND", "Log", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 103, 107));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "Comment", "${x}", "ELSE IF", "cond", "Log", "${x}", "ELSE",
                            "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 99, 103));
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
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 57, 61));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keywords", "Comment", "${x}", "AND", "Comment", "${x}", "AND", "Log", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 114, 118));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Comment", "${x}"))).isEmpty();
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "Comment", "${x}", "ELSE IF", "cond", "Log", "${x}", "ELSE",
                            "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 110, 114));
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
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 60, 64));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keywords", "Comment", "${x}", "AND", "Comment", "${x}", "AND", "Log", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 117, 121));
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword If", "cond", "Comment", "${x}")))
                    .isEmpty();
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "Comment", "${x}", "ELSE IF", "cond", "Log", "${x}",
                            "ELSE", "Comment", "${x}")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(VariablesProblem.UNDECLARED_VARIABLE_USE, 113, 117));
        });
    }

    @Test
    public void variablesSyntaxProblemsAreReported_whenSyntaxCheckingKeywordAreNested() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Variable Should Exist", "var"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, theCall("Run Keyword", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 68, 69));
            softly.assertThat(problemsOf(context, theCall("Repeat Keyword", "count", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 80, 81));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keywords", "Variable Should Exist", "x", "AND", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 69, 70),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 106, 107));
            softly.assertThat(problemsOf(context, theCall("Run Keyword If", "cond", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 79, 80));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "Variable Should Exist", "x", "ELSE", "Variable Should Exist",
                            "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 79, 80),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 117, 118));
        });
    }

    @Test
    public void variablesSyntaxProblemsAreReported_whenSyntaxCheckingKeywordAreNested_inTestSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Variable Should Exist", "var"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testSetup("Run Keyword", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 79, 80));
            softly.assertThat(problemsOf(context, testSetup("Repeat Keyword", "count", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 91, 92));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keywords", "Variable Should Exist", "x", "AND", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 80, 81),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 117, 118));
            softly.assertThat(problemsOf(context, testSetup("Run Keyword If", "cond", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 90, 91));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "Variable Should Exist", "x", "ELSE", "Variable Should Exist",
                            "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 90, 91),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 128, 129));
        });
    }

    @Test
    public void variablesSyntaxProblemsAreReported_whenSyntaxCheckingKeywordAreNested_inTestTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Variable Should Exist", "var"),
                newBuiltInKeyword("Repeat Keyword", "repeat", "name", "*args"),
                newBuiltInKeyword("Run Keyword", "name", "*args"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context, testTeardown("Run Keyword", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 82, 83));
            softly.assertThat(
                    problemsOf(context, testTeardown("Repeat Keyword", "count", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 94, 95));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keywords", "Variable Should Exist", "x", "AND", "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 83, 84),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 120, 121));
            softly.assertThat(
                    problemsOf(context, testTeardown("Run Keyword If", "cond", "Variable Should Exist", "x")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 93, 94));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "Variable Should Exist", "x", "ELSE",
                            "Variable Should Exist", "y")))
                    .extracting(CAUSE_AND_LOCATION)
                    .containsOnly(problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 93, 94),
                            problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, 131, 132));
        });
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("    Run Keyword  Set Global Variable  ${a}  1")
                .appendLine("    Log  ${a}")
                .appendLine("    Run Keywords  Set Global Variable  ${b}  2  AND  Set Global Variable  ${c}  3")
                .appendLine("    Log  ${b}, ${c}")
                .appendLine("    Run Keyword If  cond  Set Global Variable  ${d}  4")
                .appendLine("    Log  ${d}")
                .appendLine(
                        "    Run Keyword If  cond  Set Global Variable  ${e}  5  ELSE  Set Global Variable  ${f}  6")
                .appendLine("    Log  ${e}, ${f}")
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
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inTestSetup_1() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("    [Setup]  Run Keyword  Set Global Variable  ${a}  1")
                .appendLine("    Log  ${a}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keyword", "name", "*args"));
        final FileValidationContext context = prepareContext(accessibleKws);

        assertThat(problemsOf(context, fileModel)).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inTestSetup_2() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine(
                        "    [Setup]  Run Keywords  Set Global Variable  ${a}  2  AND  Set Global Variable  ${b}  3")
                .appendLine("    Log  ${a}, ${b}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keywords", "*kws"));
        final FileValidationContext context = prepareContext(accessibleKws);

        assertThat(problemsOf(context, fileModel)).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inTestSetup_3() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("    [Setup]  Run Keyword If  cond  Set Global Variable  ${a}  4")
                .appendLine("    Log  ${a}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keyword If", "name", "*args"));
        final FileValidationContext context = prepareContext(accessibleKws);

        assertThat(problemsOf(context, fileModel)).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariablesAreCreatedUsingSpecialKeywordsNestedInOtherKeywords_inTestSetup_4() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine(
                        "    [Setup]  Run Keyword If  cond  Set Global Variable  ${a}  5  ELSE  Set Global Variable  ${b}  6")
                .appendLine("    Log  ${a}, ${b}")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Set Global Variable", "name", "*values"),
                newBuiltInKeyword("Run Keyword If", "name", "*args"));
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
                    theCall("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE IF", "cond",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 68, 72,
                            "Invalid nested executables syntax. ELSE branch should not be followed by ELSE IF branches"));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE", "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 68, 72,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 90, 94,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"));
            softly.assertThat(problemsOf(context,
                    theCall("Run Keyword If", "cond", "No Args Kw", "ELSE IF", "ELSE IF", "cond", "ELSE")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 68, 75,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 79, 86,
                                    "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 98, 102,
                                    "Invalid nested executables syntax. ELSE branch requires keyword to be defined"));

        });
    }

    @Test
    public void syntaxProblemsInNestedConstructionsAreReported_inTestSetup() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE IF", "cond",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 79, 83,
                            "Invalid nested executables syntax. ELSE branch should not be followed by ELSE IF branches"));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE", "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 79, 83,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 101, 105,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"));
            softly.assertThat(problemsOf(context,
                    testSetup("Run Keyword If", "cond", "No Args Kw", "ELSE IF", "ELSE IF", "cond", "ELSE")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 79, 86,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 90, 97,
                                    "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 109, 113,
                                    "Invalid nested executables syntax. ELSE branch requires keyword to be defined"));

        });
    }

    @Test
    public void syntaxProblemsInNestedConstructionsAreReported_inTestTeardown() {
        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("Log", "msg"),
                newBuiltInKeyword("Run Keyword If", "condition", "name", "*args"),
                newResourceKeyword("No Args Kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE IF", "cond",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 82, 86,
                            "Invalid nested executables syntax. ELSE branch should not be followed by ELSE IF branches"));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE", "No Args Kw", "ELSE",
                            "No Args Kw")))
                    .extracting(ALL)
                    .containsOnly(
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 82, 86,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 104, 108,
                                    "Invalid nested executables syntax. Multiple ELSE branches are defined"));
            softly.assertThat(problemsOf(context,
                    testTeardown("Run Keyword If", "cond", "No Args Kw", "ELSE IF", "ELSE IF", "cond", "ELSE")))
                    .extracting(ALL)
                    .containsOnly(problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 82, 89,
                            "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 93, 100,
                                    "Invalid nested executables syntax. ELSE IF branch requires condition and keyword to be defined"),
                            problem(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX, 112, 116,
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
        final TestCase testCase = fileModel.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getLinkedElement();
        new TestCaseValidator(context, testCase, reporter).validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }

    static Tuple problem(final Object... properties) {
        // adding synonym for better readablity
        return tuple(properties);
    }

    private static RobotSuiteFile theCall(final String... callCells) {
        return new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("    " + String.join("    ", callCells))
                .build();
    }

    private static RobotSuiteFile testSetup(final String... callCells) {
        return new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("    [Setup]    " + String.join("    ", callCells))
                .appendLine("    Log    1")
                .build();
    }

    private static RobotSuiteFile testTeardown(final String... callCells) {
        return new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("    [Teardown]    " + String.join("    ", callCells))
                .appendLine("    Log    1")
                .build();
    }
}
