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
import org.robotframework.ide.eclipse.main.plugin.project.build.AttributesAugmentingReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.SpecialKeywords.NestedExecutables;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.SpecialKeywords.NestedKeywordsSyntaxException;

class ExecutableNestedRowValidator implements ExecutableValidator {

    private final FileValidationContext validationContext;
    private final Set<String> additionalVariables;
    private final ValidationReportingStrategy reporter;
    
    private final RobotExecutableRow<?> nestedRow;
    private final IExecutableRowDescriptor<?> descriptor;


    public ExecutableNestedRowValidator(final FileValidationContext validationContext,
            final Set<String> additionalVariables, final RobotExecutableRow<?> nestedRow,
            final IExecutableRowDescriptor<?> descriptor, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.additionalVariables = additionalVariables;
        this.nestedRow = nestedRow;
        this.descriptor = descriptor;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        final RobotAction action = descriptor.getRowType() == ERowType.FOR_CONTINUE
                ? ((ForLoopContinueRowDescriptor<?>) descriptor).getKeywordAction()
                : descriptor.getAction();
        final RobotToken keywordNameToken = action.getToken();

        final KeywordCallValidator keywordCallValidator = new KeywordCallValidator(validationContext, keywordNameToken,
                descriptor.getKeywordArguments(), reporter);
        keywordCallValidator.validate(monitor);
        final QualifiedKeywordName keywordName = keywordCallValidator.getFoundKeywordName().orElse(null);

        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext,
                AttributesAugmentingReportingStrategy.withLocalVarFixer(reporter));

        try {
            final NestedExecutables nested = SpecialKeywords.getNestedExecutables(keywordName, nestedRow.getParent(),
                    descriptor.getKeywordArguments());
            if (nested.hasNestedExecutables()) {
                for (final RobotExecutableRow<?> nestedRow : nested.getExecutables()) {
                    new ExecutableNestedRowValidator(validationContext, additionalVariables, nestedRow,
                            nestedRow.buildLineDescription(), reporter).validate(monitor);
                }
                unknownVarsValidator.reportUnknownVars(additionalVariables, nested.getOmmittedTokens());

            } else {
                final List<VariableDeclaration> variableUsedInCall = SpecialKeywords.getUsedVariables(keywordName,
                        descriptor);
                unknownVarsValidator.reportUnknownVarsDeclarations(additionalVariables, variableUsedInCall);
            }

            SpecialKeywords.getCreatedVariables(keywordName, descriptor)
                    .forEach(var -> additionalVariables.add(VariableNamesSupport.extractUnifiedVariableName(var)));

        } catch (final NestedKeywordsSyntaxException e) {
            e.forEachProblem((msg, token) -> {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.INVALID_NESTED_EXECUTABLES_SYNTAX)
                        .formatMessageWith(msg);
                reporter.handleProblem(problem, validationContext.getFile(), token);
            });
        }
    }
}