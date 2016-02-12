/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

public class KeywordTableValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void emptyKeywordIsReported() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD, new ProblemPosition(2, Range.closed(17, 24))));
    }
    
    @Test
    public void emptyKeywordIsReported_whenThereIsAnEmptyReturn() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Return]");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD, new ProblemPosition(2, Range.closed(17, 24))));
    }
    
    @Test
    public void nothingIsReported_whenThereIsNonEmptyReturn() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Return]  42");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }
    
    @Test
    public void nothingIsReported_whenThereIsALineToExecute() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***",
                "keyword",
                "  kw");

        final KeywordEntity entity = new ValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw", "", false,
                new Path("/res.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                (Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }
    
    @Test
    public void emptyKeywordIsReported_whenThereIsACommentedLine() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword", "  # kw");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.EMPTY_KEYWORD, new ProblemPosition(2, Range.closed(17, 24))));
    }
    
    @Test
    public void keywordsAreReported_whenTheyAreDuplicated() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword 1",
                "  [Return]  42", 
                "keyword 1",
                "  [Return]  100");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(2, Range.closed(17, 26))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(4, Range.closed(42, 51))));
    }
    
    @Test
    public void keywordsAreReported_whenTheyAreDuplicated_2() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword 1",
                "  [Return]  1", 
                "k e y w o r d 1",
                "  [Return]  2", 
                "k_E_y_W_o_R_d_1",
                "  [Return]  3");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(3);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(2, Range.closed(17, 26))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(4, Range.closed(41, 56))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(6, Range.closed(71, 86))));
    }

    @Test
    public void unrecognizedSettingsAreReported() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Unknown setting]",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD_SETTING, new ProblemPosition(3, Range.closed(27, 44))));
    }

    @Test
    public void duplicatedArgumentsAreReported_inArgumentsSetting() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  ${x}  ${x}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(3, Range.closed(40, 44))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void duplicatedArgumentsAreReported_inEmbeddedArguments() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword ${x} ${y} ${x} rest of name",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(25, 29))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(35, 39))));
    }

    @Test
    public void duplicatedArgumentsAreReported_inEmbeddedArgumentsWithRegex() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword ${x:\\d+} ${y} ${x} rest of name",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(25, 33))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(39, 43))));
    }

    @Test
    public void duplicatedArgumentsAreReported_whenDefinedInDuplicatedSettings() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword ${x:\\d+} rest of name",
                "  [Arguments]  ${a}  ${x}",
                "  [Arguments]  ${x}  ${b}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(6);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.ARGUMENT_SETTING_DEFINED_TWICE, new ProblemPosition(3, Range.closed(49, 60))),
                new Problem(KeywordsProblem.ARGUMENT_SETTING_DEFINED_TWICE, new ProblemPosition(4, Range.closed(75, 86))),
                new Problem(KeywordsProblem.ARGUMENT_SETTING_DEFINED_TWICE, new ProblemPosition(2, Range.closed(17, 46))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(2, Range.closed(25, 33))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(3, Range.closed(68, 72))),
                new Problem(KeywordsProblem.ARGUMENT_DEFINED_TWICE, new ProblemPosition(4, Range.closed(88, 92))));
    }

    @Test
    public void defaultArgumentsAreReported_whenTheyOccurBeforeNonDefaultOnes() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  ${a}=10  ${b}  ${c}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.NON_DEFAULT_ARGUMENT_AFTER_DEFAULT,
                        new ProblemPosition(3, Range.closed(49, 53))),
                new Problem(KeywordsProblem.NON_DEFAULT_ARGUMENT_AFTER_DEFAULT,
                        new ProblemPosition(3, Range.closed(55, 59))));
    }

    @Test
    public void nothingIsReported_whenDefaultArgumentIsFollowedByVarargs() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  ${a}=10  @{b}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void nothingIsReported_whenDefaultArgumentIsFollowedByKwargs() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  ${a}=10  &{b}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void scalarIsReported_whenItFollowsVararg() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  @{a}  ${b}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_VARARG, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void listIsReported_whenItFollowsVararg() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  @{a}  @{b}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_VARARG, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void nothingIsReported_whenDictionaryFollowsVararg() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  @{a}  &{b}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void scalarIsReported_whenItFollowsKwargs() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  &{a}  ${b}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_KWARG, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void listIsReported_whenItFollowsKwargs() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  &{a}  @{b}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_KWARG, new ProblemPosition(3, Range.closed(46, 50))));
    }

    @Test
    public void dictIsReported_whenItFollowsKwargs() throws CoreException {
        final RobotSuiteFile file = RobotSuiteFileCreator.createModel(
                "*** Keywords ***", 
                "keyword",
                "  [Arguments]  &{a}  &{b}",
                "  [Return]  10");

        final FileValidationContext context = prepareContext();
        final KeywordTableValidator validator = new KeywordTableValidator(context,
                file.findSection(RobotKeywordsSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.ARGUMENT_AFTER_KWARG, new ProblemPosition(3, Range.closed(46, 50))));
    }
    
	@Test
	public void keywordDefinitionWithDotsIsReported() throws CoreException {
		final RobotSuiteFile file = RobotSuiteFileCreator.createModel("*** Keywords ***", "keyword.1", "    kw");

		final KeywordEntity entity = new ValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw", "", false,
				new Path("/res.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

		final FileValidationContext context = prepareContext(accessibleKws);
		final KeywordTableValidator validator = new KeywordTableValidator(context,
				file.findSection(RobotKeywordsSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(KeywordsProblem.KEYWORD_NAME_WITH_DOTS, new ProblemPosition(2, Range.closed(17, 26))));
	}
	
	@Test
	public void keywordOccurrenceWithDotsIsNotReported() throws CoreException {
		final RobotSuiteFile file = RobotSuiteFileCreator.createModel("*** Keywords ***", "keyword", "    k.w");

		final KeywordEntity entity = new ValidationKeywordEntity(KeywordScope.RESOURCE, "res", "k.w", "", false,
				new Path("/res.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("k.w",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

		final FileValidationContext context = prepareContext(accessibleKws);
		final KeywordTableValidator validator = new KeywordTableValidator(context,
				file.findSection(RobotKeywordsSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getReportedProblems().isEmpty());
	}
    
	@Test
	public void keywordOccurrenceWithDotsAndSourceIsReported() throws CoreException {
		final RobotSuiteFile file = RobotSuiteFileCreator.createModel("*** Keywords ***", "keyword", "    res.k.w",
				"    res1.kw", "    res.kw");

		final KeywordEntity entity1 = new ValidationKeywordEntity(KeywordScope.RESOURCE, "res", "k.w", "", false,
				new Path("/res.robot"));
		final KeywordEntity entity2 = new ValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw", "", false,
				new Path("/res.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("k.w",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1), "kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity2));

		final FileValidationContext context = prepareContext(accessibleKws);
		final KeywordTableValidator validator = new KeywordTableValidator(context,
				file.findSection(RobotKeywordsSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(29, 36))),
				new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(4, Range.closed(41, 48))));
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
