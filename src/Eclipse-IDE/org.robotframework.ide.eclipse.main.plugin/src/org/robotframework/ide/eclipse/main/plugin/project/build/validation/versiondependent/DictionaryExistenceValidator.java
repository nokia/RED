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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationProblemException;

import com.google.common.collect.Range;


class DictionaryExistenceValidator extends VersionDependentModelUnitValidator {

    private final IVariableHolder variable;

    DictionaryExistenceValidator(final IVariableHolder variable) {
        this.variable = variable;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        // run only up to versions 2.8.x
        return Range.lessThan(new RobotVersion(2, 9));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (variable.getType() == VariableType.DICTIONARY) {
            throw new ValidationProblemException(RobotProblem.causedBy(VariablesProblem.DICTIONARY_NOT_AVAILABLE)
                    .formatMessageWith(variable.getName()), true);
        }
    }

}
