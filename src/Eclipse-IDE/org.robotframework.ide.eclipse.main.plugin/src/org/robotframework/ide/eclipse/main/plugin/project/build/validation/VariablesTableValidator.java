/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

class VariablesTableValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final Optional<RobotVariablesSection> variablesSection;

    private final ProblemsReportingStrategy reporter;

    private final VersionDependentValidators versionDependentValidators;

    VariablesTableValidator(final FileValidationContext validationContext,
            final Optional<RobotVariablesSection> variablesSection, final ProblemsReportingStrategy reportingStrategy) {
        this(validationContext, variablesSection, reportingStrategy, new VersionDependentValidators());
    }

    @VisibleForTesting
    VariablesTableValidator(final FileValidationContext validationContext,
            final Optional<RobotVariablesSection> variablesSection, final ProblemsReportingStrategy reportingStrategy,
            final VersionDependentValidators versionDependentValidators) {
        this.validationContext = validationContext;
        this.variablesSection = variablesSection;
        this.reporter = reportingStrategy;
        this.versionDependentValidators = versionDependentValidators;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!variablesSection.isPresent()) {
            return;
        }
        final RobotSuiteFile suiteModel = variablesSection.get().getSuiteFile();
        final VariableTable variableTable = (VariableTable) variablesSection.get().getLinkedElement();
        reportVersionSpecificProblems(suiteModel.getFile(), variableTable, monitor);
        
        reportInvalidVariableName(suiteModel.getFile(), variableTable);
        reportUnknownVariableTypes(suiteModel.getFile(), variableTable);
        reportDuplicatedVariables(suiteModel.getFile(), variableTable);
    }

	private void reportVersionSpecificProblems(final IFile file, final VariableTable variableTable,
            final IProgressMonitor monitor) throws CoreException {
        for (final IVariableHolder variable : variableTable.getVariables()) {
            final List<? extends ModelUnitValidator> validators = versionDependentValidators.getVariableValidators(file,
                    variable, reporter, validationContext.getVersion());
            for (final ModelUnitValidator validator : validators) {
                validator.validate(monitor);
            }
        }
    }

    private void reportInvalidVariableName(final IFile file, final VariableTable variableTable) {
        for (final IVariableHolder variable : variableTable.getVariables()) {
            final VariableType varType = variable.getType();
            if (varType != VariableType.INVALID) {
                final String variableName = variable.getDeclaration().getRaw().toString();
                if (variableName != null && variableName.length() >= 2) {
                    if (variableName.charAt(1) != '{') {
                        final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.INVALID_NAME)
                                .formatMessageWith(variableName);
                        final Map<String, Object> attributes = ImmutableMap
                                .<String, Object> of(AdditionalMarkerAttributes.NAME, variableName);
                        reporter.handleProblem(problem, file, variable.getDeclaration(), attributes);
                    }
                }
            }
        }
    }

    private void reportUnknownVariableTypes(final IFile file, final VariableTable variableTable) {
        for (final IVariableHolder variable : variableTable.getVariables()) {
            if (variable.getType() == VariableType.INVALID) {
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.INVALID_TYPE)
                        .formatMessageWith(variable.getName());
                final Map<String, Object> attributes = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME,
                        variable.getName());
                reporter.handleProblem(problem, file, variable.getDeclaration(), attributes);
            }
        }
    }

    private void reportDuplicatedVariables(final IFile file, final VariableTable variableTable) {
        final Set<String> duplicatedNames = newHashSet();

        for (final IVariableHolder var1 : variableTable.getVariables()) {
            final String var1Name = VariableNamesSupport.extractUnifiedVariableName(var1.getName());
            for (final IVariableHolder var2 : variableTable.getVariables()) {
                if (var1 != var2 && var1Name.equals(VariableNamesSupport.extractUnifiedVariableName(var2.getName()))) {
                    duplicatedNames.add(var1Name);
                }
            }
        }

        for (final IVariableHolder variable : variableTable.getVariables()) {
            if (duplicatedNames.contains(VariableNamesSupport.extractUnifiedVariableName(variable.getName()))) {
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.DUPLICATED_VARIABLE)
                        .formatMessageWith(variable.getName());
                final Map<String, Object> attributes = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME,
                        variable.getName());
                reporter.handleProblem(problem, file, variable.getDeclaration(), attributes);
            }
        }
    }
}
