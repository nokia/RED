/*
 * Copyright 2017 Nokia Solutions and Networks
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

public class VariableSetterOrGetterCallArgumentsValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void nothingIsReported_whenVariableSetterOrGetterIsCalledWithScalarVariableAsFirstArgument() {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Or Get Variable    ${x}  v")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("var", "*args");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariableSetterOrGetterIsCalledWithListVariableAsFirstArgument() {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Or Get Variable    @{x}  v1  v2")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("var", "*args");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void nothingIsReported_whenVariableSetterOrGetterIsCalledWithDictionaryVariableAsFirstArgument() {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Or Get Variable    &{x}  k1=v1")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("var", "*args");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void invalidVariableSyntaxIsReported_whenVariableSetterOrGetterIsCalledWithoutVariableAsFirstArgument() {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Or Get Variable    &b  arg")
                .build();

        final DefiningTokenWithArgumentTokens tokens = getKeywordCallTokensFromFirstLineOf(file, "test");
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("var", "*args");

        validate(file, tokens, descriptor);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ArgumentProblem.INVALID_VARIABLE_SYNTAX, new ProblemPosition(3, Range.closed(51, 53))));
    }

    private void validate(final RobotSuiteFile file, final DefiningTokenWithArgumentTokens tokens,
            final ArgumentsDescriptor descriptor) {
        final KeywordCallArgumentsValidator validator = new VariableSetterOrGetterCallArgumentsValidator(file.getFile(),
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
