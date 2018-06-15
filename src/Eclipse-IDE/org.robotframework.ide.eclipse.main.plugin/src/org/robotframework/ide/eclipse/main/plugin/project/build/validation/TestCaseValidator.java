/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;

import com.google.common.collect.ImmutableMap;


class TestCaseValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;
    private final ValidationReportingStrategy reporter;

    private final TestCase testCase;

    TestCaseValidator(final FileValidationContext validationContext, final TestCase testCase,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.testCase = testCase;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        reportEmptyNamesOfCase();
        reportEmptyCase();

        validateSettings();
        validateKeywordsAndVariablesUsages();
    }

    private void reportEmptyNamesOfCase() {
        final RobotToken caseName = testCase.getName();
        if (caseName.getText().trim().isEmpty()) {
            reporter.handleProblem(RobotProblem.causedBy(TestCasesProblem.EMPTY_CASE_NAME), validationContext.getFile(),
                    caseName);
        }
    }

    private void reportEmptyCase() {
        final RobotToken caseName = testCase.getTestName();

        if (!hasAnythingToExecute(testCase)) {
            final String name = caseName.getText();
            final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.EMPTY_CASE).formatMessageWith(name);
            final Map<String, Object> arguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
            reporter.handleProblem(problem, validationContext.getFile(), caseName, arguments);
        }
    }

    private boolean hasAnythingToExecute(final TestCase testCase) {
        return testCase.getExecutionContext().stream().anyMatch(RobotExecutableRow::isExecutable);
    }

    private void validateSettings() {
        new TestCaseSettingsValidator(validationContext, testCase, reporter).validate();
    }

    private void validateKeywordsAndVariablesUsages() {

        final Set<String> additionalVariables = new HashSet<>();
        final List<ExecutableValidator> execValidators = new ArrayList<>();
        if (testCase.getSetups().size() == 1) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables,
                    testCase.getSetups().get(0), reporter));
        }
        final String templateKeyword = testCase.getTemplateKeywordName();
        if (templateKeyword == null) {
            testCase.getExecutionContext()
                    .stream()
                    .filter(RobotExecutableRow::isExecutable)
                    .map(row -> ExecutableValidator.of(validationContext, additionalVariables, row, reporter))
                    .forEach(execValidators::add);
        }
        if (testCase.getTeardowns().size() == 1) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables,
                    testCase.getTeardowns().get(0), reporter));
        }
        execValidators.forEach(ExecutableValidator::validate);
    }
}
