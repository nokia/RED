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

    private Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
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
}
