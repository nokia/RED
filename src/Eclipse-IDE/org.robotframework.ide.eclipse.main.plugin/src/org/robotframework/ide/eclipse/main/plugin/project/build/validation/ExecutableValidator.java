/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Set;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

interface ExecutableValidator extends ModelUnitValidator {

    static ExecutableValidator of(final FileValidationContext validationContext, final Set<String> additionalVariables,
            final AModelElement<?> modelElement, final ValidationReportingStrategy reporter) {

        if (modelElement instanceof AKeywordBaseSetting<?>) {
            return new ExecutableSetupOrTeardownValidator(validationContext, additionalVariables,
                    (AKeywordBaseSetting<?>) modelElement, reporter);

        } else if (modelElement instanceof RobotExecutableRow<?>) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) modelElement;
            final IExecutableRowDescriptor<?> descriptor = row.buildLineDescription();

            if (descriptor.getRowType() == ERowType.SIMPLE || descriptor.getRowType() == ERowType.FOR_CONTINUE) {
                return new ExecutableRowValidator(validationContext, additionalVariables, row, descriptor, reporter);

            } else if (descriptor.getRowType() == ERowType.FOR) {
                return new ExecutableForValidator(validationContext, additionalVariables, descriptor, reporter);
            }
        }
        throw new IllegalStateException();
    }
}