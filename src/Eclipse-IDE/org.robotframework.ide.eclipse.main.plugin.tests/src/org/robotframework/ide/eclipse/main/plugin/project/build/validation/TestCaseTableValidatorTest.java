/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.newRefLibraryKeyword;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.newResourceKeyword;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.newStdLibraryKeyword;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.prepareContext;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Test;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class TestCaseTableValidatorTest {

    @Test
    public void duplicatedCaseIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(newResourceKeyword("kw", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(TestCasesProblem.DUPLICATED_CASE, new ProblemPosition(2, Range.closed(19, 23))),
                new Problem(TestCasesProblem.DUPLICATED_CASE, new ProblemPosition(4, Range.closed(31, 35))));
    }

    @Test
    public void deprecatedKeywordIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newDeprecatedValidationKeywordEntity(KeywordScope.STD_LIBRARY, "lib", "kw", new Path("/suite.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.DEPRECATED_KEYWORD, new ProblemPosition(3, Range.closed(28, 30))));
    }

    @Test
    public void keywordFromNestedLibraryIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw1")
                .appendLine("    kw2")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newRefLibraryKeyword("lib", "kw1", new Path("/res.robot")),
                newStdLibraryKeyword("lib", "kw2", new Path("/res.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY, new ProblemPosition(3, Range.closed(28, 31))),
                new Problem(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY, new ProblemPosition(4, Range.closed(36, 39))));
    }

    @Test
    public void keywordFromNestedLibraryIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newRefLibraryKeyword("lib", "kw", new Path("/res.robot")),
                newRefLibraryKeyword("lib", "kw", new Path("/suite.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
    }

    @Test
    public void keywordFromLibraryIsNotReported_whenAliasIsUsed() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    lib.kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newRefLibraryKeyword("lib", "kw", new Path("/suite.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
    }

    @Test
    public void keywordFromLibraryIsNotReported_whenLibraryPrefixIsUsed() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    library.kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newRefLibraryKeyword("library", "kw", new Path("/suite.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
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

        final List<KeywordEntity> accessibleKws = newArrayList(
                newRefLibraryKeyword("lib", "kw1", new Path("/suite.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
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

        final List<KeywordEntity> accessibleKws = newArrayList(
                newRefLibraryKeyword("lib1", "kw", new Path("/suite.robot")),
                newRefLibraryKeyword("lib2", "kw", new Path("/suite.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
                new Problem(KeywordsProblem.AMBIGUOUS_KEYWORD, new ProblemPosition(3, Range.closed(28, 30))));
    }

    @Test
    public void keywordWithAmbiguousNameIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    res1.kw")
                .appendLine("    res2.kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newResourceKeyword("kw", new Path("/res1.robot")),
                newResourceKeyword("kw", new Path("/res2.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
    }

    @Test
    public void unknownKeywordIsReported_whenUsedWithOriginalLibraryNameInsteadOfAlias() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Library  lib  WITH NAME  alias")
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  lib.kw")
                .build();

        final List<KeywordEntity> accessibleKws = newArrayList(
                newRefLibraryKeyword("lib", "alias", "kw1", new Path("/res1.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).containsOnly(
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

        final List<KeywordEntity> accessibleKws = newArrayList(
                newRefLibraryKeyword("lib", "alias", "kw", new Path("/suite.robot")));
        final FileValidationContext context = prepareContext(accessibleKws);

        final Collection<Problem> problems = validate(context, file);
        assertThat(problems).isEmpty();
    }

    private static KeywordEntity newDeprecatedValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String name, final IPath exposingPath) {
        return new ValidationKeywordEntity(scope, sourceName, name, Optional.empty(), true, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor());
    }

    private Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
            throws CoreException {
        final MockReporter reporter = new MockReporter();
        new TestCaseTableValidator(context, fileModel.findSection(RobotCasesSection.class), reporter)
                .validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }
}
