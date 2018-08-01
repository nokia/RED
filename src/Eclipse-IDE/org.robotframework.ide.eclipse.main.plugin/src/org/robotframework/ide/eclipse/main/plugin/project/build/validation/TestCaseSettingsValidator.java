/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
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
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

/**
 * @author Michal Anglart
 *
 */
public class TestCaseSettingsValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final TestCase testCase;

    private final ValidationReportingStrategy reporter;

    private final VersionDependentValidators versionDependentValidators;

    TestCaseSettingsValidator(final FileValidationContext validationContext, final TestCase testCase,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.testCase = testCase;
        this.reporter = reporter;
        this.versionDependentValidators = new VersionDependentValidators();
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        reportVersionSpecificProblems();
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

    private void reportVersionSpecificProblems() {
        versionDependentValidators.getTestCaseSettingsValidators(validationContext, testCase, reporter)
                .forEach(ModelUnitValidator::validate);
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
        testCase.getTags().stream()
            .filter(tag -> tag.getTags().isEmpty())
            .forEach(this::reportEmptySetting);
    }

    private void reportTimeoutsProblems() {
        testCase.getTimeouts().stream()
                .filter(timeout -> timeout.getTimeout() == null)
                .forEach(this::reportEmptySetting);

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
        testCase.getDocumentation().stream().findFirst()
                .filter(doc -> doc.getDocumentationText().isEmpty())
                .ifPresent(this::reportEmptySetting);
    }

    private void reportTemplateProblems() {
        testCase.getTemplates().stream()
                .filter(template -> template.getKeywordName() == null)
                .forEach(this::reportEmptySetting);

        reportTemplateWrittenInMultipleCells(testCase.getTemplates());
        reportTemplateKeywordProblems(testCase.getTemplates());
    }

    private void reportTemplateWrittenInMultipleCells(final List<TestCaseTemplate> templates) {
        for (final TestCaseTemplate template : templates) {
            if (!template.getUnexpectedTrashArguments().isEmpty()) {
                final RobotToken settingToken = template.getDeclaration();
                final RobotProblem problem = RobotProblem
                        .causedBy(GeneralSettingsProblem.TEMPLATE_KEYWORD_NAME_IN_MULTIPLE_CELLS);
                reporter.handleProblem(problem, validationContext.getFile(), settingToken);
            }
        }
    }

    private void reportTemplateKeywordProblems(final List<TestCaseTemplate> templates) {
        for (final TestCaseTemplate template : templates) {
            final RobotToken keywordToken = template.getKeywordName();
            if (keywordToken != null) {
                final List<String> keywordParts = newArrayList(keywordToken.getText());
                template.getUnexpectedTrashArguments().stream().map(RobotToken::getText).forEach(keywordParts::add);

                final String keywordName = String.join(" ", keywordParts);
                if (keywordName.toLowerCase().equals("none")) {
                    continue;
                }
                new KeywordCallInTemplateValidator(validationContext, keywordName, keywordToken, reporter).validate();
            }
        }
    }

    private void reportSetupProblems() {
        testCase.getSetups().stream()
                .filter(setup -> setup.getKeywordName() == null)
                .forEach(this::reportEmptySetting);
    }

    private void reportTeardownProblems() {
        testCase.getTeardowns().stream()
                .filter(teardown -> teardown.getKeywordName() == null)
                .forEach(this::reportEmptySetting);
    }

    private void reportEmptySetting(final AModelElement<?> element) {
        final RobotToken defToken = element.getDeclaration();
        final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.EMPTY_CASE_SETTING)
                .formatMessageWith(defToken.getText());
        reporter.handleProblem(problem, validationContext.getFile(), defToken);
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
