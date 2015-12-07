/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class VersionDependentValidators {

    public List<? extends ModelUnitValidator> getVariableValidators(final IFile file, final IVariableHolder variable,
            final ProblemsReportingStrategy reporter, final RobotVersion version) {
        final List<VersionDependentModelUnitValidator> allValidators = newArrayList(
                new DictionaryExistenceValidator(file, variable, reporter),
                new ScalarAsListInOlderRobotValidator(file, variable, reporter),
                new ScalarAsListValidator(file, variable, reporter));

        return newArrayList(Iterables.filter(allValidators, new Predicate<VersionDependentModelUnitValidator>() {

            @Override
            public boolean apply(final VersionDependentModelUnitValidator validator) {
                return validator.isApplicableFor(version);
            }
        }));
    }

    public List<? extends ModelUnitValidator> getGeneralSettingsValidators(final IFile file,
            final RobotSettingsSection section, final RobotVersion version) {
        final List<VersionDependentModelUnitValidator> allValidators = newArrayList(
                new DuplicatedTemplateInOlderValidator(file, section), new DuplicatedTemplateValidator(file, section),
                new DuplicatedSuiteSetupInOlderValidator(file, section),
                new DuplicatedSuiteSetupValidator(file, section),
                new DuplicatedSuiteTeardownInOlderValidator(file, section),
                new DuplicatedSuiteTeardownValidator(file, section),
                new DuplicatedTestSetupInOlderValidator(file, section), new DuplicatedTestSetupValidator(file, section),
                new DuplicatedTestTeardownInOlderValidator(file, section),
                new DuplicatedTestTeardownValidator(file, section),
                new DuplicatedTestTimeoutInOlderValidator(file, section),
                new DuplicatedTestTimeoutValidator(file, section),
                new DuplicatedForceTagsInOlderValidator(file, section), new DuplicatedForceTagsValidator(file, section),
                new DuplicatedDefaultTagsInOlderValidator(file, section),
                new DuplicatedDefaultTagsValidator(file, section),
                new DuplicatedDocumentationInOlderValidator(file, section),
                new DuplicatedDocumentationValidator(file, section));

        return newArrayList(Iterables.filter(allValidators, new Predicate<VersionDependentModelUnitValidator>() {

            @Override
            public boolean apply(final VersionDependentModelUnitValidator validator) {
                return validator.isApplicableFor(version);
            }
        }));
    }
}
