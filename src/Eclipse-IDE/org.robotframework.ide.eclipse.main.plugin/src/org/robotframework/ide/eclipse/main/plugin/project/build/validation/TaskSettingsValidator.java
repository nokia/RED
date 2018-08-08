/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.RobotTimeFormat;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TasksProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

/**
 * @author Michal Anglart
 *
 */
public class TaskSettingsValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final Task task;

    private final ValidationReportingStrategy reporter;

    private final VersionDependentValidators versionDependentValidators;

    TaskSettingsValidator(final FileValidationContext validationContext, final Task task,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.task = task;
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
        versionDependentValidators.getTaskSettingsValidators(task).forEach(ModelUnitValidator::validate);
    }

    private void reportUnknownSettings() {
        for (final LocalSetting<Task> unknownSetting : task.getUnknownSettings()) {
            final RobotToken token = unknownSetting.getDeclaration();
            final RobotProblem problem = RobotProblem.causedBy(TasksProblem.UNKNOWN_TASK_SETTING)
                    .formatMessageWith(token.getText());

            reporter.handleProblem(problem, validationContext.getFile(), token);
        }
    }

    private void reportTagsProblems() {
        task.getTags()
                .stream()
                .filter(tag -> !tag.tokensOf(RobotTokenType.TASK_SETTING_TAGS).findFirst().isPresent())
                .forEach(this::reportEmptySetting);
    }

    private void reportTimeoutsProblems() {
        task.getTimeouts()
                .stream()
                .filter(timeout -> timeout.getToken(RobotTokenType.TASK_SETTING_TIMEOUT_VALUE) == null)
                .forEach(this::reportEmptySetting);

        reportInvalidTimeoutSyntax(task.getTimeouts());
    }

    private void reportInvalidTimeoutSyntax(final List<LocalSetting<Task>> timeouts) {
        for (final LocalSetting<Task> testTimeout : timeouts) {
            final RobotToken timeoutToken = testTimeout.getToken(RobotTokenType.TASK_SETTING_TIMEOUT_VALUE);
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
        task.getDocumentation()
                .stream()
                .findFirst()
                .filter(doc -> !doc.tokensOf(RobotTokenType.TASK_SETTING_DOCUMENTATION_TEXT).findFirst().isPresent())
                .ifPresent(this::reportEmptySetting);
    }

    private void reportTemplateProblems() {
        task.getTemplates()
                .stream()
                .filter(template -> template.getToken(RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_NAME) == null)
                .forEach(this::reportEmptySetting);

        reportTemplateWrittenInMultipleCells(task.getTemplates());
        reportTemplateKeywordProblems(task.getTemplates());
    }

    private void reportTemplateWrittenInMultipleCells(final List<LocalSetting<Task>> templates) {
        for (final LocalSetting<Task> template : templates) {
            final boolean hasUnwantedArgs = template
                    .getToken(RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT) != null;
            if (hasUnwantedArgs) {
                final RobotToken settingToken = template.getDeclaration();
                final RobotProblem problem = RobotProblem
                        .causedBy(GeneralSettingsProblem.TEMPLATE_KEYWORD_NAME_IN_MULTIPLE_CELLS);
                reporter.handleProblem(problem, validationContext.getFile(), settingToken);
            }
        }
    }

    private void reportTemplateKeywordProblems(final List<LocalSetting<Task>> templates) {
        for (final LocalSetting<Task> template : templates) {
            final List<RobotToken> keywordNameParts = template.getTokensWithoutDeclaration();
            if (!keywordNameParts.isEmpty()) {
                final RobotToken firstPartOfName = keywordNameParts.get(0);

                final String keywordName = keywordNameParts.stream().map(RobotToken::getText).collect(joining(" "));
                if (keywordName.isEmpty() || keywordName.toLowerCase().equals("none")) {
                    continue;
                }
                new KeywordCallInTemplateValidator(validationContext, keywordName, firstPartOfName, reporter)
                        .validate();
            }
        }
    }

    private void reportSetupProblems() {
        task.getSetups()
                .stream()
                .filter(setup -> setup.getToken(RobotTokenType.TASK_SETTING_SETUP_KEYWORD_NAME) == null)
                .forEach(this::reportEmptySetting);
    }

    private void reportTeardownProblems() {
        task.getTeardowns()
                .stream()
                .filter(setup -> setup.getToken(RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_NAME) == null)
                .forEach(this::reportEmptySetting);
    }

    private void reportEmptySetting(final AModelElement<?> element) {
        final RobotToken defToken = element.getDeclaration();
        final RobotProblem problem = RobotProblem.causedBy(TasksProblem.EMPTY_TASK_SETTING)
                .formatMessageWith(defToken.getText());
        reporter.handleProblem(problem, validationContext.getFile(), defToken);
    }

    private void reportUnknownVariablesInNonExecutables() {
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);

        for (final LocalSetting<Task> taskTimeout : task.getTimeouts()) {
            unknownVarsValidator.reportUnknownVars(taskTimeout.getToken(RobotTokenType.TASK_SETTING_TIMEOUT_VALUE));
        }
        for (final LocalSetting<Task> tag : task.getTags()) {
            unknownVarsValidator.reportUnknownVars(tag.tokensOf(RobotTokenType.TASK_SETTING_TAGS).collect(toList()));
        }
    }
}
