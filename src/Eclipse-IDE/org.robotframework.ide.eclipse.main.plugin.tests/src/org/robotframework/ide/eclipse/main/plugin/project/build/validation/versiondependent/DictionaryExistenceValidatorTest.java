/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext.RobotVersion;

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

}
