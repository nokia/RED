/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.RowType;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

interface ExecutableValidator extends ModelUnitValidator {

    static ExecutableValidator of(final FileValidationContext validationContext, final Set<String> additionalVariables,
            final AModelElement<?> modelElement, final ValidationReportingStrategy reporter) {

        if (modelElement instanceof AKeywordBaseSetting<?>) {
            return new ExecutableSetupOrTeardownValidator(validationContext, additionalVariables,
                    (AKeywordBaseSetting<?>) modelElement, reporter);

        } else if (modelElement instanceof LocalSetting<?> &&
                (modelElement.getModelType() == ModelType.TASK_SETUP
                || modelElement.getModelType() == ModelType.TASK_TEARDOWN
                || modelElement.getModelType() == ModelType.TEST_CASE_SETUP
                || modelElement.getModelType() == ModelType.TEST_CASE_TEARDOWN)
                || modelElement.getModelType() == ModelType.USER_KEYWORD_TEARDOWN) {
            return new ExecutableSetupOrTeardownValidator(validationContext, additionalVariables,
                    ((LocalSetting<?>) modelElement).adaptTo(ExecutableSetting.class), reporter);

        } else if (modelElement instanceof RobotExecutableRow<?>) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) modelElement;
            final IExecutableRowDescriptor<?> descriptor = row.buildLineDescription();

            if (descriptor.getRowType() == RowType.SIMPLE || descriptor.getRowType() == RowType.FOR_CONTINUE) {
                return new ExecutableRowValidator(validationContext, additionalVariables, row, descriptor, reporter);

            } else if (descriptor.getRowType() == RowType.FOR) {
                return new ExecutableForValidator(validationContext, additionalVariables, descriptor, reporter);

            } else if (descriptor.getRowType() == RowType.FOR_END) {
                return new ExecutableValidator() {
                    @Override
                    public void validate(final IProgressMonitor monitor) throws CoreException {
                        // nothing to validate
                    }
                };
            }
        }
        throw new IllegalStateException();
    }
}