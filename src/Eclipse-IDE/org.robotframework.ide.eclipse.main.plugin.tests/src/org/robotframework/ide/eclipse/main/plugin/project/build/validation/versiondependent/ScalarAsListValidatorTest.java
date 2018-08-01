/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.ScalarAsListValidator;

import com.google.common.collect.Range;

public class ScalarAsListValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void validatorIsNotApplicableForVersionsUnder28() {
        final ScalarAsListValidator validator = new ScalarAsListValidator(null, null, null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.0"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.6"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.7"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.7.10"))).isFalse();
    }

    @Test
    public void validatorIsNotApplicableForVersionsOver28() {
        final ScalarAsListValidator validator = new ScalarAsListValidator(null, null, null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.9"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.9.0"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.9.5"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.0"))).isFalse();
    }

    @Test
    public void validatorIsApplicableForVersions28x() {
        final ScalarAsListValidator validator = new ScalarAsListValidator(null, null, null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.8"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.8.0"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.8.1"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.8.100"))).isTrue();
    }

    @Test
    public void validatorThrowsProblem_whenScalarAsListVariableIsUsed() throws CoreException {
        final ScalarVariable variable = new ScalarVariable("scalar_as_list", new RobotToken(), VariableScope.TEST_CASE);
        variable.addValue(new RobotToken());
        variable.addValue(new RobotToken());
        final ScalarAsListValidator validator = new ScalarAsListValidator(null, variable, reporter);

        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                VariablesProblem.SCALAR_WITH_MULTIPLE_VALUES_2_8_x, new ProblemPosition(-1, Range.closed(-1, -1))));
    }

    @Test
    public void validatorDoesNothing_whenScalarVariableIsUsed() throws CoreException {
        final ScalarVariable variable = new ScalarVariable("scalar", new RobotToken(), VariableScope.TEST_CASE);
        final ScalarAsListValidator validator = new ScalarAsListValidator(null, variable, reporter);

        validator.validate(null);
        assertThat(reporter.wasProblemReported()).isFalse();
    }

    @Test
    public void validatorDoesNothing_whenListVariableIsUsed() throws CoreException {
        final IVariableHolder variable = new ListVariable("list", new RobotToken(), VariableScope.TEST_CASE);
        final ScalarAsListValidator validator = new ScalarAsListValidator(null, variable, reporter);

        validator.validate(null);
        assertThat(reporter.wasProblemReported()).isFalse();
    }

    @Test
    public void validatorDoesNothing_whenDictionaryVariableIsUsed() throws CoreException {
        final IVariableHolder variable = new DictionaryVariable("dict", new RobotToken(), VariableScope.TEST_CASE);
        final ScalarAsListValidator validator = new ScalarAsListValidator(null, variable, reporter);

        validator.validate(null);
        assertThat(reporter.wasProblemReported()).isFalse();
    }
}
