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
            final RobotSettingsSection section, final ProblemsReportingStrategy reporter, final RobotVersion version) {
        final List<VersionDependentModelUnitValidator> allValidators = newArrayList(
                new DuplicatedTemplateInOlderValidator(file, section, reporter),
                new DuplicatedTemplateValidator(file, section, reporter),
                new DuplicatedSuiteSetupInOlderValidator(file, section, reporter),
                new DuplicatedSuiteSetupValidator(file, section, reporter),
                new DuplicatedSuiteTeardownInOlderValidator(file, section, reporter),
                new DuplicatedSuiteTeardownValidator(file, section, reporter),
                new DuplicatedTestSetupInOlderValidator(file, section, reporter),
                new DuplicatedTestSetupValidator(file, section, reporter),
                new DuplicatedTestTeardownInOlderValidator(file, section, reporter),
                new DuplicatedTestTeardownValidator(file, section, reporter),
                new DuplicatedTestTimeoutInOlderValidator(file, section, reporter),
                new DuplicatedTestTimeoutValidator(file, section, reporter),
                new DuplicatedForceTagsInOlderValidator(file, section, reporter),
                new DuplicatedForceTagsValidator(file, section, reporter),
                new DuplicatedDefaultTagsInOlderValidator(file, section, reporter),
                new DuplicatedDefaultTagsValidator(file, section, reporter),
                new DuplicatedDocumentationInOlderValidator(file, section, reporter),
                new DuplicatedDocumentationValidator(file, section, reporter));

        return newArrayList(Iterables.filter(allValidators, new Predicate<VersionDependentModelUnitValidator>() {

            @Override
            public boolean apply(final VersionDependentModelUnitValidator validator) {
                return validator.isApplicableFor(version);
            }
        }));
    }
}
