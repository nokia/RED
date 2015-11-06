/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.model.table.variables.ScalarVariable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationProblemException;

public class ScalarAsListInOlderRobotValidatorTest {

    @Test
    public void validatorIsApplicableForVersionsUnder28() {
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.0"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.6"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.7"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.7.10"))).isTrue();
    }

    @Test
    public void validatorIsNotApplicableForVersionsOver28() {
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.8"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.8.1"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.0"))).isFalse();
    }

    @Test(expected = ValidationProblemException.class)
    public void validatorThrowsProblem_whenScalarAsListVariableIsUsed() throws CoreException {
        final ScalarVariable variable = new ScalarVariable("scalar_as_list", new RobotToken(),
                VariableScope.TEST_CASE);
        variable.addValue(new RobotToken());
        variable.addValue(new RobotToken());
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(variable);

        validator.validate(null);
    }

    @Test
    public void validatorDoesNothing_whenScalarVariableIsUsed() throws CoreException {
        final ScalarVariable variable = new ScalarVariable("scalar", new RobotToken(), VariableScope.TEST_CASE);
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(variable);

        validator.validate(null);
    }

    @Test
    public void validatorDoesNothing_whenListVariableIsUsed() throws CoreException {
        final IVariableHolder variable = new ListVariable("list", new RobotToken(), VariableScope.TEST_CASE);
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(variable);

        validator.validate(null);
    }

    @Test
    public void validatorDoesNothing_whenDictionaryVariableIsUsed() throws CoreException {
        final IVariableHolder variable = new DictionaryVariable("dict", new RobotToken(), VariableScope.TEST_CASE);
        final ScalarAsListInOlderRobotValidator validator = new ScalarAsListInOlderRobotValidator(variable);

        validator.validate(null);
    }
}
