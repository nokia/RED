/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.UnknownSetting;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting.DeprecatedSettingHeaderAlias;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting.DocumentationDeclarationSettingValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting.LibraryAliasesDeclarationUpperCaseValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting.MetaDeclarationSettingValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting.SuitePostconditionDeclarationExistenceValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting.SuitePreconditionDeclarationExistenceValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting.TestPostconditionDeclarationExistenceValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting.TestPreconditionDeclarationExistenceValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

class GeneralSettingsTableValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final Optional<RobotSettingsSection> settingsSection;

    private final ProblemsReportingStrategy reporter;

    private final VersionDependentValidators versionDependentValidators;

    GeneralSettingsTableValidator(final FileValidationContext validationContext,
            final Optional<RobotSettingsSection> settingSection, final ProblemsReportingStrategy reporter) {
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

        validateByExternal(settingsSection.get(), monitor);

        reportVersionSpecificProblems(settingsSection.get(), monitor);
        reportUnknownSettings(settingsTable.getUnknownSettings());

        validateLibraries(suiteFile, getLibraryImports(settingsTable), monitor);
        validateResources(suiteFile, getResourcesImports(settingsTable), monitor);
        validateVariables(suiteFile, getVariablesImports(settingsTable), monitor);

        final List<RobotExecutableRow<?>> executableRowsInSetupsAndTeardowns = newArrayList();
        validateSetupsAndTeardowns(settingsTable.getSuiteSetups(), executableRowsInSetupsAndTeardowns);
        validateSetupsAndTeardowns(settingsTable.getSuiteTeardowns(), executableRowsInSetupsAndTeardowns);
        validateSetupsAndTeardowns(settingsTable.getTestSetups(), executableRowsInSetupsAndTeardowns);
        validateSetupsAndTeardowns(settingsTable.getTestTeardowns(), executableRowsInSetupsAndTeardowns);
        validateVariablesInSetupAndTeardownExeRows(executableRowsInSetupsAndTeardowns);

        validateTestTemplates(settingsTable.getTestTemplates());
        validateTestTimeouts(settingsTable.getTestTimeouts());
        validateTags(settingsTable.getDefaultTags());
        validateTags(settingsTable.getForceTags());
        validateDocumentations(settingsTable.getDocumentation());
        validateMetadatas(settingsTable.getMetadatas());
    }

    private void validateByExternal(final RobotSettingsSection section, final IProgressMonitor monitor)
            throws CoreException {
        new MetaDeclarationSettingValidator(validationContext.getFile(), section, reporter).validate(monitor);
        new DocumentationDeclarationSettingValidator(validationContext.getFile(), section, reporter).validate(monitor);
        new SuitePreconditionDeclarationExistenceValidator(validationContext.getFile(), reporter, section)
                .validate(monitor);
        new SuitePostconditionDeclarationExistenceValidator(validationContext.getFile(), reporter, section)
                .validate(monitor);
        new TestPreconditionDeclarationExistenceValidator(validationContext.getFile(), reporter, section)
                .validate(monitor);
        new TestPostconditionDeclarationExistenceValidator(validationContext.getFile(), reporter, section)
                .validate(monitor);
        new DeprecatedSettingHeaderAlias(validationContext.getFile(), reporter, section).validate(monitor);
        new LibraryAliasesDeclarationUpperCaseValidator(validationContext.getFile(), reporter, section)
                .validate(monitor);
    }

    private void reportVersionSpecificProblems(final RobotSettingsSection section, final IProgressMonitor monitor)
            throws CoreException {
        final Iterable<VersionDependentModelUnitValidator> validators = versionDependentValidators
                .getGeneralSettingsValidators(validationContext, section, reporter);
        for (final ModelUnitValidator validator : validators) {
            validator.validate(monitor);
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

    private static List<VariablesImport> getVariablesImports(final SettingTable settingsTable) {
        return newArrayList(Iterables.filter(settingsTable.getImports(), VariablesImport.class));
    }

    private static List<ResourceImport> getResourcesImports(final SettingTable settingsTable) {
        return newArrayList(Iterables.filter(settingsTable.getImports(), ResourceImport.class));
    }

    private static List<LibraryImport> getLibraryImports(final SettingTable settingsTable) {
        return newArrayList(Iterables.filter(settingsTable.getImports(), LibraryImport.class));
    }

    private void validateLibraries(final RobotSuiteFile suiteFile, final List<LibraryImport> libraryImports,
            final IProgressMonitor monitor) throws CoreException {
        new GeneralSettingsLibrariesImportValidator(validationContext, suiteFile, libraryImports, reporter)
                .validate(monitor);
    }

    private void validateVariables(final RobotSuiteFile suiteFile, final List<VariablesImport> variablesImports,
            final IProgressMonitor monitor) throws CoreException {
        new GeneralSettingsVariablesImportValidator(validationContext, suiteFile, variablesImports, reporter)
                .validate(monitor);
    }

    private void validateResources(final RobotSuiteFile suiteFile, final List<ResourceImport> resourcesImports,
            final IProgressMonitor monitor) throws CoreException {
        new GeneralSettingsResourcesImportValidator(validationContext, suiteFile, resourcesImports, reporter)
                .validate(monitor);
    }

    private void validateSetupsAndTeardowns(final List<? extends AKeywordBaseSetting<?>> keywordBasedSettings,
            final List<RobotExecutableRow<?>> executableRowsForVariablesChecking) {

        boolean wasAllEmpty = true;
        for (final AKeywordBaseSetting<?> keywordBased : keywordBasedSettings) {
            final RobotToken keywordToken = keywordBased.getKeywordName();
            if (keywordToken != null) {
                wasAllEmpty = false;
                TestCaseTableValidator.reportKeywordUsageProblemsInSetupAndTeardownSetting(validationContext, reporter,
                        keywordToken, Optional.of(keywordBased.getArguments()));
            }
        }

        if (wasAllEmpty) {
            for (final AKeywordBaseSetting<?> keywordBased : keywordBasedSettings) {
                final RobotToken settingToken = keywordBased.getDeclaration();
                final String settingName = settingToken.getText();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, validationContext.getFile(), settingToken);
            }
        } else {
            executableRowsForVariablesChecking.add(keywordBasedSettings.get(0).asExecutableRow());
        }
    }

    private void validateTestTemplates(final List<TestTemplate> testTemplates) {
        boolean wasAllEmpty = true;
        for (final TestTemplate template : testTemplates) {
            final RobotToken settingToken = template.getDeclaration();
            final RobotToken keywordToken = template.getKeywordName();
            if (keywordToken != null) {
                wasAllEmpty = false;
                final String keywordName = keywordToken.getText();
                if (keywordName.toLowerCase().equals("none")) {
                    continue;
                }
                TestCaseTableValidator.validateExistingKeywordCall(validationContext, reporter, keywordToken,
                        Optional.empty());
            }
            if (!template.getUnexpectedTrashArguments().isEmpty()) {
                final String actualArgs = "[" + Joiner.on(", ").join(toString(template.getUnexpectedTrashArguments()))
                        + "]";
                final String additionalMsg = "Only keyword name should be specified for templates.";
                final RobotProblem problem = RobotProblem
                        .causedBy(GeneralSettingsProblem.SETTING_ARGUMENTS_NOT_APPLICABLE)
                        .formatMessageWith(settingToken.getText(), actualArgs, additionalMsg);
                reporter.handleProblem(problem, validationContext.getFile(), settingToken);
            }
        }

        if (wasAllEmpty) {
            for (final TestTemplate template : testTemplates) {
                final RobotToken settingToken = template.getDeclaration();
                final String settingName = settingToken.getText();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, validationContext.getFile(), settingToken);
            }
        }
    }

    private void validateTestTimeouts(final List<TestTimeout> timeouts) {
        boolean wasAllEmpty = true;
        for (final TestTimeout testTimeout : timeouts) {
            final RobotToken timeoutToken = testTimeout.getTimeout();
            if (timeoutToken != null) {
                wasAllEmpty = false;
                TestCaseTableValidator.validateTimeoutSetting(validationContext, reporter,
                        validationContext.getAccessibleVariables(), timeoutToken);
            }
        }

        if (wasAllEmpty) {
            for (final TestTimeout testTimeout : timeouts) {
                final RobotToken settingToken = testTimeout.getDeclaration();
                final String settingName = settingToken.getText();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, validationContext.getFile(), settingToken);
            }
        }
    }

    private void validateTags(final List<? extends ATags<?>> tagsSetting) {
        boolean wasAllEmpty = true;

        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);
        for (final ATags<?> tags : tagsSetting) {
            if (!tags.getTags().isEmpty()) {
                wasAllEmpty = false;
                final Set<String> accessibleVariables = validationContext.getAccessibleVariables();
                unknownVarsValidator.reportUnknownVars(tags.getTags(), accessibleVariables);
            }
        }

        if (wasAllEmpty) {
            for (final ATags<?> tags : tagsSetting) {
                final RobotToken declarationToken = tags.getDeclaration();
                final String settingName = declarationToken.getText();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, validationContext.getFile(), declarationToken);
            }
        }
    }

    private void validateMetadatas(final List<Metadata> metadatas) {
        for (final Metadata metadata : metadatas) {
            if (metadata.getKey() == null) {
                final RobotToken declarationToken = metadata.getDeclaration();
                final String settingName = declarationToken.getText();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, validationContext.getFile(), declarationToken);
            }
        }
    }

    private void validateDocumentations(final List<SuiteDocumentation> documentations) {
        boolean wasAllEmpty = true;
        for (final SuiteDocumentation docu : documentations) {
            if (!docu.getDocumentationText().isEmpty()) {
                wasAllEmpty = false;
                break;
            }
        }

        if (wasAllEmpty) {
            for (final SuiteDocumentation docu : documentations) {
                final RobotToken declarationToken = docu.getDeclaration();
                final String settingName = declarationToken.getText();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, validationContext.getFile(), declarationToken);
            }
        }
    }

    private void validateVariablesInSetupAndTeardownExeRows(final List<RobotExecutableRow<?>> executableRows) {
        if (!executableRows.isEmpty()) {
            final Set<String> variables = validationContext.getAccessibleVariables();
            TestCaseTableValidator.reportUnknownVariables(validationContext, reporter, executableRows, variables);
        }
    }

    private static List<String> toString(final List<RobotToken> tokens) {
        return newArrayList(Lists.transform(tokens, token -> token.getText().trim()));
    }
}
