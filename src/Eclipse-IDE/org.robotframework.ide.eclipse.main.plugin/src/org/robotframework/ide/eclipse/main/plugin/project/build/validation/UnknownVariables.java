/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;

import com.google.common.collect.ImmutableMap;

public class UnknownVariables {

    private final FileValidationContext validationContext;

    private final ValidationReportingStrategy reporter;

    public UnknownVariables(final FileValidationContext validationContext, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.reporter = reporter;
    }

    public void reportUnknownVars(final RobotToken token) {
        reportUnknownVars(newArrayList(token));
    }

    void reportUnknownVars(final List<RobotToken> tokens) {
        reportUnknownVars(new HashSet<>(), tokens);
    }

    void reportUnknownVars(final Set<String> additionalKnownVariables, final List<RobotToken> tokens) {
        for (final RobotToken token : tokens) {
            if (token != null) {
                final List<VariableUse> uses = VariablesAnalyzer.analyzer(validationContext.getVersion())
                        .getVariablesUses(token)
                        .collect(toList());
                reportUnknownVarsDeclarations(additionalKnownVariables, uses);
            }
        }
    }

    void reportUnknownVarsDeclarations(final Set<String> additionalKnownVariables,
            final List<? extends VariableUse> variableUsages) {
        final Set<String> allVariables = new HashSet<>(validationContext.getAccessibleVariables());
        allVariables.addAll(additionalKnownVariables);

        for (final VariableUse declaration : variableUsages) {
            if (!declaration.isDefinedIn(allVariables)) {
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.UNDECLARED_VARIABLE_USE)
                        .formatMessageWith(declaration.getName());

                final Map<String, Object> additionalArguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME,
                        declaration.asToken().getText());
                reporter.handleProblem(problem, validationContext.getFile(),
                        ProblemPosition.fromRegion(declaration.getRegion()), additionalArguments);
            }
        }
    }
}
