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
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

public class TestCaseTableValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void emptyNameIsReported_1() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(TestCasesProblem.EMPTY_CASE_NAME, new ProblemPosition(2, Range.closed(19, 19))));
    }

    @Test
    public void emptyNameIsReported_2() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(TestCasesProblem.EMPTY_CASE_NAME, new ProblemPosition(2, Range.closed(19, 19))));
    }

    @Test
    public void emptyNameIsReported_inTsvFile() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("    ")
                .appendLine("\tkw")
                .buildTsv();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(TestCasesProblem.EMPTY_CASE_NAME, new ProblemPosition(2, Range.closed(19, 19))));
    }

    @Test
    public void emptyTestCaseIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(TestCasesProblem.EMPTY_CASE, new ProblemPosition(2, Range.closed(19, 23))));
    }

    @Test
    public void givenTestCaseWithEnvironmentVariable_whenNoMarkersShouldBeReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    [Setup]  kw  %{foobar}")
                .appendLine("    [Teardown]    kw    %{PATH}")
                .appendLine("    [Tags]    tag_with_%{HOME}")
                .appendLine("    kw    %{bar}")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "var");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void emptyTestCaseIsReported_whenCommentedLineIsInside() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  # kw")
                .build();

        final FileValidationContext context = prepareContext();
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(TestCasesProblem.EMPTY_CASE, new ProblemPosition(2, Range.closed(19, 23))));
    }

    @Test
    public void unknownTestCaseSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Unknown]")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(TestCasesProblem.UNKNOWN_TEST_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 35))));
    }

    @Test
    public void duplicatedCaseIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(TestCasesProblem.DUPLICATED_CASE, new ProblemPosition(2, Range.closed(19, 23))),
                new Problem(TestCasesProblem.DUPLICATED_CASE, new ProblemPosition(4, Range.closed(31, 35))));
    }

    @Test
    public void deprecatedKeywordIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final KeywordEntity entity1 = newDeprecatedValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.DEPRECATED_KEYWORD, new ProblemPosition(3, Range.closed(28, 30))));
    }

    @Test
    public void keywordFromNestedLibraryIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw1")
                .appendLine("    kw2")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "lib", "kw1",
                new Path("/res.robot"));
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "lib", "kw2",
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw1",
                newArrayList(entity1), "kw2", newArrayList(entity2));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY, new ProblemPosition(3, Range.closed(28, 31))),
                new Problem(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY, new ProblemPosition(4, Range.closed(36, 39))));
    }

    @Test
    public void keywordFromNestedLibraryIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "lib", "kw",
                new Path("/suite.robot"));
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "lib", "kw",
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1, entity2));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void keywordFromLibraryIsNotReported_whenAliasIsUsed() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    lib.kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "library", "lib", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void keywordFromLibraryIsNotReported_whenLibraryPrefixIsUsed() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    library.kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "library", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void keywordWithInconsistentNameIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  kw1")
                .appendLine("  k w1")
                .appendLine("  k_w1")
                .appendLine("  k_w 1")
                .appendLine("  K w1")
                .appendLine("  K_w 1")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw1",
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw1",
                newArrayList(entity1));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(5);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
                        new ProblemPosition(4, Range.closed(32, 36))),
                new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
                        new ProblemPosition(5, Range.closed(39, 43))),
                new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
                        new ProblemPosition(6, Range.closed(46, 51))),
                new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
                        new ProblemPosition(7, Range.closed(54, 58))),
                new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
                        new ProblemPosition(8, Range.closed(61, 66))));
    }

    @Test
    public void keywordWithAmbiguousNameIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res1", "kw",
                new Path("/res1.robot"));
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res2", "kw",
                new Path("/res2.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1, entity2));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.AMBIGUOUS_KEYWORD, new ProblemPosition(3, Range.closed(28, 30))));
    }

    @Test
    public void keywordWithAmbiguousNameIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    res1.kw")
                .appendLine("    res2.kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res1", "kw",
                new Path("/res1.robot"));
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res2", "kw",
                new Path("/res2.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1, entity2));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void undeclaredVariableIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw  ${var}")
                .appendLine("    kw  ${var2}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "var2");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1));

        final Set<String> accessibleVariables = newHashSet("${var2}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(32, 38))));
    }

    @Test
    public void numberVariableIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw  ${2}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void variableInComputationIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw  ${var-2}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1));

        final Set<String> accessibleVariables = newHashSet("${var}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void variableInCommentKeywordIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Comment  ${var}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn", "Comment",
                new Path("/suite.robot"), "var");

        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("comment",
                newArrayList(entity1));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void variableInGetVariableValueIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    ${var} =  Get Variable Value  ${x}")
                .appendLine("    kw  ${var}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn",
                "Get Variable Value", new Path("/suite.robot"), "arg");
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("getvariablevalue",
                newArrayList(entity1), "kw", newArrayList(entity2));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void variableInGetVariableValueIsReported_whenSyntaxIncorrect() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    ${var} =  Get Variable Value  %{x}")
                .appendLine("    kw  ${var}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn",
                "Get Variable Value", new Path("/suite.robot"), "arg");
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("getvariablevalue",
                newArrayList(entity1), "kw", newArrayList(entity2));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(58, 62))));
    }

    @Test
    public void variableInSetGlobalVariableKeywordIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Global Variable  ${V_ar}")
                .appendLine("    kw  ${var}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn",
                "Set Global Variable", new Path("/suite.robot"), "arg");
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("setglobalvariable",
                newArrayList(entity1), "kw", newArrayList(entity2));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void variableInSetGlobalVariableKeywordIsReported_whenSyntaxIncorrect() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Global Variable  %{V_ar}")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn",
                "Set Global Variable", new Path("/suite.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("setglobalvariable",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(49, 56))));
    }

    @Test
    public void variableInSetSuiteVariableKeywordIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Suite Variable  ${V_ar}")
                .appendLine("    kw  ${var}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn",
                "Set Suite Variable", new Path("/suite.robot"), "arg");
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("setsuitevariable",
                newArrayList(entity1), "kw", newArrayList(entity2));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void variableInSetSuiteVariableKeywordIsReported_whenSyntaxIncorrect() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Suite Variable  %{V_ar}")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn",
                "Set Suite Variable", new Path("/suite.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("setsuitevariable",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(48, 55))));
    }

    @Test
    public void variableInSetTestVariableKeywordIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Test Variable  ${V_ar}")
                .appendLine("    kw  ${var}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn",
                "Set Test Variable", new Path("/suite.robot"), "arg");
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("settestvariable",
                newArrayList(entity1), "kw", newArrayList(entity2));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void variableInSetTestVariableKeywordIsReported_whenSyntaxIncorrect() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Test Variable  %{V_ar}")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn",
                "Set Test Variable", new Path("/suite.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("settestvariable",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(47, 54))));
    }

    @Test
    public void undeclaredVariableAndKeywordInTestCaseSetupAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  kw1  ${var}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(35, 38))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(40, 46))));
    }

    @Test
    public void undeclaredVariableAndKeywordInTestCaseTeardownAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Teardown]  kw1  ${var}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(38, 41))),
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(43, 49))));
    }

    @Test
    public void declaredVariablesAndKeywordsInTestCaseSettingsAreNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  kw  ${var1}")
                .appendLine("  [Teardown]  kw  ${var2}")
                .appendLine("  ${var2}=  Set Variable  2")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"), "arg");
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn", "Set Variable",
                new Path("/suite.robot"), "arg");
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity1), "setvariable", newArrayList(entity2));

        final Set<String> accessibleVariables = newHashSet("${var1}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void declaredVariableAsKeywordInTestCaseSetupAndTeardownIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  ${var}")
                .appendLine("  [Teardown]  ${var}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final Set<String> accessibleVariables = newHashSet("${var}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                        new ProblemPosition(3, Range.closed(35, 41))),
                new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                        new ProblemPosition(4, Range.closed(56, 62))));
    }

    @Test
    public void unknownKeywordIsReported_whenUsedWithOriginalLibraryNameInsteadOfAlias() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Settings ***")
                .appendLine("Library  lib  WITH NAME  alias")
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  lib.kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "lib", "alias", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(5, Range.closed(74, 80))));
    }
    
    @Test
    public void unknownKeywordIsNotReported_whenUsedWithAlias() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Library  lib  WITH NAME  alias")
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  alias.kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "lib", "alias", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
    }
    
    
    @Test
    public void undeclaredKeywordInTestCaseTemplateIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Template]  kw1 ${var}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(38, 48))));
    }

    @Test
    public void noneInTestTemplateIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Template]  None")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void undeclaredVariableInTestCaseTagsIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Tags]  ${var1}  ${var2}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final Set<String> accessibleVariables = newHashSet("${var1}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(43, 50))));
    }

    @Test
    public void undeclaredVariableInTestCaseTimeoutIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test1")
                .appendLine("  [Timeout]  ${var1}")
                .appendLine("  kw")
                .appendLine("test2")
                .appendLine("  [Timeout]  ${var2}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                newArrayList(entity));

        final Set<String> accessibleVariables = newHashSet("${var1}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(6, Range.closed(70, 77))));
    }

    private static KeywordEntity newValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String name, final IPath exposingPath, final String... args) {
        return new ValidationKeywordEntity(scope, sourceName, name, Optional.empty(), false, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor(args));
    }

    private static KeywordEntity newValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String sourceAlias, final String name, final IPath exposingPath) {
        return new ValidationKeywordEntity(scope, sourceName, name, Optional.of(sourceAlias), false, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor());
    }

    private static KeywordEntity newDeprecatedValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String name, final IPath exposingPath) {
        return new ValidationKeywordEntity(scope, sourceName, name, Optional.empty(), true, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor());
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
