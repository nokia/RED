/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.TemplateSetting;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TaskSetup;
import org.rf.ide.core.testdata.model.table.setting.TaskTeardown;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.model.table.setting.TaskTimeout;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.UnknownSetting;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.RobotTimeFormat;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;

class GeneralSettingsTableValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final Optional<RobotSettingsSection> settingsSection;

    private final ValidationReportingStrategy reporter;

    private final VersionDependentValidators versionDependentValidators;

    GeneralSettingsTableValidator(final FileValidationContext validationContext,
            final Optional<RobotSettingsSection> settingSection, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.settingsSection = settingSection;
        this.reporter = reporter;
        this.versionDependentValidators = new VersionDependentValidators(validationContext, reporter);
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!settingsSection.isPresent()) {
            return;
        }
        final RobotSettingsSection robotSettingsSection = settingsSection.get();
        final RobotSuiteFile suiteFile = robotSettingsSection.getSuiteFile();
        final SettingTable settingsTable = robotSettingsSection.getLinkedElement();

        reportVersionSpecificProblems(settingsTable, monitor);
        reportUnknownSettings(settingsTable.getUnknownSettings());

        validateLibraries(suiteFile, settingsTable.getLibraryImports(), monitor);
        validateResources(suiteFile, settingsTable.getResourcesImports(), monitor);
        validateVariables(suiteFile, settingsTable.getVariablesImports(), monitor);

        validateSetupsAndTeardowns(settingsTable.getSuiteSetups());
        validateSetupsAndTeardowns(settingsTable.getSuiteTeardowns());
        validateSetupsAndTeardowns(settingsTable.getTestSetups());
        validateSetupsAndTeardowns(settingsTable.getTestTeardowns());
        validateSetupsAndTeardowns(settingsTable.getTaskSetups());
        validateSetupsAndTeardowns(settingsTable.getTaskTeardowns());
        validateTemplates(settingsTable.getTestTemplatesViews(), settingsTable.getTaskTemplates());
        validateTestTimeouts(settingsTable.getTestTimeoutsViews());
        validateTaskTimeouts(settingsTable.getTaskTimeouts());
        validateTags(settingsTable.getDefaultTags());
        validateTags(settingsTable.getForceTags());
        validateDocumentations(settingsTable.getDocumentation());
        validateMetadatas(settingsTable.getMetadatas());

        validateTestAndTasksSettingsMixes(suiteFile, settingsTable);

        validateKeywordsAndVariablesUsagesInExecutables(settingsTable);
        reportUnknownVariablesInNonExecutables(settingsTable);
    }

    private void reportVersionSpecificProblems(final SettingTable table, final IProgressMonitor monitor)
            throws CoreException {
        final List<VersionDependentModelUnitValidator> validators = versionDependentValidators
                .getGeneralSettingsTableValidators(table)
                .collect(toList());
        for (final ModelUnitValidator validator : validators) {
            validator.validate(monitor);
        }
    }

    private void reportUnknownSettings(final List<UnknownSetting> unknownSettings) {
        for (final UnknownSetting unknownSetting : unknownSettings) {
            final RobotToken token = unknownSetting.getDeclaration();
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNKNOWN_SETTING)
                    .formatMessageWith(token.getText());
            final String robotVersion = validationContext.getVersion().asString();
            reporter.handleProblem(problem, validationContext.getFile(), token,
                    ImmutableMap.of(AdditionalMarkerAttributes.NAME, token.getText(),
                            AdditionalMarkerAttributes.ROBOT_VERSION, robotVersion));
        }
    }

    private void validateLibraries(final RobotSuiteFile suiteFile, final List<LibraryImport> libraryImports,
            final IProgressMonitor monitor) throws CoreException {
        new GeneralSettingsLibrariesImportValidator(validationContext, suiteFile, libraryImports, reporter)
                .validate(monitor);
    }

    private void validateResources(final RobotSuiteFile suiteFile, final List<ResourceImport> resourcesImports,
            final IProgressMonitor monitor) throws CoreException {
        new GeneralSettingsResourcesImportValidator(validationContext, suiteFile, resourcesImports, reporter)
                .validate(monitor);
    }

    private void validateVariables(final RobotSuiteFile suiteFile, final List<VariablesImport> variablesImports,
            final IProgressMonitor monitor) throws CoreException {
        new GeneralSettingsVariablesImportValidator(validationContext, suiteFile, variablesImports, reporter)
                .validate(monitor);
    }

    private void validateSetupsAndTeardowns(final List<? extends AKeywordBaseSetting<?>> keywordBasedSettings) {
        final boolean areAllEmpty = keywordBasedSettings.stream().map(AKeywordBaseSetting::getKeywordName).allMatch(
                kwToken -> kwToken == null);
        if (areAllEmpty) {
            reportEmptySettings(keywordBasedSettings);
        }
    }

    private void validateTemplates(final List<TestTemplate> testTemplates, final List<TaskTemplate> taskTemplates) {
        final List<TemplateSetting> allTemplates = new ArrayList<>(testTemplates);
        allTemplates.addAll(taskTemplates);

        final boolean areAllEmpty = allTemplates.stream()
                .map(TemplateSetting::getKeywordName)
                .allMatch(kwToken -> kwToken == null);
        if (areAllEmpty) {
            reportEmptySettings(testTemplates);
            reportEmptySettings(taskTemplates);
        }
    }

    private void validateTestTimeouts(final List<TestTimeout> timeouts) {
        final boolean areAllEmpty = timeouts.stream().map(TestTimeout::getTimeout).allMatch(
                timeoutToken -> timeoutToken == null);
        if (areAllEmpty) {
            reportEmptySettings(timeouts);
        }
        reportInvalidTimeoutSyntax(
                timeouts.stream().map(TestTimeout::getTimeout).filter(t -> t != null).collect(toList()));
    }

    private void validateTaskTimeouts(final List<TaskTimeout> timeouts) {
        final boolean areAllEmpty = timeouts.stream()
                .map(TaskTimeout::getTimeout)
                .allMatch(timeoutToken -> timeoutToken == null);
        if (areAllEmpty) {
            reportEmptySettings(timeouts);
        }
        reportInvalidTimeoutSyntax(
                timeouts.stream().map(TaskTimeout::getTimeout).filter(t -> t != null).collect(toList()));
    }

    private void reportInvalidTimeoutSyntax(final List<RobotToken> timeouts) {
        for (final RobotToken timeoutToken : timeouts) {
            final String timeout = timeoutToken.getText();
            if (!timeoutToken.getTypes().contains(RobotTokenType.VARIABLE_USAGE) && !timeout.equalsIgnoreCase("none")
                    && !RobotTimeFormat.isValidRobotTimeArgument(timeout.trim())) {
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_TIME_FORMAT)
                        .formatMessageWith(timeout);
                reporter.handleProblem(problem, validationContext.getFile(), timeoutToken);
            }
        }
    }

    private void validateTags(final List<? extends ATags<?>> tagsSetting) {
        final boolean areAllEmpty = tagsSetting.stream().map(ATags::getTags).allMatch(List::isEmpty);
        if (areAllEmpty) {
            reportEmptySettings(tagsSetting);
        }
    }

    private void validateDocumentations(final List<SuiteDocumentation> documentations) {
        final boolean areAllEmpty = documentations.stream().map(SuiteDocumentation::getDocumentationText).allMatch(
                List::isEmpty);
        if (areAllEmpty) {
            reportEmptySettings(documentations);
        }
    }

    private void validateMetadatas(final List<Metadata> metadatas) {
        final List<Metadata> emptyMetadatas = metadatas.stream().filter(metadata -> metadata.getKey() == null).collect(
                toList());
        reportEmptySettings(emptyMetadatas);
    }

    private void reportEmptySettings(final List<? extends AModelElement<?>> elements) {
        for (final AModelElement<?> element : elements) {
            final RobotToken settingToken = element.getDeclaration();
            final String settingName = settingToken.getText();
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                    .formatMessageWith(settingName);
            reporter.handleProblem(problem, validationContext.getFile(), settingToken);
        }
    }

    private void validateTestAndTasksSettingsMixes(final RobotSuiteFile model, final SettingTable settingsTable) {
        if (model.isSuiteFile()) {
            final Stream<AModelElement<?>> taskSettings = Streams.concat(settingsTable.getTaskSetups().stream(),
                    settingsTable.getTaskTeardowns().stream(), settingsTable.getTaskTemplates().stream(),
                    settingsTable.getTaskTimeouts().stream());
            taskSettings.map(AModelElement::getDeclaration)
                    .forEach(settingDecl -> reportTestAndTaskSettingMixed(
                            GeneralSettingsProblem.TASK_SETTING_USED_IN_TESTS_SUITE, settingDecl));

        } else if (model.isRpaSuiteFile()) {
            final Stream<AModelElement<?>> testSettings = Streams.concat(settingsTable.getTestSetups().stream(),
                    settingsTable.getTestTeardowns().stream(), settingsTable.getTestTemplates().stream(),
                    settingsTable.getTestTimeouts().stream());
            testSettings.map(AModelElement::getDeclaration)
                    .forEach(settingDecl -> reportTestAndTaskSettingMixed(
                            GeneralSettingsProblem.TEST_SETTING_USED_IN_TASKS_SUITE, settingDecl));
        }
    }

    private void reportTestAndTaskSettingMixed(final IProblemCause cause, final RobotToken declaration) {
        final String settingName = declaration.getText();
        final RobotProblem problem = RobotProblem.causedBy(cause).formatMessageWith(settingName);
        reporter.handleProblem(problem, validationContext.getFile(), declaration,
                ImmutableMap.of(AdditionalMarkerAttributes.NAME, settingName));
    }

    private void validateKeywordsAndVariablesUsagesInExecutables(final SettingTable settingsTable) {
        final Set<String> additionalVariables = new HashSet<>();

        final List<ExecutableValidator> execValidators = new ArrayList<>();
        for (final SuiteSetup suiteSetup : settingsTable.getSuiteSetupsViews()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables, suiteSetup, reporter));
        }
        for (final TestSetup testSetup : settingsTable.getTestSetupsViews()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables, testSetup, reporter));
        }
        for (final TaskSetup taskSetup : settingsTable.getTaskSetups()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables, taskSetup, reporter));
        }
        for (final TaskTeardown taskTeardown : settingsTable.getTaskTeardowns()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables, taskTeardown, reporter));
        }
        for (final TestTeardown testTeardown : settingsTable.getTestTeardownsViews()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables, testTeardown, reporter));
        }
        for (final SuiteTeardown suiteTeardown : settingsTable.getSuiteTeardownsViews()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables, suiteTeardown, reporter));
        }
        execValidators.forEach(ExecutableValidator::validate);
    }

    private void reportUnknownVariablesInNonExecutables(final SettingTable settingsTable) {
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);

        for (final TestTimeout testTimeout : settingsTable.getTestTimeouts()) {
            unknownVarsValidator.reportUnknownVars(testTimeout.getTimeout());
        }
        for (final TaskTimeout taskTimeout : settingsTable.getTaskTimeouts()) {
            unknownVarsValidator.reportUnknownVars(taskTimeout.getTimeout());
        }
        for (final DefaultTags defaultTag : settingsTable.getDefaultTags()) {
            unknownVarsValidator.reportUnknownVars(defaultTag.getTags());
        }
        for (final ForceTags forceTag : settingsTable.getForceTags()) {
            unknownVarsValidator.reportUnknownVars(forceTag.getTags());
        }
    }
}