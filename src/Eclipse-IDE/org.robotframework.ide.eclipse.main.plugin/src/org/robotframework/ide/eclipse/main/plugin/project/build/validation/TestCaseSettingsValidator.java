/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.RobotTimeFormat;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;

/**
 * @author Michal Anglart
 *
 */
public class TestCaseSettingsValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final TestCase testCase;

    private final ValidationReportingStrategy reporter;

    TestCaseSettingsValidator(final FileValidationContext validationContext, final TestCase testCase,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.testCase = testCase;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        reportUnknownSettings();

        reportTagsProblems();
        reportTimeoutsProblems();
        reportDocumentationsProblems();
        reportSetupProblems();
        reportTeardownProblems();
        reportTemplateProblems();

        reportOutdatedSettingsSynonyms();
        reportUnknownVariablesInNonExecutables();
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
        final Map<RobotToken, Boolean> declarationIsEmpty = testCase.getTags().stream().collect(
                toMap(TestCaseTags::getDeclaration, tag -> tag.getTags().isEmpty()));

        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTimeoutsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = testCase.getTimeouts().stream().collect(
                toMap(TestCaseTimeout::getDeclaration, timeout -> timeout.getTimeout() == null));

        reportCommonProblems(declarationIsEmpty);
        reportInvalidTimeoutSyntax(testCase.getTimeouts());
    }

    private void reportInvalidTimeoutSyntax(final List<TestCaseTimeout> timeouts) {
        for (final TestCaseTimeout testTimeout : timeouts) {
            final RobotToken timeoutToken = testTimeout.getTimeout();
            if (timeoutToken != null) {
                final String timeout = timeoutToken.getText();
                if (!timeoutToken.getTypes().contains(RobotTokenType.VARIABLE_USAGE)
                        && !RobotTimeFormat.isValidRobotTimeArgument(timeout.trim())) {
                    final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_TIME_FORMAT)
                            .formatMessageWith(timeout);
                    reporter.handleProblem(problem, validationContext.getFile(), timeoutToken);
                }
            }
        }
    }

    private void reportDocumentationsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = testCase.getDocumentation().stream().collect(
                toMap(TestDocumentation::getDeclaration, doc -> doc.getDocumentationText().isEmpty()));

        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTemplateProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = testCase.getTemplates().stream().collect(
                toMap(TestCaseTemplate::getDeclaration, template -> template.getKeywordName() == null));

        reportCommonProblems(declarationIsEmpty);
        reportTemplateUnexpectedArguments(testCase.getTemplates());
        reportTemplateKeywordProblems(testCase.getTemplates());
    }

    private void reportTemplateUnexpectedArguments(final List<TestCaseTemplate> templates) {
        for (final TestCaseTemplate template : templates) {
            if (!template.getUnexpectedTrashArguments().isEmpty()) {
                final RobotToken settingToken = template.getDeclaration();
                final String actualArgs = template.getUnexpectedTrashArguments()
                        .stream()
                        .map(RobotToken::getText)
                        .map(String::trim)
                        .collect(joining(", ", "[", "]"));
                final String additionalMsg = "Only keyword name should be specified for templates.";
                final RobotProblem problem = RobotProblem
                        .causedBy(GeneralSettingsProblem.SETTING_ARGUMENTS_NOT_APPLICABLE)
                        .formatMessageWith(settingToken.getText(), actualArgs, additionalMsg);
                reporter.handleProblem(problem, validationContext.getFile(), settingToken);
            }
        }
    }

    private void reportTemplateKeywordProblems(final List<TestCaseTemplate> templates) {
        for (final TestCaseTemplate template : templates) {
            final RobotToken keywordToken = template.getKeywordName();
            if (keywordToken != null) {
                final String keywordName = keywordToken.getText();
                if (keywordName.toLowerCase().equals("none")) {
                    continue;
                }
                new KeywordCallInTemplateValidator(validationContext, keywordToken, reporter).validate();
            }
        }
    }

    private void reportSetupProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = testCase.getSetups().stream().collect(
                toMap(TestCaseSetup::getDeclaration, setup -> setup.getKeywordName() == null));

        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTeardownProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = testCase.getTeardowns().stream().collect(
                toMap(TestCaseTeardown::getDeclaration, teardown -> teardown.getKeywordName() == null));

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

    private void reportOutdatedSettingsSynonyms() {
        reportOutdatedSettings(testCase.getDocumentation(), TestCasesProblem.DOCUMENT_SYNONYM, "documentation");
        reportOutdatedSettings(testCase.getSetups(), TestCasesProblem.PRECONDITION_SYNONYM, "setup");
        reportOutdatedSettings(testCase.getTeardowns(), TestCasesProblem.POSTCONDITION_SYNONYM, "teardown");
    }

    private void reportOutdatedSettings(final List<? extends AModelElement<?>> settings, final IProblemCause cause,
            final String correctRepresentation) {
        for (final AModelElement<?> setting : settings) {
            final RobotToken declarationToken = setting.getDeclaration();
            final String text = declarationToken.getText();
            final String canonicalText = text.replaceAll("\\s", "").toLowerCase();
            final String canonicalCorrectRepresentation = correctRepresentation.replaceAll("\\s", "").toLowerCase();
            if (!canonicalText.contains(canonicalCorrectRepresentation)) {
                reporter.handleProblem(RobotProblem.causedBy(cause).formatMessageWith(text),
                        validationContext.getFile(), declarationToken);
            }
        }
    }

    private void reportUnknownVariablesInNonExecutables() {
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);

        for (final TestCaseTimeout testTimeout : testCase.getTimeouts()) {
            unknownVarsValidator.reportUnknownVars(testTimeout.getTimeout());
        }
        for (final TestCaseTags tag : testCase.getTags()) {
            unknownVarsValidator.reportUnknownVars(tag.getTags());
        }
    }
}
