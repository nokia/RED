/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.variables.AVariable;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.collect.Range;

class VariablesTableValidator implements ModelUnitValidator {

    private final ValidationContext validationContext;

    private final IFile file;

    private final VariableTable variableTable;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    VariablesTableValidator(final ValidationContext validationContext, final IFile file,
            final VariableTable variableTable) {
        this.validationContext = validationContext;
        this.file = file;
        this.variableTable = variableTable;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!variableTable.isPresent()) {
            return;
        }
        reportVersionSpecificProblems(variableTable, monitor);

        reportUnknownVariableTypes(variableTable);
        reportDuplicatedVariables(variableTable);
    }

    private void reportVersionSpecificProblems(final VariableTable variableTable, final IProgressMonitor monitor)
            throws CoreException {
        for (final IVariableHolder variable : variableTable.getVariables()) {
            final List<? extends ModelUnitValidator> validators = new VersionDependentValidators()
                    .getVariableValidators(variable, validationContext.getVersion());
            for (final ModelUnitValidator validator : validators) {
                try {
                    validator.validate(monitor);
                } catch (final ValidationProblemException e) {
                    final ProblemPosition position = e.shouldMarkWholeDefinition()
                            ? toPositionOfWholeDefinition(variable) : toPosition(variable);
                    reporter.handleProblem(e.getProblem(), file, position);
                }
            }
        }
    }

    private void reportUnknownVariableTypes(final VariableTable variableTable) {
        for (final IVariableHolder variable : variableTable.getVariables()) {
            if (variable.getType() == VariableType.INVALID) {
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.INVALID_TYPE)
                        .formatMessageWith(variable.getName());
                reporter.handleProblem(problem, file, toPosition(variable));
            }
        }
    }

    private void reportDuplicatedVariables(final VariableTable variableTable) {
        final Set<String> duplicatedNames = newHashSet();

        for (final IVariableHolder var1 : variableTable.getVariables()) {
            for (final IVariableHolder var2 : variableTable.getVariables()) {
                if (var1 != var2 && var1.getName().equals(var2.getName())) {
                    duplicatedNames.add(var1.getName());
                }
            }
        }

        for (final IVariableHolder variable : variableTable.getVariables()) {
            if (duplicatedNames.contains(variable.getName())) {
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.DUPLICATED_VARIABLE)
                        .formatMessageWith(variable.getName());
                reporter.handleProblem(problem, file, toPosition(variable));
            }
        }
    }

    private static ProblemPosition toPosition(final IVariableHolder variable) {
        return new ProblemPosition(variable.getDeclaration().getLineNumber(),
                Range.closed(variable.getDeclaration().getStartOffset(),
                        variable.getDeclaration().getStartOffset() + variable.getDeclaration().getText().length()));
    }

    private static ProblemPosition toPositionOfWholeDefinition(final IVariableHolder variable) {
        final List<RobotToken> tokens = ((AVariable) variable).getElementTokens();
        final RobotToken lastToken = tokens.isEmpty() ? variable.getDeclaration() : tokens.get(tokens.size() - 1);

        return new ProblemPosition(variable.getDeclaration().getLineNumber(), Range.closed(
                variable.getDeclaration().getStartOffset(), lastToken.getStartOffset() + lastToken.getText().length()));
    }
}
