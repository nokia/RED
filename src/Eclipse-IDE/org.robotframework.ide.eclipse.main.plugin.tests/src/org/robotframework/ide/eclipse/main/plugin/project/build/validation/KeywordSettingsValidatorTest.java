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
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class KeywordSettingsValidatorTest {

    @Test
    public void unknownSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [SomeSetting]")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 40))));
    }

    @Test
    public void emptyReturnSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Return]")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 35))));
    }

    @Test
    public void nothingIsReported_whenReturnHasValueToReturn() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Return]    0")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void duplicatedReturnsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Return]    1")
                .appendLine("  [Return]    2")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 35))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(4, Range.closed(43, 51))));
    }

    @Test
    public void emptyTagsSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Tags]")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 33))));
    }

    @Test
    public void nothingIsReported_whenThereIsATagDefined() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Tags]    tag")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void duplicatedTagsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Tags]    tag1")
                .appendLine("  [Tags]    tag2")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 33))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(4, Range.closed(44, 50))));
    }

    @Test
    public void undeclaredVariableInTagsIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Tags]  ${var}")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(35, 41))));
    }

    @Test
    public void emptyDocumentationSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Documentation]")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 42))));
    }

    @Test
    public void nothingIsReported_whenThereIsADocumentationWritten() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Documentation]    docu")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).isEmpty();
    }

    @Ignore("see RED-1036")
    @Test
    public void duplicatedDocumentationsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Documentation]    doc1")
                .appendLine("  [Documentation]    doc2")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 42))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(4, Range.closed(53, 68))));
    }

    @Test
    public void outdatedDocumentationSynonymIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Document]    doc1")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.DOCUMENT_SYNONYM, new ProblemPosition(3, Range.closed(27, 37))));
    }

    @Test
    public void emptyTimeoutSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Timeout]")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 36))));
    }

    @Test
    public void nothingIsReported_whenThereIsATimeoutProvided() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Timeout]    10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).isEmpty();
    }

    @Ignore("see RED-1036")
    @Test
    public void duplicatedTimeoutsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Timeout]    1")
                .appendLine("  [Timeout]    2")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 36))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(4, Range.closed(44, 53))));
    }

    @Test
    public void invalidSyntaxInKeywordTimeoutIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw1")
                .appendLine("  [Timeout]  something")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(ArgumentProblem.INVALID_TIME_FORMAT, new ProblemPosition(3, Range.closed(34, 43))));
    }

    @Test
    public void undeclaredVariableInKeywordTimeoutIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw1")
                .appendLine("  [Timeout]  ${var1}")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(34, 41))));
    }

    @Test
    public void emptyTeardownSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 37))));
    }

    @Test
    public void nothingIsReported_whenThereIsATeardownProvided() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]    keyword")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("keyword", new Path("/file.robot")));

        final Collection<Problem> problems = validate(prepareContext(accessibleKws), fileModel);
        assertThat(problems).isEmpty();
    }

    @Ignore("see RED-1036")
    @Test
    public void duplicatedTeardownsAreReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  [Teardown]    keyword")
                .appendLine("  [Teardown]    keyword")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("keyword", new Path("/file.robot")));

        final Collection<Problem> problems = validate(prepareContext(accessibleKws), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(22, 32))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(4, Range.closed(46, 56))));
    }

    @Test
    public void outdatedTeardownSynonymIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  [Postcondition]    keyword")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("keyword", new Path("/file.robot")));

        final Collection<Problem> problems = validate(prepareContext(accessibleKws), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.POSTCONDITION_SYNONYM, new ProblemPosition(3, Range.closed(22, 37))));
    }

    @Test
    public void emptyArgumentsSettingIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 38))));
    }

    @Test
    public void duplicatedArgumentsAreReported_inArgumentsSetting() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  ${x}  ${x}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(3, Range.closed(40, 44))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void duplicatedArgumentsAreReported_inEmbeddedArguments() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword ${x} ${y} ${x} rest of name")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(25, 29))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(35, 39))));
    }

    @Test
    public void duplicatedArgumentsAreReported_inEmbeddedArgumentsWithRegex() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword ${x:\\d+} ${y} ${x} rest of name")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(25, 33))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(39, 43))));
    }

    @Test
    public void duplicatedArgumentsAreReported_whenDefinedInDuplicatedSettings() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword ${x:\\d+} rest of name")
                .appendLine("  [Arguments]  ${a}  ${x}")
                .appendLine("  [Arguments]  ${x}  ${b}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(49, 60))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(4, Range.closed(75, 86))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD_SETTING, new ProblemPosition(2, Range.closed(17, 46))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(25, 33))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(3, Range.closed(68, 72))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(4, Range.closed(88, 92))));
    }

    @Test
    public void defaultArgumentsAreReported_whenTheyOccurBeforeNonDefaultOnes() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  ${a}=10  ${b}  ${c}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.NON_DEFAULT_ARGUMENT_AFTER_DEFAULT,
                        new ProblemPosition(3, Range.closed(49, 53))),
                new Problem(KeywordsProblem.NON_DEFAULT_ARGUMENT_AFTER_DEFAULT,
                        new ProblemPosition(3, Range.closed(55, 59))));
    }

    @Test
    public void nothingIsReported_whenDefaultArgumentIsFollowedByVarargs() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  ${a}=10  @{b}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nothingIsReported_whenDefaultArgumentIsFollowedByKwargs() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  ${a}=10  &{b}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void scalarIsReported_whenItFollowsVararg() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  @{a}  ${b}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_VARARG, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void listIsReported_whenItFollowsVararg() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  @{a}  @{b}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_VARARG, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void nothingIsReported_whenDictionaryFollowsVararg() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  @{a}  &{b}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void scalarIsReported_whenItFollowsKwargs() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  &{a}  ${b}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_KWARG, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void listIsReported_whenItFollowsKwargs() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  &{a}  @{b}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_KWARG, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void dictIsReported_whenItFollowsKwargs() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  &{a}  &{b}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_KWARG, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void unknownVariableIsReported_whenItIsUsedInDefaultValueAndNotKnown() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  ${x}  ${y}=${unknown}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(51, 61))));
    }

    @Test
    public void nothingIsReported_whenDefaultValuesUsesVariableDefinedJustBefore() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  ${x}  ${y}=${x}")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).isEmpty();
    }

    @Test
    public void syntaxProblemsAreReported_whenDefinitionIsInvalid() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  123  ${x  {y}  ${x} =123  ${}  ${a} ${b}  @{m}=0")
                .appendLine("  [Return]  10")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX, new ProblemPosition(3, Range.closed(40, 43))),
                new Problem(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX, new ProblemPosition(3, Range.closed(45, 48))),
                new Problem(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX, new ProblemPosition(3, Range.closed(50, 53))),
                new Problem(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX, new ProblemPosition(3, Range.closed(55, 64))),
                new Problem(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX, new ProblemPosition(3, Range.closed(66, 69))),
                new Problem(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX, new ProblemPosition(3, Range.closed(71, 80))),
                new Problem(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX, new ProblemPosition(3, Range.closed(82, 88))));
    }

    private Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
            throws CoreException {
        final MockReporter reporter = new MockReporter();
        final UserKeyword keyword = fileModel.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getLinkedElement();
        new KeywordSettingsValidator(context, keyword, reporter).validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }
}
