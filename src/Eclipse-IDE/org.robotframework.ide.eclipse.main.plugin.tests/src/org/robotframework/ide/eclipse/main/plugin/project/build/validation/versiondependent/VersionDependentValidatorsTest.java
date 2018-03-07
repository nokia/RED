/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.variables.DictionaryExistenceValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.variables.ScalarAsListInOlderRobotValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.variables.ScalarAsListValidator;

public class VersionDependentValidatorsTest {

    @Test
    public void properValidatorsAreReturnedForVersionsUnder28() {
        final VersionDependentValidators validators = new VersionDependentValidators();

        final List<? extends ModelUnitValidator> applicableValidators = newArrayList(
                validators.getVariableValidators(prepareContext("2.7.7"), null, null));
        assertThat(applicableValidators.size()).isEqualTo(2);
        assertThat(applicableValidators.get(0)).isInstanceOf(DictionaryExistenceValidator.class);
        assertThat(applicableValidators.get(1)).isInstanceOf(ScalarAsListInOlderRobotValidator.class);
    }

    @Test
    public void properValidatorsAreReturnedForVersions28() {
        final VersionDependentValidators validators = new VersionDependentValidators();

        final List<? extends ModelUnitValidator> applicableValidators = newArrayList(
                validators.getVariableValidators(prepareContext("2.8.0"), null, null));
        assertThat(applicableValidators.size()).isEqualTo(2);
        assertThat(applicableValidators.get(0)).isInstanceOf(DictionaryExistenceValidator.class);
        assertThat(applicableValidators.get(1)).isInstanceOf(ScalarAsListValidator.class);
    }

    @Test
    public void properValidatorsAreReturnedForVersions29() {
        final VersionDependentValidators validators = new VersionDependentValidators();

        final List<? extends ModelUnitValidator> applicableValidators = newArrayList(
                validators.getVariableValidators(prepareContext("2.9"), null, null));
        assertThat(applicableValidators).isEmpty();
    }

    private static FileValidationContext prepareContext(final String version) {
        final ValidationContext context = new ValidationContext(null, null, RobotVersion.from(version), null, null,
                null);
        return new FileValidationContext(context, mock(IFile.class));
    }
}
