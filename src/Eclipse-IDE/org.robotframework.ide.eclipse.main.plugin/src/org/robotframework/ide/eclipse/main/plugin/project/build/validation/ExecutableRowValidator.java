/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
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
        final RobotAction action = descriptor.getRowType() == ERowType.FOR_CONTINUE
                ? ((ForLoopContinueRowDescriptor<?>) descriptor).getKeywordAction()
                : descriptor.getAction();
        final RobotToken keywordNameToken = action.getToken();

        if (keywordNameToken.getFilePosition().isNotSet()) {
            reporter.handleProblem(
                    RobotProblem.causedBy(KeywordsProblem.MISSING_KEYWORD).formatMessageWith(row.getAction().getText()),
                    validationContext.getFile(), row.getAction());

        } else {
            final KeywordCallValidator keywordCallValidator = new KeywordCallValidator(validationContext,
                    keywordNameToken, descriptor.getKeywordArguments(), reporter);
            keywordCallValidator.validate();

            final QualifiedKeywordName keywordName = keywordCallValidator.getFoundKeywordName().orElse(null);
            final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);

            final List<VariableDeclaration> variableUsedInCall = SpecialKeywords.getUsedVariables(keywordName,
                    descriptor);
            unknownVarsValidator.reportUnknownVarsDeclarations(additionalVariables, variableUsedInCall);

            SpecialKeywords.getCreatedVariables(keywordName, descriptor)
                    .forEach(var -> additionalVariables.add(VariableNamesSupport.extractUnifiedVariableName(var)));
            descriptor.getCreatedVariables()
                    .forEach(var -> additionalVariables.add(VariableNamesSupport.extractUnifiedVariableName(var)));
        }
    }
}