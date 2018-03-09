/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class GeneralKeywordCallArgumentsValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void nothingIsReported_whenThereIsExactNumberOfArguments() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    arg")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void invalidNumberOfParametersIsReported_whenThereAreToFewArguments() {
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
    public void invalidNumberOfParametersIsReported_whenThereAreTooManyArguments() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
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
    public void nothingIsReported_whenNumberOfArgumentsAreInBoundedRange() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    arg1    arg2")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y=1", "z=2");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void invalidNumberOfParametersIsReported_whenThereAreToFewArgumentsThanInBoundedRange() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y=1", "z=2");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, new ProblemPosition(3, Range.closed(28, 35))));
    }

    @Test
    public void invalidNumberOfParametersIsReported_whenThereAreTooManyArgumentsThanInBoundedRange() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    arg1    arg2    arg3    arg4")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y=1", "z=2");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, new ProblemPosition(3, Range.closed(28, 35))));
    }

    @Test
    public void nothingIsReported_whenNumberOfArgumentsAreInUnboundedRange() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    arg1    arg2    arg3")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y", "*z");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void invalidNumberOfParametersIsReported_whenThereAreToFewArgumentsThanInUnboundedRange() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    arg")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y", "*z");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, new ProblemPosition(3, Range.closed(28, 35))));
    }

    @Test
    public void positionalArgumentsAreReported_whenTheyAreUsedAfterNamedOnes_1() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
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
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
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

    @Test
    public void namedArgumentIsReported_whenItMatchesArgumentAlreadyDefinedByPositionalOne() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    1    x=2")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y=0");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.MULTIPLE_MATCH_TO_SINGLE_ARG, new ProblemPosition(3, Range.closed(44, 47))));
    }

    @Test
    public void namedArgumentIsReported_whenItMatchesArgumentAlreadyDefinedByOtherNamedOne() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    1    y=2    y=3")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y=0", "z=0");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.MULTIPLE_MATCH_TO_SINGLE_ARG, new ProblemPosition(3, Range.closed(51, 54))));
    }

    @Test
    public void namedArgumentIsReported_whenItMatchesArgumentAlreadyDefinedByOtherNamedOneInPresenceOfKwargs() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    1   arg=2   y=3   z=4")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("arg", "**kwargs");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.MULTIPLE_MATCH_TO_SINGLE_ARG, new ProblemPosition(3, Range.closed(43, 48))));
    }

    @Test
    public void missingRequiredArgumentIsReported_whenThereIsNoValueProvidedForIt() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    y=2")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y=0");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.NO_VALUE_PROVIDED_FOR_REQUIRED_ARG, new ProblemPosition(3, Range.closed(28, 35))));
    }

    @Test
    public void nothingIsReported_whenKeywordWithVarargIsCalledWithMultipleArguments() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    1   2   3   4   5")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("arg", "*args");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void nothingIsReported_whenKeywordWithKwargsIsCalledWithMultipleArguments() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    1   a=2    b=3")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("arg", "**kwargs");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void nothingIsReported_whenDictionaryIsUsedWithNamedArguments() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    1   a=2    &{d}    b=3")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("arg", "**kwargs");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void positionalArgumentsAreReported_whenTheyWereMatchedToKwargsArgument() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    1   2   3")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("arg", "**kwargs");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ArgumentProblem.MISMATCHING_ARGUMENT, new ProblemPosition(3, Range.closed(43, 44))),
                new Problem(ArgumentProblem.MISMATCHING_ARGUMENT, new ProblemPosition(3, Range.closed(47, 48))));
    }

    @Test
    public void warningIsReported_whenListIsUsedInOrderToProvideMultipleArguments() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    @{variables}")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.LIST_ARGUMENT_SHOULD_PROVIDE_ARGS, new ProblemPosition(3, Range.closed(39, 51))));
    }

    @Test
    public void warningIsReported_whenDictionaryIsUsedInOrderToProvideMultipleArguments() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    &{variables}")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.DICT_ARGUMENT_SHOULD_PROVIDE_ARGS, new ProblemPosition(3, Range.closed(39, 51))));
    }

    @Test
    public void warningIsReported_whenListIsUsedInOrderToProvideMultipleArguments_2() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    ${a}=    keyword    @{variables}")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x", "y");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.LIST_ARGUMENT_SHOULD_PROVIDE_ARGS, new ProblemPosition(3, Range.closed(48, 60))));
    }

    @Test
    public void nothingIsReported_whenSomethingWhichSeemToBeAListIsUsed() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    @{b")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void nothingIsReported_whenSomethingWhichSeemToBeADictionaryIsUsed() {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    keyword    &{b")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("x");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    private void validate(final RobotSuiteFile file, final DefiningTokenWithArgumentTokens tokens,
            final ArgumentsDescriptor descriptor) {
        final KeywordCallArgumentsValidator validator = new GeneralKeywordCallArgumentsValidator(file.getFile(),
                tokens.definingToken, reporter, descriptor, tokens.argumentTokens);
        validator.validate(null);
    }

    private static DefiningTokenWithArgumentTokens getKeywordCallTokensFromFirstLineOf(final RobotSuiteFile file,
            final String caseName) {
        final Optional<RobotCasesSection> casesSection = file.findSection(RobotCasesSection.class);
        final RobotCase testCase = (RobotCase) casesSection.get().findChild(caseName);
        final RobotExecutableRow<TestCase> executable = testCase.getLinkedElement().getExecutionContext().get(0);
        final IExecutableRowDescriptor<?> executableRowDescriptor = executable.buildLineDescription();
        return new DefiningTokenWithArgumentTokens(executableRowDescriptor.getAction().getToken(),
                executableRowDescriptor.getKeywordArguments());
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
