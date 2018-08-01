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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;

import com.google.common.collect.Range;

class ScalarAsListInOlderRobotValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final IVariableHolder variable;

    private final ValidationReportingStrategy reporter;

    ScalarAsListInOlderRobotValidator(final IFile file, final IVariableHolder variable,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.variable = variable;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(2, 8));
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        if (variable.getType() == VariableType.SCALAR_AS_LIST) {
            final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.SCALAR_WITH_MULTIPLE_VALUES_2_7)
                    .formatMessageWith(variable.getName());
            reporter.handleProblem(problem, file, DictionaryExistenceValidator.toPositionOfWholeDefinition(variable));
        }
    }
}
