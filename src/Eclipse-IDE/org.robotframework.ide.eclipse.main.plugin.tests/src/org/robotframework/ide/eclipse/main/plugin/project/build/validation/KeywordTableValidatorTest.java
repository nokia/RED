/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;

public class KeywordTableValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
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

    private static FileValidationContext prepareContext() {
        final ValidationContext parentContext = new ValidationContext(new RobotModel(), RobotVersion.from("0.0"),
                SuiteExecutor.Python, Maps.<String, LibrarySpecification> newHashMap(),
                Maps.<ReferencedLibrary, LibrarySpecification> newHashMap());
        final FileValidationContext context = new FileValidationContext(parentContext, mock(IFile.class),
                new HashSet<String>());
        return context;
    }
}
