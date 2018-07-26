/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

class ExecutableRowValidator implements ExecutableValidator {

    private final FileValidationContext validationContext;
    private final Set<String> additionalVariables;
    private final ValidationReportingStrategy reporter;
    
    private final RobotExecutableRow<?> row;
    private final IExecutableRowDescriptor<?> descriptor;


    public ExecutableRowValidator(final FileValidationContext validationContext, final Set<String> additionalVariables,
            final RobotExecutableRow<?> row, final IExecutableRowDescriptor<?> descriptor,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.additionalVariables = additionalVariables;
        this.row = row;
        this.descriptor = descriptor;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        final RobotToken keywordNameToken = descriptor.getKeywordAction().getToken();

        if (keywordNameToken.getFilePosition().isNotSet()) {
            reporter.handleProblem(
                    RobotProblem.causedBy(KeywordsProblem.MISSING_KEYWORD).formatMessageWith(row.getAction().getText()),
                    validationContext.getFile(), row.getAction());

        } else {
            new ExecutableNestedRowValidator(validationContext, additionalVariables, row, descriptor, reporter)
                    .validate(monitor);
        }
    }
}