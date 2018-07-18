/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.UnknownSetting;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.RobotTimeFormat;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

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
        this.versionDependentValidators = new VersionDependentValidators();
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!settingsSection.isPresent()) {
            return;
        }
        final RobotSuiteFile suiteFile = settingsSection.get().getSuiteFile();
        final SettingTable settingsTable = settingsSection.get().getLinkedElement();

        reportVersionSpecificProblems(settingsSection.get(), monitor);
        reportOutdatedTableName(settingsTable);
        reportOutdatedSettings(settingsTable);
        reportUnknownSettings(settingsTable.getUnknownSettings());

        validateLibraries(suiteFile, settingsTable.getLibraryImports(), monitor);
        validateResources(suiteFile, settingsTable.getResourcesImports(), monitor);
        validateVariables(suiteFile, settingsTable.getVariablesImports(), monitor);

        validateSetupsAndTeardowns(settingsTable.getSuiteSetups());
        validateSetupsAndTeardowns(settingsTable.getSuiteTeardowns());
        validateSetupsAndTeardowns(settingsTable.getTestSetups());
        validateSetupsAndTeardowns(settingsTable.getTestTeardowns());
        validateTestTemplates(settingsTable.getTestTemplates());
        validateTestTimeouts(settingsTable.getTestTimeouts());
        validateTags(settingsTable.getDefaultTags());
        validateTags(settingsTable.getForceTags());
        validateDocumentations(settingsTable.getDocumentation());
        validateMetadatas(settingsTable.getMetadatas());

        validateKeywordsAndVariablesUsagesInExecutables(settingsTable);
        reportUnknownVariablesInNonExecutables(settingsTable);
    }

    private void reportVersionSpecificProblems(final RobotSettingsSection section, final IProgressMonitor monitor)
            throws CoreException {
        final Iterable<VersionDependentModelUnitValidator> validators = versionDependentValidators
                .getGeneralSettingsValidators(validationContext, section, reporter);
        for (final ModelUnitValidator validator : validators) {
            validator.validate(monitor);
        }
    }

    private void reportOutdatedTableName(final SettingTable table) {
        for (final TableHeader<? extends ARobotSectionTable> th : table.getHeaders()) {
            final RobotToken declaration = th.getDeclaration();
            final String tableName = declaration.getText();
            final String tableNameWithoutWhiteSpaces = tableName.toLowerCase().replaceAll("\\s", "");
            if (tableNameWithoutWhiteSpaces.contains("metadata")) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.METADATA_TABLE_HEADER_SYNONYM)
                        .formatMessageWith(tableName), validationContext.getFile(), declaration);
            }
        }
    }

    private void reportOutdatedSettings(final SettingTable table) {
        reportOutdated(table.getDocumentation(), GeneralSettingsProblem.DOCUMENT_SYNONYM, "documentation");
        reportOutdated(table.getSuiteSetups(), GeneralSettingsProblem.SUITE_PRECONDITION_SYNONYM, "suitesetup");
        reportOutdated(table.getSuiteTeardowns(), GeneralSettingsProblem.SUITE_POSTCONDITION_SYNONYM, "suiteteardown");
        reportOutdated(table.getTestSetups(), GeneralSettingsProblem.TEST_PRECONDITION_SYNONYM, "testsetup");
        reportOutdated(table.getTestTeardowns(), GeneralSettingsProblem.TEST_POSTCONDITION_SYNONYM, "testteardown");
    }

    private void reportOutdated(final List<? extends AModelElement<?>> settings, final IProblemCause cause,
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

    private void reportUnknownSettings(final List<UnknownSetting> unknownSettings) {
        for (final UnknownSetting unknownSetting : unknownSettings) {
            final RobotToken token = unknownSetting.getDeclaration();
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNKNOWN_SETTING)
                    .formatMessageWith(token.getText());
            reporter.handleProblem(problem, validationContext.getFile(), token);
        }
    }

    private void validateLibraries(final RobotSuiteFile suiteFile, final List<LibraryImport> libraryImports,
            final IProgressMonitor monitor) throws CoreException {

        reportInvalidWithNameUsage(libraryImports);
        
        new GeneralSettingsLibrariesImportValidator(validationContext, suiteFile, libraryImports, reporter)
                .validate(monitor);
    }

    private void reportInvalidWithNameUsage(final List<LibraryImport> libraryImports) {
        for (final LibraryImport libImport : libraryImports) {
            final LibraryAlias alias = libImport.getAlias();
            if (alias.isPresent()) {
                final RobotToken withNameDeclaration = alias.getDeclaration();
                final String withName = withNameDeclaration.getText();
                if (withName.chars().anyMatch(Character::isLowerCase)) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(GeneralSettingsProblem.LIBRARY_WITH_NAME_NOT_UPPER_CASE_COMBINATION)
                                    .formatMessageWith(withName),
                            validationContext.getFile(), withNameDeclaration);
                }
            }
        }
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

    private void validateTestTemplates(final List<TestTemplate> testTemplates) {
        final boolean areAllEmpty = testTemplates.stream().map(TestTemplate::getKeywordName).allMatch(
                kwToken -> kwToken == null);
        if (areAllEmpty) {
            reportEmptySettings(testTemplates);
        }

        reportTemplateUnexpectedArguments(testTemplates);
        reportTemplateKeywordProblems(testTemplates);
    }

    private void reportTemplateUnexpectedArguments(final List<TestTemplate> testTemplates) {
        for (final TestTemplate template : testTemplates) {
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

    private void reportTemplateKeywordProblems(final List<TestTemplate> testTemplates) {
        for (final TestTemplate template : testTemplates) {
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

    private void validateTestTimeouts(final List<TestTimeout> timeouts) {
        final boolean areAllEmpty = timeouts.stream().map(TestTimeout::getTimeout).allMatch(
                timeoutToken -> timeoutToken == null);
        if (areAllEmpty) {
            reportEmptySettings(timeouts);
        }
        reportInvalidTimeoutSyntax(timeouts);
    }

    private void reportInvalidTimeoutSyntax(final List<TestTimeout> timeouts) {
        for (final TestTimeout testTimeout : timeouts) {
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

    private void validateKeywordsAndVariablesUsagesInExecutables(final SettingTable settingsTable) {
        final Set<String> additionalVariables = new HashSet<>();

        final List<ExecutableValidator> execValidators = new ArrayList<>();
        if (!settingsTable.getSuiteSetups().isEmpty()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables,
                    settingsTable.getSuiteSetups().get(0), reporter));
        }
        if (!settingsTable.getTestSetups().isEmpty()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables,
                    settingsTable.getTestSetups().get(0), reporter));
        }
        if (!settingsTable.getTestTeardowns().isEmpty()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables,
                    settingsTable.getTestTeardowns().get(0), reporter));
        }
        if (!settingsTable.getSuiteTeardowns().isEmpty()) {
            execValidators.add(ExecutableValidator.of(validationContext, additionalVariables,
                    settingsTable.getSuiteTeardowns().get(0), reporter));
        }
        execValidators.forEach(ExecutableValidator::validate);
    }

    private void reportUnknownVariablesInNonExecutables(final SettingTable settingsTable) {
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);

        for (final TestTimeout testTimeout : settingsTable.getTestTimeouts()) {
            unknownVarsValidator.reportUnknownVars(testTimeout.getTimeout());
        }
        for (final DefaultTags defaultTag : settingsTable.getDefaultTags()) {
            unknownVarsValidator.reportUnknownVars(defaultTag.getTags());
        }
        for (final ForceTags forceTag : settingsTable.getForceTags()) {
            unknownVarsValidator.reportUnknownVars(forceTag.getTags());
        }
    }
}