/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

class ExecutableSetupOrTeardownValidator implements ExecutableValidator {

    private final FileValidationContext validationContext;
    private final Set<String> additionalVariables;
    private final ValidationReportingStrategy reporter;

    private final ExecutableSetting setupOrTeardown;


    ExecutableSetupOrTeardownValidator(final FileValidationContext validationContext,
            final Set<String> additionalVariables, final ExecutableSetting setupOrTeardown,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.additionalVariables = additionalVariables;
        this.setupOrTeardown = setupOrTeardown;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        if (setupOrTeardown.getKeywordName() == null) {
            return;
        }
        final RobotExecutableRow<?> row = setupOrTeardown.asExecutableRow();
        final IExecutableRowDescriptor<?> descriptor = row.buildLineDescription();

        final RobotToken keywordNameToken = setupOrTeardown.getKeywordName();
        final MappingResult variablesExtraction = new VariableExtractor().extract(keywordNameToken,
                validationContext.getFile().getName());
        final List<VariableDeclaration> variablesDeclarations = variablesExtraction.getCorrectVariables();

        if (variablesExtraction.getMappedElements().size() == 1 && variablesDeclarations.size() == 1) {
            final RobotProblem problem = RobotProblem
                    .causedBy(KeywordsProblem.KEYWORD_NAME_IS_PARAMETERIZED)
                    .formatMessageWith(variablesDeclarations.get(0).getVariableName().getText(), "");
            reporter.handleProblem(problem, validationContext.getFile(), keywordNameToken);

            final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);
            unknownVarsValidator.reportUnknownVarsDeclarations(additionalVariables, descriptor.getUsedVariables());

        } else if (!keywordNameToken.getText().equalsIgnoreCase("none")) {
            new ExecutableNestedRowValidator(validationContext, additionalVariables, row, descriptor, reporter)
                    .validate(monitor);
        }
    }
}