/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext.RobotVersion;

import com.google.common.collect.Range;

public class VersionDependentModelUnitValidatorTest {

    @Test
    public void validatorIsApplicable_whenVersionIsInsideTheRange() {
        final VersionDependentModelUnitValidator validator = createValidator(Range.atMost(RobotVersion.from("1.2.3")));

        assertThat(validator.isApplicableFor(RobotVersion.from("1.2.3"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("1.2.1"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("1.1.1"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("1.1"))).isTrue();
        assertThat(validator.isApplicableFor(RobotVersion.from("0.7"))).isTrue();
    }

    @Test
    public void validatorIsApplicable_whenVersionIsOutsideTheRange() {
        final VersionDependentModelUnitValidator validator = createValidator(Range.atLeast(RobotVersion.from("1.2.3")));

        assertThat(validator.isApplicableFor(RobotVersion.from("1.2.1"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("1.1.1"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("1.1"))).isFalse();
        assertThat(validator.isApplicableFor(RobotVersion.from("0.7"))).isFalse();
    }

    private static VersionDependentModelUnitValidator createValidator(final Range<RobotVersion> applicableRange) {
        return new VersionDependentModelUnitValidator() {

            @Override
            public void validate(final IProgressMonitor monitor) throws CoreException {
                // nothing to do
            }

            @Override
            protected Range<RobotVersion> getApplicableVersionRange() {
                return applicableRange;
            }
        };
    }

}
