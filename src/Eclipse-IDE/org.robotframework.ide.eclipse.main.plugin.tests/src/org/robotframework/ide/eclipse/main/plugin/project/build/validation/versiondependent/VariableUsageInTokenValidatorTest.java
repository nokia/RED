/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.prepareContext;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class VariableUsageInTokenValidatorTest {

    @Test
    public void validatorIsOnlyApplicableToVersionsGiven() {
        final VariableUsageInTokenValidator validator = new VariableUsageInTokenValidator(null,
                Range.closed(new RobotVersion(3, 0), new RobotVersion(3, 2)), null, null);

        assertThat(validator.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(2, 9, 9))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0, 1))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0, 5))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 1, 7))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 2))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 2, 1))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 3))).isFalse();
    }

    @Test
    public void noProblemIsReported_whenThereAreNoVariablesInToken() {
        final FileValidationContext context = prepareContext();
        final RobotToken token = RobotToken.create("text");

        final MockReporter reporter = new MockReporter();
        final VariableUsageInTokenValidator validator = new VariableUsageInTokenValidator(context, null, token,
                reporter);

        validator.validate();

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void noProblemIsReported_whenVariableIsKnown() {
        final FileValidationContext context = prepareContext(newHashSet("${var}"));
        final RobotToken token = RobotToken.create("text with ${var} inside");

        final MockReporter reporter = new MockReporter();
        final VariableUsageInTokenValidator validator = new VariableUsageInTokenValidator(context, null, token,
                reporter);

        validator.validate();

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void problemIsReported_whenVariableIsUnknown() {
        final FileValidationContext context = prepareContext();
        final RobotToken token = RobotToken.create("text with ${var} inside");

        final MockReporter reporter = new MockReporter();
        final VariableUsageInTokenValidator validator = new VariableUsageInTokenValidator(context, null, token,
                reporter);

        validator.validate();

        assertThat(reporter.getReportedProblems()).hasSize(1)
                .extracting(Problem::getCause)
                .containsOnly(VariablesProblem.UNDECLARED_VARIABLE_USE);
        assertThat(reporter.getReportedProblems()).extracting(Problem::getMessage)
                .containsOnly("Variable 'var' is used, but not defined");
        assertThat(reporter.getReportedProblems()).extracting(Problem::getPosition)
                .containsOnly(new ProblemPosition(-1, Range.closed(9, 15)));
    }
}
