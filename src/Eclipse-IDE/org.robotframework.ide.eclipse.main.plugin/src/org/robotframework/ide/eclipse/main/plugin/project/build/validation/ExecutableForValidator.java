/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.AttributesAugmentingReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

import com.google.common.collect.Range;


public class ExecutableForValidator implements ExecutableValidator {

    private final FileValidationContext validationContext;
    private final Set<String> additionalVariables;
    private final ValidationReportingStrategy reporter;

    private final IExecutableRowDescriptor<?> descriptor;

    public ExecutableForValidator(final FileValidationContext validationContext, final Set<String> additionalVariables,
            final IExecutableRowDescriptor<?> descriptor, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.additionalVariables = additionalVariables;
        this.descriptor = descriptor;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final BuildMessage buildMessage : descriptor.getMessages()) {
            final ProblemPosition position = new ProblemPosition(buildMessage.getFileRegion().getStart().getLine(),
                    Range.closed(buildMessage.getFileRegion().getStart().getOffset(),
                            buildMessage.getFileRegion().getEnd().getOffset()));
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.INVALID_FOR_KEYWORD)
                    .formatMessageWith(buildMessage.getMessage());
            reporter.handleProblem(problem, validationContext.getFile(), position);
        }
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext,
                AttributesAugmentingReportingStrategy.withLocalVarFixer(reporter));
        unknownVarsValidator.reportUnknownVarsDeclarations(additionalVariables, descriptor.getUsedVariables());

        descriptor.getCreatedVariables()
                .forEach(var -> additionalVariables.add(VariableNamesSupport.extractUnifiedVariableName(var)));
    }
}
