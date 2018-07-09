/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

class ExecutableSetupOrTeardownValidator implements ExecutableValidator {

    private final FileValidationContext validationContext;
    private final Set<String> additionalVariables;
    private final ValidationReportingStrategy reporter;

    private final AKeywordBaseSetting<?> setupOrTeardown;


    ExecutableSetupOrTeardownValidator(final FileValidationContext validationContext,
            final Set<String> additionalVariables, final AKeywordBaseSetting<?> setupOrTeardown,
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
        final IExecutableRowDescriptor<?> descriptor = setupOrTeardown.asExecutableRow().buildLineDescription();

        final RobotToken keywordNameToken = setupOrTeardown.getKeywordName();
        final MappingResult variablesExtraction = new VariableExtractor().extract(keywordNameToken,
                validationContext.getFile().getName());
        final List<VariableDeclaration> variablesDeclarations = variablesExtraction.getCorrectVariables();

        if (variablesExtraction.getMappedElements().size() == 1 && variablesDeclarations.size() == 1) {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING)
                    .formatMessageWith(variablesDeclarations.get(0).getVariableName().getText());
            reporter.handleProblem(problem, validationContext.getFile(), keywordNameToken);

            final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);
            unknownVarsValidator.reportUnknownVarsDeclarations(additionalVariables, descriptor.getUsedVariables());

        } else {
            final KeywordCallValidator keywordCallValidator = new KeywordCallValidator(validationContext,
                    keywordNameToken, setupOrTeardown.getArguments(), reporter);
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