/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;

/**
 * @author Michal Anglart
 *
 */
public class TestCaseSettingsValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final TestCase testCase;

    private final ProblemsReportingStrategy reporter;

    TestCaseSettingsValidator(final FileValidationContext validationContext, final TestCase testCase,
            final ProblemsReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.testCase = testCase;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        reportUnknownSettings();

        reportTagsProblems();
        reportTimeoutsProblems();
        reportDocumentationsProblems();
        reportSetupProblems();
        reportTeardownProblems();
        reportTemplateProblems();

        reportKeywordUsageProblemsInTestCaseSettings();
    }

    private void reportUnknownSettings() {
        final List<TestCaseUnknownSettings> unknownSettings = testCase.getUnknownSettings();
        for (final TestCaseUnknownSettings unknownSetting : unknownSettings) {
            final RobotToken token = unknownSetting.getDeclaration();
            final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.UNKNOWN_TEST_CASE_SETTING)
                    .formatMessageWith(token.getText());
            reporter.handleProblem(problem, validationContext.getFile(), token);
        }
    }

    private void reportTagsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final TestCaseTags tag : testCase.getTags()) {
            declarationIsEmpty.put(tag.getDeclaration(), tag.getTags().isEmpty());
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTimeoutsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final TestCaseTimeout timeout : testCase.getTimeouts()) {
            declarationIsEmpty.put(timeout.getDeclaration(), timeout.getTimeout() == null);
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportDocumentationsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final TestDocumentation doc : testCase.getDocumentation()) {
            declarationIsEmpty.put(doc.getDeclaration(), doc.getDocumentationText().isEmpty());
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTemplateProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final TestCaseTemplate template : testCase.getTemplates()) {
            declarationIsEmpty.put(template.getDeclaration(), template.getKeywordName() == null);
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTeardownProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final TestCaseSetup setup : testCase.getSetups()) {
            declarationIsEmpty.put(setup.getDeclaration(), setup.getKeywordName() == null);
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportSetupProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final TestCaseTeardown teardown : testCase.getTeardowns()) {
            declarationIsEmpty.put(teardown.getDeclaration(), teardown.getKeywordName() == null);
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportCommonProblems(final Map<RobotToken, Boolean> declarationTokens) {
        final String caseName = testCase.getTestName().getText();
        final IFile file = validationContext.getFile();
        final boolean tooManySettings = declarationTokens.size() > 1;

        declarationTokens.forEach((defToken, isEmpty) -> {
            if (tooManySettings) {
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.DUPLICATED_CASE_SETTING)
                        .formatMessageWith(caseName, defToken.getText());
                reporter.handleProblem(problem, file, defToken);
            }

            if (isEmpty) {
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.EMPTY_CASE_SETTING)
                        .formatMessageWith(defToken.getText());
                reporter.handleProblem(problem, file, defToken);
            }
        });
    }

    private void reportKeywordUsageProblemsInTestCaseSettings() {
        for (final TestCaseSetup setup : testCase.getSetups()) {
            final RobotToken keywordNameToken = setup.getKeywordName();
            if (keywordNameToken != null) {
                TestCaseTableValidator.reportKeywordUsageProblemsInSetupAndTeardownSetting(validationContext, reporter,
                        keywordNameToken, Optional.of(setup.getArguments()));
            }
        }

        for (final TestCaseTeardown teardown : testCase.getTeardowns()) {
            final RobotToken name = teardown.getKeywordName();
            if (name != null) {
                TestCaseTableValidator.reportKeywordUsageProblemsInSetupAndTeardownSetting(validationContext, reporter,
                        name, Optional.of(teardown.getArguments()));
            }
        }
    }
}
