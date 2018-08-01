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
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.ScalarAsListInOlderRobotValidator;

import com.google.common.collect.Range;

public class ScalarAsListInOlderRobotValidatorTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }

    @Test
    public void validatorIsApplicableForVersionsUnder28() {
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(null, null, null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.0"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.6"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.7"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.7.10"))).isTrue();
    }

    @Test
    public void validatorIsNotApplicableForVersionsOver28() {
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(null, null, null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.8"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.8.1"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.0"))).isFalse();
    }

    @Test
    public void validatorThrowsProblem_whenScalarAsListVariableIsUsed() throws CoreException {
        final ScalarVariable variable = new ScalarVariable("scalar_as_list", new RobotToken(),
                VariableScope.TEST_CASE);
        variable.addValue(new RobotToken());
        variable.addValue(new RobotToken());
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(null, variable,
                reporter);

        validator.validate(null);
        
        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                VariablesProblem.SCALAR_WITH_MULTIPLE_VALUES_2_7, new ProblemPosition(-1, Range.closed(-1, -1))));
    }

    @Test
    public void validatorDoesNothing_whenScalarVariableIsUsed() throws CoreException {
        final ScalarVariable variable = new ScalarVariable("scalar", new RobotToken(), VariableScope.TEST_CASE);
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(null, variable,
                reporter);

        validator.validate(null);
        assertThat(reporter.wasProblemReported()).isFalse();
    }

    @Test
    public void validatorDoesNothing_whenListVariableIsUsed() throws CoreException {
        final IVariableHolder variable = new ListVariable("list", new RobotToken(), VariableScope.TEST_CASE);
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(null, variable,
                reporter);

        validator.validate(null);
        assertThat(reporter.wasProblemReported()).isFalse();
    }

    @Test
    public void validatorDoesNothing_whenDictionaryVariableIsUsed() throws CoreException {
        final IVariableHolder variable = new DictionaryVariable("dict", new RobotToken(), VariableScope.TEST_CASE);
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(null, variable,
                reporter);

        validator.validate(null);
        assertThat(reporter.wasProblemReported()).isFalse();
    }
}
