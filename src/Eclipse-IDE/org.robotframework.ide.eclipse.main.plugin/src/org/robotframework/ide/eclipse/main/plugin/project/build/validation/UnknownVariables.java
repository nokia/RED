/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.HashSet;
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
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
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

    private static Predicate<VariableDeclaration> isInvalidVariableDeclaration(final Set<String> definedVariables) {
        return declaration ->
                   !declaration.asToken().getTypes().contains(RobotTokenType.VARIABLES_ENVIRONMENT_DECLARATION)
                && !declaration.isDynamic()
                && !(declaration.getVariableType() instanceof Number)
                && !VariableNamesSupport.isDefinedVariable(declaration, definedVariables)
                && !VariableNamesSupport.isDefinedVariableInsideComputation(declaration, definedVariables);
    }

    void reportUnknownVars(final RobotToken token) {
        reportUnknownVars(newArrayList(token));
    }

    void reportUnknownVars(final List<RobotToken> tokens) {
        reportUnknownVars(new HashSet<>(), tokens);
    }

    void reportUnknownVars(final Set<String> additionalKnownVariables, final List<RobotToken> tokens) {
        final String filename = validationContext.getFile().getName();
        for (final RobotToken token : tokens) {
            if (token != null) {
                final List<VariableDeclaration> declarations = new VariableExtractor().extract(token, filename)
                        .getCorrectVariables();
                reportUnknownVarsDeclarations(additionalKnownVariables, declarations);
            }
        }
    }

    void reportUnknownVarsDeclarations(final Set<String> additionalKnownVariables,
            final List<VariableDeclaration> variablesDeclarations) {
        final Set<String> allVariables = new HashSet<>(validationContext.getAccessibleVariables());
        allVariables.addAll(additionalKnownVariables);

        final Predicate<VariableDeclaration> isInvalid = isInvalidVariableDeclaration(allVariables);

        for (final VariableDeclaration declaration : variablesDeclarations) {
            if (isInvalid.test(declaration)) {
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.UNDECLARED_VARIABLE_USE)
                        .formatMessageWith(getVariableName(declaration));

                final int variableOffset = declaration.getStartFromFile().getOffset();
                final ProblemPosition position = new ProblemPosition(declaration.getStartFromFile().getLine(),
                        Range.closed(variableOffset,
                                variableOffset + ((declaration.getEndFromFile().getOffset() + 1) - variableOffset)));

                final Map<String, Object> additionalArguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME,
                        getVariableNameWithBrackets(declaration));
                reporter.handleProblem(problem, validationContext.getFile(), position, additionalArguments);
            }
        }
    }

    private String getVariableName(final VariableDeclaration variableDeclaration) {
        return variableDeclaration.getTextWithoutComputation().map(TextPosition::getText).orElseGet(
                () -> variableDeclaration.getVariableName().getText());
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
