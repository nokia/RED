/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class VersionDependentValidators {

    public List<? extends ModelUnitValidator> getVariableValidators(final IVariableHolder variable,
            final ValidationContext.RobotVersion version) {
        final List<VersionDependentModelUnitValidator> allValidators = newArrayList(
                new DictionaryExistenceValidator(variable),
                new ScalarAsListInOlderRobotValidator(variable),
                new ScalarAsListValidator(variable)
        );
        
        return newArrayList(Iterables.filter(allValidators, new Predicate<VersionDependentModelUnitValidator>() {
            @Override
            public boolean apply(final VersionDependentModelUnitValidator validator) {
                return validator.isApplicableFor(version);
            }
        }));
    }
}
