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

public class DictionaryExistenceValidatorTest {

    @Test
    public void validatorIsApplicableForVersionsUnder29() {
        final DictionaryExistenceValidator validator = new DictionaryExistenceValidator(null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.0"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.7"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.8"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.8.10"))).isTrue();
    }

    @Test
    public void validatorIsNotApplicableForVersionsOver29() {
        final DictionaryExistenceValidator validator = new DictionaryExistenceValidator(null);

        assertThat(validator.isApplicableFor(RobotVersion.from("2.9"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("2.9.1"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("3.0"))).isFalse();
    }

    @Test(expected = ValidationProblemException.class)
    public void validatorThrowsProblem_whenDictionaryVariableIsUsed() throws CoreException {
        final IVariableHolder variable = new DictionaryVariable("dir", new RobotToken(), VariableScope.TEST_CASE);
        final DictionaryExistenceValidator validator = new DictionaryExistenceValidator(variable);

        validator.validate(null);
    }

    @Test
    public void validatorDoesNothing_whenListVariableIsUsed() throws CoreException {
        final IVariableHolder variable = new ListVariable("list", new RobotToken(), VariableScope.TEST_CASE);
        final DictionaryExistenceValidator validator = new DictionaryExistenceValidator(variable);

        validator.validate(null);
    }

    @Test
    public void validatorDoesNothing_whenScalarVariableIsUsed() throws CoreException {
        final IVariableHolder variable = new ScalarVariable("scalar", new RobotToken(), VariableScope.TEST_CASE);
        final DictionaryExistenceValidator validator = new DictionaryExistenceValidator(variable);

        validator.validate(null);
    }

}
