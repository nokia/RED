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
    public void keywordOverriddingOtherImportedKeywordIsReported() throws CoreException {
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

    private Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
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
}
