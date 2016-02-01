/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.variables.DictionaryExistenceValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.variables.ScalarAsListInOlderRobotValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.variables.ScalarAsListValidator;

public class VersionDependentValidatorsTest {

    @Test
    public void properValidatorsAreReturnedForVersionsUnder28() {
        final VersionDependentValidators validators = new VersionDependentValidators();

        final List<? extends ModelUnitValidator> applicableValidators = validators.getVariableValidators(null, null,
                null, RobotVersion.from("2.7.7"));
        assertThat(applicableValidators.size()).isEqualTo(2);
        assertThat(applicableValidators.get(0)).isInstanceOf(DictionaryExistenceValidator.class);
        assertThat(applicableValidators.get(1)).isInstanceOf(ScalarAsListInOlderRobotValidator.class);
    }

    @Test
    public void properValidatorsAreReturnedForVersions28() {
        final VersionDependentValidators validators = new VersionDependentValidators();

        final List<? extends ModelUnitValidator> applicableValidators = validators.getVariableValidators(null, null,
                null, RobotVersion.from("2.8.0"));
        assertThat(applicableValidators.size()).isEqualTo(2);
        assertThat(applicableValidators.get(0)).isInstanceOf(DictionaryExistenceValidator.class);
        assertThat(applicableValidators.get(1)).isInstanceOf(ScalarAsListValidator.class);
    }

    @Test
    public void properValidatorsAreReturnedForVersions29() {
        final VersionDependentValidators validators = new VersionDependentValidators();

        final List<? extends ModelUnitValidator> applicableValidators = validators.getVariableValidators(null, null,
                null, RobotVersion.from("2.9"));
        assertThat(applicableValidators).isEmpty();
    }
}
