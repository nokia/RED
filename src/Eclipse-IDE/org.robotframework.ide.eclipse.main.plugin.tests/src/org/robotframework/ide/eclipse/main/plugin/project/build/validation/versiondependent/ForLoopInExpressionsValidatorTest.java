/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class ForLoopInExpressionsValidatorTest {

    private MockReporter reporter;

    @BeforeEach
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void validatorIsNotApplicableForVersionsUnder31() {
        final ForLoopInExpressionsValidator validator = new ForLoopInExpressionsValidator(null, null, null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.0"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.7"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.0"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.0.10"))).isFalse();
    }

    @Test
    public void validatorIsApplicableForVersionsOver31() {
        final ForLoopInExpressionsValidator validator = new ForLoopInExpressionsValidator(null, null, null);

        assertThat(validator.isApplicableFor(RobotVersion.from("3.1"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.1.1"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.2"))).isTrue();
    }

    @Test
    public void validatorDoesNothing_whenThereIsNoInExpression() {
        final ForLoopDeclarationRowDescriptor<?> desc = createLoopDescriptor("FOR", "${i}");

        final ForLoopInExpressionsValidator validator = new ForLoopInExpressionsValidator(null, desc, reporter);
        validator.validate();

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void validatorDoesNothing_whenInExpressionIsTypedCorrectly() {
        final ForLoopDeclarationRowDescriptor<?> desc = createLoopDescriptor("FOR", "${i}", "IN ZIP", "source");

        final ForLoopInExpressionsValidator validator = new ForLoopInExpressionsValidator(null, desc, reporter);
        validator.validate();

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void validatorReportsProblem_whenInExpressionIsTypedWrongly() {
        final ForLoopDeclarationRowDescriptor<?> desc = createLoopDescriptor("FOR", "${i}", "in range", "source");

        final ForLoopInExpressionsValidator validator = new ForLoopInExpressionsValidator(null, desc, reporter);
        validator.validate();

        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.FOR_IN_EXPR_WRONGLY_TYPED, new ProblemPosition(3, Range.closed(43, 51))));
    }

    private static ForLoopDeclarationRowDescriptor<?> createLoopDescriptor(final String... forLoopCells) {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 1)).appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    " + String.join("    ", forLoopCells))
                .appendLine("    \\    Log    1")
                .build();
        final RobotExecutableRow<?> forRow = (RobotExecutableRow<?>) model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0)
                .getLinkedElement();
        return (ForLoopDeclarationRowDescriptor<?>) forRow.buildLineDescription();
    }
}
