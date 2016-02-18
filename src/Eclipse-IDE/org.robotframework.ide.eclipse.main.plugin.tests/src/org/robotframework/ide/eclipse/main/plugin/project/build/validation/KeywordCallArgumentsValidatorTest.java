/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class KeywordCallArgumentsValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void nothingIsReported_whenThereIsExactNumberOfArguments() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    arg")
                .build();
        
        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
    }

    @Test
    public void invalidNumberOfParametersIsReported_whenThereIsAnArgumentMissing() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword")
                .build();
        
        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, new ProblemPosition(3, Range.closed(28, 35))));
    }

    @Test
    public void invalidNumberOfParametersIsReported_whenThereAreTooManyArguments() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    arg1    arg2")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, new ProblemPosition(3, Range.closed(28, 35))));
    }
    
    @Test
    public void positionalArgumentsAreReported_whenTheyAreUsedAfterNamedOnes_1() {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    x=5    arg    z=10")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y", "z");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
            new Problem(ArgumentProblem.POSITIONAL_ARGUMENT_AFTER_NAMED, new ProblemPosition(3, Range.closed(46, 49))));
    }

    @Test
    public void positionalArgumentsAreReported_whenTheyAreUsedAfterNamedOnes_2() {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    x=5    arg    w=10")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y", "z");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
            new Problem(ArgumentProblem.POSITIONAL_ARGUMENT_AFTER_NAMED, new ProblemPosition(3, Range.closed(46, 49))),
            new Problem(ArgumentProblem.POSITIONAL_ARGUMENT_AFTER_NAMED, new ProblemPosition(3, Range.closed(53, 57))));
    }

    private void validate(final RobotSuiteFile file, final DefiningTokenWithArgumentTokens tokens,
            final ArgumentsDescriptor descriptor) {
        final KeywordCallArgumentsValidator validator = new KeywordCallArgumentsValidator(file.getFile(),
                tokens.definingToken, reporter, descriptor, tokens.argumentTokens);
        validator.validate(null);
    }

    private static DefiningTokenWithArgumentTokens getKeywordCallTokensFromFirstLineOf(final RobotSuiteFile file,
            final String caseName) {
        final Optional<RobotCasesSection> casesSection = file.findSection(RobotCasesSection.class);
        final RobotCase testCase = (RobotCase) casesSection.get().findChild(caseName);
        final RobotExecutableRow<TestCase> executable = testCase.getLinkedElement().getTestExecutionRows().get(0);
        final IExecutableRowDescriptor<?> executableRowDescriptor = executable.buildLineDescription();
        return new DefiningTokenWithArgumentTokens(executableRowDescriptor.getAction().getToken(),
                executable.getArguments());
    }

    private static class DefiningTokenWithArgumentTokens {

        RobotToken definingToken;

        List<RobotToken> argumentTokens;

        DefiningTokenWithArgumentTokens(final RobotToken definingToken, final List<RobotToken> arguments) {
            this.definingToken = definingToken;
            this.argumentTokens = arguments;
        }
    }
}
