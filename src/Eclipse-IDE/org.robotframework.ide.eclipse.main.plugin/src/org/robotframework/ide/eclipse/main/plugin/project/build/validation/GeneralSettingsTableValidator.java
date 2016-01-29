/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ATags;
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
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.KeywordValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
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
        final IFile file = suiteFile.getFile();
        final SettingTable settingsTable = (SettingTable) settingsSection.get().getLinkedElement();

        reportVersionSpecificProblems(file, settingsSection.get(), monitor);

        reportUnknownSettings(file, settingsTable.getUnknownSettings());

        validateLibraries(suiteFile, getLibraryImports(settingsTable), monitor);
        validateResources(suiteFile, getResourcesImports(settingsTable), monitor);
        validateVariables(suiteFile, getVariablesImports(settingsTable), monitor);

        validateSetupsAndTeardowns(file, getKeywordBasedSettings(settingsTable));
        validateTestTemplates(file, settingsTable.getTestTemplates());
        validateTestTimeouts(file, settingsTable.getTestTimeouts());
        validateTags(file, getTags(settingsTable));
        validateDocumentations(file, settingsTable.getDocumentation());
        validateMetadatas(file, settingsTable.getMetadatas());
    }

    private void reportVersionSpecificProblems(final IFile file, final RobotSettingsSection section,
            final IProgressMonitor monitor) throws CoreException {
        final List<? extends ModelUnitValidator> validators = versionDependentValidators
                .getGeneralSettingsValidators(file, section, reporter, validationContext.getVersion());
        for (final ModelUnitValidator validator : validators) {
            validator.validate(monitor);
        }
    }

    private List<ATags<?>> getTags(final SettingTable settingsTable) {
        final List<ATags<?>> tags = newArrayList();
        tags.addAll(settingsTable.getDefaultTags());
        tags.addAll(settingsTable.getForceTags());
        return tags;
    }

    private void reportUnknownSettings(final IFile file, final List<UnknownSetting> unknownSettings) {
        for (final UnknownSetting unknownSetting : unknownSettings) {
            final RobotToken token = unknownSetting.getDeclaration();
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNKNOWN_SETTING)
                    .formatMessageWith(token.getText().toString());
            reporter.handleProblem(problem, file, token);
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
        new GeneralSettingsImportsValidator.LibraryImportValidator(validationContext, suiteFile, libraryImports,
                reporter).validate(monitor);
    }

    private void validateVariables(final RobotSuiteFile suiteFile, final List<VariablesImport> variablesImports,
            final IProgressMonitor monitor) throws CoreException {
        new GeneralSettingsImportsValidator.VariablesImportValidator(validationContext, suiteFile, variablesImports,
                reporter).validate(monitor);
    }

    private void validateResources(final RobotSuiteFile suiteFile, final List<ResourceImport> resourcesImports,
            final IProgressMonitor monitor) throws CoreException {
        new GeneralSettingsImportsValidator.ResourcesImportValidator(validationContext, suiteFile, resourcesImports,
                reporter).validate(monitor);
    }

    private static List<AKeywordBaseSetting<?>> getKeywordBasedSettings(final SettingTable settingsTable) {
        final List<AKeywordBaseSetting<?>> settings = newArrayList();
        settings.addAll(settingsTable.getSuiteSetups());
        settings.addAll(settingsTable.getSuiteTeardowns());
        settings.addAll(settingsTable.getTestSetups());
        settings.addAll(settingsTable.getTestTeardowns());
        return settings;
    }

    private void validateSetupsAndTeardowns(final IFile file, final List<AKeywordBaseSetting<?>> keywordBasedSettings) {
        for (final AKeywordBaseSetting<?> keywordBased : keywordBasedSettings) {
            final RobotToken keywordToken = keywordBased.getKeywordName();
            if (keywordToken == null) {
                final RobotToken settingToken = keywordBased.getDeclaration();
                final String settingName = settingToken.getText().toString();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, file, settingToken);
            } else {
                final String keywordName = keywordToken.getText().toString();

                if (!validationContext.isKeywordAccessible(keywordName)) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.UNKNOWN_KEYWORD)
                            .formatMessageWith(keywordName);
                    final Map<String, Object> additional = ImmutableMap.<String, Object> of(
                            AdditionalMarkerAttributes.NAME, keywordName,
                            AdditionalMarkerAttributes.ORIGINAL_NAME, keywordName);
                    reporter.handleProblem(problem, file, keywordToken, additional);
                }
                final List<String> sources = validationContext.getKeywordSourceNames(keywordName);
                if (sources.size() > 1) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(KeywordsProblem.AMBIGUOUS_KEYWORD).formatMessageWith(keywordName,
                                    "[" + Joiner.on(", ").join(sources) + "]"),
                            file, keywordToken,
                            ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME, keywordName,
                                    AdditionalMarkerAttributes.ORIGINAL_NAME, keywordName,
                                    AdditionalMarkerAttributes.SOURCES, Joiner.on(';').join(sources)));
                }
                if (validationContext.isKeywordDeprecated(keywordName)) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DEPRECATED_KEYWORD)
                            .formatMessageWith(keywordName);
                    reporter.handleProblem(problem, file, keywordToken);
                }
                if (validationContext.isKeywordFromNestedLibrary(keywordName)) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY)
                            .formatMessageWith(keywordName);
                    reporter.handleProblem(problem, file, keywordToken);
                }
                final KeywordValidationContext keywordValidationContext = validationContext.checkIfKeywordOccurrenceIsEqualToDefinition(keywordName);
                if (keywordValidationContext != null) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION)
                                    .formatMessageWith(keywordName,
                                            keywordValidationContext.getNameFromKeywordDefinition()), file,
                            keywordToken, ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME,
                                    keywordName, AdditionalMarkerAttributes.ORIGINAL_NAME,
                                    keywordValidationContext.getNameFromKeywordDefinition(),
                                    AdditionalMarkerAttributes.SOURCES, keywordValidationContext.getSourceNameInUse()));

                }
            }
        }
    }

    private void validateTestTemplates(final IFile file, final List<TestTemplate> testTemplates) {
        for (final TestTemplate template : testTemplates) {
            final RobotToken settingToken = template.getDeclaration();
            final RobotToken keywordToken = template.getKeywordName();
            if (keywordToken == null) {

                final String settingName = settingToken.getText().toString();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, file, settingToken);
            } else {
                
                final String keywordName = keywordToken.getText().toString();
                
                if (keywordName.toLowerCase().equals("none")) {
                    continue;
                }

                if (!validationContext.isKeywordAccessible(keywordName)) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.UNKNOWN_KEYWORD)
                            .formatMessageWith(keywordName);
                    final Map<String, Object> additional = ImmutableMap.<String, Object> of(
                            AdditionalMarkerAttributes.NAME, keywordName,
                            AdditionalMarkerAttributes.ORIGINAL_NAME, keywordName);
                    reporter.handleProblem(problem, file, keywordToken, additional);
                }
                final List<String> sources = validationContext.getKeywordSourceNames(keywordName);
                if (sources.size() > 1) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(KeywordsProblem.AMBIGUOUS_KEYWORD).formatMessageWith(keywordName,
                                    "[" + Joiner.on(", ").join(sources) + "]"),
                            file, keywordToken,
                            ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME, keywordName,
                                    AdditionalMarkerAttributes.ORIGINAL_NAME, keywordName,
                                    AdditionalMarkerAttributes.SOURCES, Joiner.on(';').join(sources)));
                }
                if (validationContext.isKeywordDeprecated(keywordName)) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DEPRECATED_KEYWORD)
                            .formatMessageWith(keywordName);
                    reporter.handleProblem(problem, file, keywordToken);
                }
                if (validationContext.isKeywordFromNestedLibrary(keywordName)) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY)
                            .formatMessageWith(keywordName);
                    reporter.handleProblem(problem, file, keywordToken);
                }
                final KeywordValidationContext keywordValidationContext = validationContext.checkIfKeywordOccurrenceIsEqualToDefinition(keywordName);
                if (keywordValidationContext != null) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION)
                                    .formatMessageWith(keywordName,
                                            keywordValidationContext.getNameFromKeywordDefinition()), file,
                            keywordToken, ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME,
                                    keywordName, AdditionalMarkerAttributes.ORIGINAL_NAME,
                                    keywordValidationContext.getNameFromKeywordDefinition(),
                                    AdditionalMarkerAttributes.SOURCES, keywordValidationContext.getSourceNameInUse()));

                }
            }
            if (!template.getUnexpectedTrashArguments().isEmpty()) {

                final String actualArgs = "[" + Joiner.on(", ").join(toString(template.getUnexpectedTrashArguments()))
                        + "]";
                final String additionalMsg = "Only keyword name should be specified for templates.";
                final RobotProblem problem = RobotProblem
                        .causedBy(GeneralSettingsProblem.SETTING_ARGUMENTS_NOT_APPLICABLE)
                        .formatMessageWith(settingToken.getText().toString(), actualArgs, additionalMsg);
                reporter.handleProblem(problem, file, settingToken);
            }
        }

    }

    private void validateTestTimeouts(final IFile file, final List<TestTimeout> timeouts) {
        for (final TestTimeout testTimeout : timeouts) {
            final RobotToken timeoutToken = testTimeout.getTimeout();
            if (timeoutToken == null) {

                final RobotToken settingToken = testTimeout.getDeclaration();
                final String settingName = settingToken.getText().toString();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, file, settingToken);
            } else {
                final String timeout = timeoutToken.getText().toString();
                if (!RobotTimeFormat.isValidRobotTimeArgument(timeout.trim())) {
                    final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_TIME_FORMAT)
                            .formatMessageWith(timeout);
                    reporter.handleProblem(problem, file, timeoutToken);
                }
            }
        }
    }

    private void validateTags(final IFile file, final List<ATags<?>> tagsSetting) {
        for (final ATags<?> tags : tagsSetting) {
            if (tags.getTags().isEmpty()) {
                final RobotToken declarationToken = tags.getDeclaration();
                final String settingName = declarationToken.getText().toString();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, file, declarationToken);
            }
        }
    }

    private void validateMetadatas(final IFile file, final List<Metadata> metadatas) {
        for (final Metadata metadata : metadatas) {
            if (metadata.getKey() == null) {
                final RobotToken declarationToken = metadata.getDeclaration();
                final String settingName = declarationToken.getText().toString();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, file, declarationToken);
            }
        }
    }

    private void validateDocumentations(final IFile file, final List<SuiteDocumentation> documentations) {
        for (final SuiteDocumentation docu : documentations) {
            if (docu.getDocumentationText().isEmpty()) {
                final RobotToken declarationToken = docu.getDeclaration();
                final String settingName = declarationToken.getText().toString();
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.EMPTY_SETTING)
                        .formatMessageWith(settingName);
                reporter.handleProblem(problem, file, declarationToken);
            }
        }

    }

    private static List<String> toString(final List<RobotToken> tokens) {
        return newArrayList(Lists.transform(tokens, new Function<RobotToken, String>() {

            @Override
            public String apply(final RobotToken token) {
                return token.getText().toString().trim();
            }
        }));
    }
}
