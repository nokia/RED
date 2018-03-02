/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration.Number;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

class UnknownVariables {

    private final FileValidationContext validationContext;

    private final ValidationReportingStrategy reporter;

    UnknownVariables(final FileValidationContext validationContext, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.reporter = reporter;
    }

    void reportUnknownVars(final List<RobotToken> tokens, final Set<String> variables) {
        final String filename = validationContext.getFile().getName();
        final Predicate<VariableDeclaration> isInvalid = isInvalidVariableDeclaration(variables);
        for (final RobotToken token : tokens) {
            final List<VariableDeclaration> declarations = new VariableExtractor().extract(token, filename)
                    .getCorrectVariables();
            if (!declarations.isEmpty()) {
                reportUnknownVariables(declarations, isInvalid);
            }
        }
    }

    static Predicate<VariableDeclaration> isInvalidVariableDeclaration(final Set<String> definedVariables) {
        return new Predicate<VariableDeclaration>() {

            @Override
            public boolean test(final VariableDeclaration variableDeclaration) {
                return !variableDeclaration.asToken().getTypes().contains(
                        RobotTokenType.VARIABLES_ENVIRONMENT_DECLARATION) && !variableDeclaration.isDynamic()
                        && !VariableNamesSupport.isDefinedVariable(variableDeclaration, definedVariables)
                        && !isSpecificVariableDeclaration(definedVariables, variableDeclaration);
            }
        };
    }

    private static boolean isSpecificVariableDeclaration(final Set<String> definedVariables,
            final VariableDeclaration variableDeclaration) {
        return variableDeclaration.getVariableType() instanceof Number
                || VariableNamesSupport.isDefinedVariableInsideComputation(variableDeclaration, definedVariables);
    }

    void reportUnknownVariables(final List<VariableDeclaration> variablesDeclarations,
            final Predicate<VariableDeclaration> isInvalid) {

        for (final VariableDeclaration variableDeclaration : variablesDeclarations) {
            if (isInvalid.test(variableDeclaration)) {
                final String variableName = getVariableName(variableDeclaration);
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.UNDECLARED_VARIABLE_USE)
                        .formatMessageWith(variableName);
                final int variableOffset = variableDeclaration.getStartFromFile().getOffset();
                final ProblemPosition position = new ProblemPosition(variableDeclaration.getStartFromFile().getLine(),
                        Range.closed(variableOffset, variableOffset
                                + ((variableDeclaration.getEndFromFile().getOffset() + 1) - variableOffset)));
                final Map<String, Object> additionalArguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME,
                        getVariableNameWithBrackets(variableDeclaration));
                reporter.handleProblem(problem, validationContext.getFile(), position, additionalArguments);
            }
        }
    }

    private String getVariableName(final VariableDeclaration variableDeclaration) {
        final Optional<TextPosition> extractVariableName = variableDeclaration.getTextWithoutComputation();
        if (extractVariableName.isPresent()) {
            return extractVariableName.get().getText();
        }
        return variableDeclaration.getVariableName().getText();
    }

    private String getVariableNameWithBrackets(final VariableDeclaration variableDeclaration) {
        final String name = variableDeclaration.asToken().getText();

        final Optional<TextPosition> extractVariableName = variableDeclaration.getTextWithoutComputation();
        if (extractVariableName.isPresent()) {
            return name.substring(0, 2) + extractVariableName.get().getText() + name.charAt(name.length() - 1);
        }
        return name;
    }
}
