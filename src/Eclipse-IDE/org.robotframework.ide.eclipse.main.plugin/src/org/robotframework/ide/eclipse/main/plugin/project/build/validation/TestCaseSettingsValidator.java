/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.RobotTimeFormat;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.collect.ImmutableMap;

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
        this.versionDependentValidators = new VersionDependentValidators(validationContext, reporter);
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

        reportUnknownVariablesInNonExecutables();
    }

    private void reportVersionSpecificProblems() {
        versionDependentValidators.getTestCaseSettingsValidators(testCase).forEach(ModelUnitValidator::validate);
    }

    private void reportUnknownSettings() {
        final List<LocalSetting<TestCase>> unknownSettings = testCase.getUnknownSettings();
        for (final LocalSetting<TestCase> unknownSetting : unknownSettings) {
            final RobotToken token = unknownSetting.getDeclaration();
            final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.UNKNOWN_TEST_CASE_SETTING)
                    .formatMessageWith(token.getText());

            final String robotVersion = validationContext.getVersion().asString();
            reporter.handleProblem(problem, validationContext.getFile(), token,
                    ImmutableMap.of(AdditionalMarkerAttributes.NAME, token.getText(),
                            AdditionalMarkerAttributes.ROBOT_VERSION, robotVersion));
        }
    }

    private void reportTagsProblems() {
        testCase.getTags().stream()
            .filter(tag -> !tag.tokensOf(RobotTokenType.TEST_CASE_SETTING_TAGS).findFirst().isPresent())
            .forEach(this::reportEmptySetting);
    }

    private void reportTimeoutsProblems() {
        testCase.getTimeouts().stream()
                .filter(timeout -> timeout.getToken(RobotTokenType.TEST_CASE_SETTING_TIMEOUT_VALUE) == null)
                .forEach(this::reportEmptySetting);

        reportInvalidTimeoutSyntax(testCase.getTimeouts());
    }

    private void reportInvalidTimeoutSyntax(final List<LocalSetting<TestCase>> timeouts) {
        for (final LocalSetting<TestCase> testTimeout : timeouts) {
            final RobotToken timeoutToken = testTimeout.getToken(RobotTokenType.TEST_CASE_SETTING_TIMEOUT_VALUE);
            if (timeoutToken != null) {
                final String timeout = timeoutToken.getText();
                if (!timeoutToken.getTypes().contains(RobotTokenType.VARIABLE_USAGE)
                        && !timeout.equalsIgnoreCase("none")
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
                .filter(doc -> doc.getToken(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT) == null)
                .ifPresent(this::reportEmptySetting);
    }

    private void reportTemplateProblems() {
        testCase.getTemplates().stream()
                .filter(template -> template.getToken(RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME) == null)
                .forEach(this::reportEmptySetting);
    }

    private void reportSetupProblems() {
        testCase.getSetups().stream()
                .filter(setup -> setup.getToken(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME) == null)
                .forEach(this::reportEmptySetting);
    }

    private void reportTeardownProblems() {
        testCase.getTeardowns().stream()
                .filter(teardown -> teardown.getToken(RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME) == null)
                .forEach(this::reportEmptySetting);
    }

    private void reportEmptySetting(final AModelElement<?> element) {
        final RobotToken defToken = element.getDeclaration();
        final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.EMPTY_CASE_SETTING)
                .formatMessageWith(defToken.getText());
        reporter.handleProblem(problem, validationContext.getFile(), defToken);
    }

    private void reportUnknownVariablesInNonExecutables() {
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);

        for (final LocalSetting<TestCase> testTimeout : testCase.getTimeouts()) {
            unknownVarsValidator
                    .reportUnknownVars(testTimeout.getToken(RobotTokenType.TEST_CASE_SETTING_TIMEOUT_VALUE));
        }
        for (final LocalSetting<TestCase> tag : testCase.getTags()) {
            unknownVarsValidator
                    .reportUnknownVars(tag.tokensOf(RobotTokenType.TEST_CASE_SETTING_TAGS).collect(toList()));
        }
    }
}
