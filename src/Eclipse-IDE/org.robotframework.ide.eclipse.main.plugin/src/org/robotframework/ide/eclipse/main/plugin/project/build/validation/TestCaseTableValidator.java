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
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;

import com.google.common.collect.ImmutableMap;

class TestCaseTableValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final Optional<RobotCasesSection> testCaseSection;

    private final ValidationReportingStrategy reporter;

    TestCaseTableValidator(final FileValidationContext validationContext, final Optional<RobotCasesSection> section,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.testCaseSection = section;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!testCaseSection.isPresent()) {
            return;
        }
        final RobotCasesSection robotCasesSection = testCaseSection.get();
        final TestCaseTable casesTable = robotCasesSection.getLinkedElement();
        final List<TestCase> cases = casesTable.getTestCases();

        reportDuplicatedCases(cases);

        validateTestCases(cases);
    }

    private void reportDuplicatedCases(final List<TestCase> cases) {
        final Set<String> duplicatedNames = newHashSet();

        for (final TestCase case1 : cases) {
            for (final TestCase case2 : cases) {
                if (case1 != case2) {
                    final String case1Name = case1.getTestName().getText();
                    final String case2Name = case2.getTestName().getText();

                    if (case1Name.equalsIgnoreCase(case2Name)) {
                        duplicatedNames.add(case1Name.toLowerCase());
                    }
                }
            }
        }

        for (final TestCase testCase : cases) {
            final RobotToken caseName = testCase.getTestName();
            final String name = caseName.getText();

            if (duplicatedNames.contains(name.toLowerCase())) {
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.DUPLICATED_CASE)
                        .formatMessageWith(name);
                final Map<String, Object> additionalArguments = ImmutableMap.of("name", name);
                reporter.handleProblem(problem, validationContext.getFile(), caseName, additionalArguments);
            }
        }
    }

    private void validateTestCases(final List<TestCase> cases) {
        for (final TestCase test : cases) {
            new TestCaseValidator(validationContext, test, reporter).validate();
        }
    }
}
