/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.TemplateSetting;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;

import com.google.common.collect.Range;

public class VersionDependentValidators {

    private final FileValidationContext validationContext;

    private final ValidationReportingStrategy reporter;

    public VersionDependentValidators(final FileValidationContext validationContext,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.reporter = reporter;
    }

    public Stream<VersionDependentModelUnitValidator> getFileValidators(final RobotSuiteFile fileModel,
            final Class<? extends RobotSuiteFileSection> section) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new DeprecatedSuiteFileExtensionValidator(file, fileModel, section, reporter),
                new UnsupportedSuiteFileExtensionValidator(file, fileModel, section, reporter),
                new DeprecatedVariableCollectionElementUseValidator(file, fileModel, reporter));
        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getResourceValidators(final RobotSuiteFile fileModel) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream
                .of(new DeprecatedVariableCollectionElementUseValidator(file, fileModel, reporter));
        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getGeneralSettingsTableValidators(final SettingTable table) {
        final IFile file = validationContext.getFile();
        final String settingDuplDetail = getDuplicatedSettingDetailInfo();

        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new DeprecatedGeneralSettingsTableHeaderValidator(file, table, reporter),

                new SettingsDuplicationInOldRfValidator<>(file, table::getTestTemplates, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestTemplates, reporter, settingDuplDetail),

                new SettingsDuplicationValidator<>(file, table::getTaskTemplates, reporter, settingDuplDetail),

                new SettingsDuplicationInOldRfValidator<>(file, table::getSuiteSetups, reporter),
                new SettingsDuplicationValidator<>(file, table::getSuiteSetups, reporter, settingDuplDetail),

                new SettingsDuplicationInOldRfValidator<>(file, table::getSuiteTeardowns, reporter),
                new SettingsDuplicationValidator<>(file, table::getSuiteTeardowns, reporter, settingDuplDetail),

                new SettingsDuplicationInOldRfValidator<>(file, table::getTestSetups, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestSetups, reporter, settingDuplDetail),

                new SettingsDuplicationInOldRfValidator<>(file, table::getTestTeardowns, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestTeardowns, reporter, settingDuplDetail),

                new SettingsDuplicationValidator<>(file, table::getTaskSetups, reporter, settingDuplDetail),

                new SettingsDuplicationValidator<>(file, table::getTaskTeardowns, reporter, settingDuplDetail),

                new SettingsDuplicationInOldRfValidator<>(file, table::getTestTimeouts, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestTimeouts, reporter, settingDuplDetail),

                new SettingsDuplicationValidator<>(file, table::getTaskTimeouts, reporter, settingDuplDetail),

                new SettingsDuplicationInOldRfValidator<>(file, table::getForceTags, reporter),
                new SettingsDuplicationValidator<>(file, table::getForceTags, reporter, settingDuplDetail),

                new SettingsDuplicationInOldRfValidator<>(file, table::getDefaultTags, reporter),
                new SettingsDuplicationValidator<>(file, table::getDefaultTags, reporter, settingDuplDetail),

                new SettingsDuplicationInOldRfValidator<>(file, table::getDocumentation, reporter),
                new SettingsDuplicationValidator<>(file, table::getDocumentation, reporter, settingDuplDetail),

                new DeprecatedGeneralSettingNameValidator(file, table, reporter),
                new MetadataKeyInColumnOfSettingValidatorUntilRF30(file, table, reporter),
                new TemplateSettingUntilRf31Validator(validationContext, table.getTestTemplatesViews(), reporter),
                new TemplateSettingUntilRf31Validator(validationContext, table.getTaskTemplates(), reporter),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, table::getTestTimeouts, reporter,
                        ". No timeout will be checked"),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, table::getTaskTimeouts, reporter,
                        ". No timeout will be checked"),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, table::getTestTemplates, reporter,
                        ". No template will be used in this suite unless one is defined locally in test"),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, table::getTaskTemplates, reporter,
                        ". No template will be used in this suite unless one is defined locally in task"),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, table::getResourcesImports,
                        reporter, ". File will not be imported"),
                new TimeoutMessageValidator<>(file, table::getTestTimeouts, TestTimeout::getMessageArguments, reporter),
                new LibraryAliasNotInUpperCaseValidator(file, table, reporter),
                new LibraryAliasNotInUpperCaseValidator31(file, table, reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getKeywordTableValidators(final KeywordTable table) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new DeprecatedKeywordTableHeaderValidator(file, table, reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getTestCaseValidators(final TestCase testCase) {
        final Range<RobotVersion> varInTestNameApplicableVersion = Range.atLeast(new RobotVersion(3, 2));
        return Stream
                .<VersionDependentModelUnitValidator> of(new VariableUsageInTokenValidator(validationContext,
                        varInTestNameApplicableVersion, testCase.getName(), reporter))
                .filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getTestCaseSettingsValidators(final TestCase testCase) {
        final String settingDuplDetail = getDuplicatedSettingDetailInfo();
        final IFile file = validationContext.getFile();
        final List<RobotLine> fileContent = testCase.getParent().getParent().getFileContent();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new LocalSettingsDuplicationInOldRfValidator(file, fileContent, testCase.getBeginPosition(),
                        testCase.getEndPosition(), RobotTokenType.TEST_CASE_SETTING_NAME_DUPLICATION, reporter),
                new SettingsDuplicationValidator<>(file, testCase::getSetups, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, testCase::getTeardowns, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, testCase::getTemplates, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, testCase::getTimeouts, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, testCase::getTags, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, testCase::getDocumentation, reporter, settingDuplDetail),
                new DeprecatedTestCaseSettingNameValidator(file, testCase, reporter),
                new TemplateSettingUntilRf31Validator(validationContext,
                        testCase.getTemplates().stream().map(t -> t.adaptTo(TemplateSetting.class)).collect(toList()),
                        reporter),
                new TimeoutMessageValidator<>(file, testCase::getTimeouts,
                        timeout -> timeout.tokensOf(RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE).collect(toList()),
                        reporter),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, testCase::getTimeouts, reporter,
                        ". No timeout will be checked"),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, testCase::getTemplates, reporter,
                        ". No template will be used in this test unless defined in suite settings"));
        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getTaskValidators(final Task task) {
        final Range<RobotVersion> varInTaskApplicableVersion = Range.atLeast(new RobotVersion(3, 2));
        return Stream
                .<VersionDependentModelUnitValidator> of(new VariableUsageInTokenValidator(validationContext,
                        varInTaskApplicableVersion, task.getName(), reporter))
                .filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getTaskSettingsValidators(final Task task) {
        final String settingDuplDetail = getDuplicatedSettingDetailInfo();
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new SettingsDuplicationValidator<>(file, task::getSetups, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, task::getTeardowns, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, task::getTemplates, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, task::getTimeouts, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, task::getTags, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, task::getDocumentation, reporter, settingDuplDetail),

                new TemplateSettingUntilRf31Validator(validationContext,
                        task.getTemplates().stream().map(t -> t.adaptTo(TemplateSetting.class)).collect(toList()),
                        reporter),
                new TimeoutMessageValidator<>(file, task::getTimeouts,
                        timeout -> timeout.tokensOf(RobotTokenType.TASK_SETTING_TIMEOUT_MESSAGE).collect(toList()),
                        reporter),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, task::getTimeouts, reporter,
                        ". No timeout will be checked"),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, task::getTemplates, reporter,
                        ". No template will be used in this task unless defined in suite settings"));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getKeywordSettingsValidators(final UserKeyword keyword) {
        final String settingDuplDetail = getDuplicatedSettingDetailInfo();
        final IFile file = validationContext.getFile();
        final List<RobotLine> fileContent = keyword.getParent().getParent().getFileContent();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new LocalSettingsDuplicationInOldRfValidator(file, fileContent, keyword.getBeginPosition(),
                        keyword.getEndPosition(), RobotTokenType.KEYWORD_SETTING_NAME_DUPLICATION, reporter),
                new SettingsDuplicationValidator<>(file, keyword::getArguments, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, keyword::getTeardowns, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, keyword::getReturns, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, keyword::getTimeouts, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, keyword::getTags, reporter, settingDuplDetail),
                new SettingsDuplicationValidator<>(file, keyword::getDocumentation, reporter, settingDuplDetail),
                new DeprecatedKeywordSettingNameValidator(file, keyword, reporter),
                new TimeoutMessageValidator<>(file, keyword::getTimeouts,
                        timeout -> timeout.tokensOf(RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE).collect(toList()),
                        reporter),
                new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(file, keyword::getTimeouts, reporter,
                        ". No timeout will be checked"));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    private String getDuplicatedSettingDetailInfo() {
        return validationContext.getVersion().isNewerOrEqualTo(new RobotVersion(3, 2))
                ? ". Only first setting will be used"
                : ". It will not be applied";
    }

    public Stream<VersionDependentModelUnitValidator> getForLoopValidators(
            final ForLoopDeclarationRowDescriptor<?> descriptor) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream
                .<VersionDependentModelUnitValidator> of(new ForLoopInExpressionsValidator(file, descriptor, reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }
}
