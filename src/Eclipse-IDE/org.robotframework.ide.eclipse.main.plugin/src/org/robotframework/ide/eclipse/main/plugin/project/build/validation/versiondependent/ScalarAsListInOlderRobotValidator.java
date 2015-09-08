/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationProblemException;

import com.google.common.collect.Range;

class ScalarAsListInOlderRobotValidator extends VersionDependentModelUnitValidator {

    private final IVariableHolder variable;

    ScalarAsListInOlderRobotValidator(final IVariableHolder variable) {
        this.variable = variable;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        // run only for versions older than 2.8.x
        return Range.lessThan(new RobotVersion(2, 8));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws ValidationProblemException {
        if (variable.getType() == VariableType.SCALAR_AS_LIST) {
            throw new ValidationProblemException(RobotProblem.causedBy(VariablesProblem.SCALAR_WITH_MULTIPLE_VALUES_2_7)
                    .formatMessageWith(variable.getName()), true);
        }
    }
}
