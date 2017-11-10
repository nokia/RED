/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.mapping.variables.CommonVariableHelper;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.annotations.VisibleForTesting;
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
        final VariableTable variableTable = variablesSection.get().getLinkedElement();

        reportVersionSpecificProblems(variableTable, monitor);

        reportInvalidVariableName(variableTable);
        reportUnknownVariableTypes(variableTable);
        reportDuplicatedVariables(variableTable);
        reportDictionaryValuesWithInvalidSyntax(variableTable);
        reportUnknownVariablesInValues(variableTable);
        reportVariableDeclarationWithoutAssignment(variableTable);
    }

    private void reportVersionSpecificProblems(final VariableTable variableTable, final IProgressMonitor monitor)
            throws CoreException {
        for (final IVariableHolder variable : variableTable.getVariables()) {
            final Iterable<VersionDependentModelUnitValidator> validators = versionDependentValidators
                    .getVariableValidators(validationContext, variable, reporter);
            for (final ModelUnitValidator validator : validators) {
                validator.validate(monitor);
            }
        }
    }

    private void reportInvalidVariableName(final VariableTable variableTable) {
        for (final IVariableHolder variable : variableTable.getVariables()) {
            final VariableType varType = variable.getType();
            if (varType != VariableType.INVALID) {
                final String variableName = variable.getDeclaration().getRaw();
                if (variableName != null && variableName.length() >= 2) {
                    if (variableName.charAt(1) != '{') {
                        final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.INVALID_NAME)
                                .formatMessageWith(variableName);
                        final Map<String, Object> attributes = ImmutableMap.of(AdditionalMarkerAttributes.NAME,
                                variableName);
                        reporter.handleProblem(problem, validationContext.getFile(), variable.getDeclaration(),
                                attributes);
                    }
                }
            }
        }
    }

    private void reportUnknownVariableTypes(final VariableTable variableTable) {
        for (final IVariableHolder variable : variableTable.getVariables()) {
            if (variable.getType() == VariableType.INVALID) {
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.INVALID_TYPE)
                        .formatMessageWith(variable.getName());
                final Map<String, Object> attributes = ImmutableMap.of(AdditionalMarkerAttributes.NAME,
                        variable.getName());
                reporter.handleProblem(problem, validationContext.getFile(), variable.getDeclaration(), attributes);
            }
        }
    }

    private void reportDuplicatedVariables(final VariableTable variableTable) {
        final Set<String> duplicatedNames = newHashSet();

        for (final IVariableHolder var1 : variableTable.getVariables()) {
            final String var1Name = VariableNamesSupport.extractUnifiedVariableName(var1.getName());
            for (final IVariableHolder var2 : variableTable.getVariables()) {
                if (var1.getName() != null && var2.getName() != null) {
                    if (var1 != var2
                            && var1Name.equals(VariableNamesSupport.extractUnifiedVariableName(var2.getName()))) {
                        duplicatedNames.add(var1Name);
                    }
                }
            }
        }

        for (final IVariableHolder variable : variableTable.getVariables()) {
            if (variable.getName() != null
                    && duplicatedNames.contains(VariableNamesSupport.extractUnifiedVariableName(variable.getName()))) {
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.DUPLICATED_VARIABLE)
                        .formatMessageWith(variable.getName());
                final Map<String, Object> attributes = ImmutableMap.of(AdditionalMarkerAttributes.NAME,
                        variable.getName());
                reporter.handleProblem(problem, validationContext.getFile(), variable.getDeclaration(), attributes);
            }
        }
    }

    private void reportDictionaryValuesWithInvalidSyntax(final VariableTable variableTable) {
        for (final AVariable variableDef : variableTable.getVariables()) {
            if (variableDef.getType() == VariableType.DICTIONARY) {
                final DictionaryVariable dictionaryDef = (DictionaryVariable) variableDef;

                for (final DictionaryKeyValuePair pair : dictionaryDef.getItems()) {
                    final boolean hasAssignment = pair.getRaw().getText().contains("=");

                    if (!hasAssignment && !isOtherDictionary(pair.getRaw())) {
                        reportDictionarySyntaxProblem(variableDef, pair, true, "has to contain '=' separator");
                    } else if (hasAssignment && keyIsListOrDict(pair.getKey())) {
                        reportDictionarySyntaxProblem(variableDef, pair, false, "cannot use list or dictionary as key");
                    }
                }
            }
        }
    }

    private boolean keyIsListOrDict(final RobotToken token) {
        return (token.getText().startsWith("@{") || token.getText().startsWith("&{")) && token.getText().endsWith("}");
    }

    private boolean isOtherDictionary(final RobotToken token) {
        return token.getTypes().contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION)
                && token.getText().startsWith("&{") && token.getText().endsWith("}");
    }

    private void reportDictionarySyntaxProblem(final AVariable variableDef, final DictionaryKeyValuePair pair,
            final boolean change, final String additionalMsg) {
        final String value = pair.getRaw().getText();
        final RobotProblem problem = RobotProblem
                .causedBy(VariablesProblem.INVALID_DICTIONARY_ELEMENT_SYNTAX)
                .formatMessageWith(value, variableDef.getName(), additionalMsg);
        final Map<String, Object> attributes = change
                ? ImmutableMap.of(AdditionalMarkerAttributes.VALUE, value)
                : ImmutableMap.of();

        reporter.handleProblem(problem, validationContext.getFile(), pair.getRaw(), attributes);
    }

    private void reportUnknownVariablesInValues(final VariableTable variableTable) {
        final Set<String> variables = validationContext.getAccessibleVariables();
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);

        for (final AVariable variableDef : variableTable.getVariables()) {
            unknownVarsValidator.reportUnknownVars(variableDef.getValueTokens(), variables);
        }
    }

    private void reportVariableDeclarationWithoutAssignment(final VariableTable variableTable) {

        for (final AVariable variable : variableTable.getVariables()) {
            final RobotToken variableToken = variable.getDeclaration();
            final CommonVariableHelper varHelper = new CommonVariableHelper();

            if (varHelper.isVariable(variableToken)) {
                final List<RobotToken> valueTokens = variable.getValueTokens();

                if (valueTokens.isEmpty()) {
                    final RobotProblem problem = RobotProblem
                            .causedBy(VariablesProblem.VARIABLE_DECLARATION_WITHOUT_ASSIGNMENT)
                            .formatMessageWith(variable.getName());
                    final Map<String, Object> attributes = ImmutableMap.of(AdditionalMarkerAttributes.NAME,
                            variable.getName());
                    reporter.handleProblem(problem, validationContext.getFile(), variable.getDeclaration(), attributes);
                }
            }
        }
    }
}
