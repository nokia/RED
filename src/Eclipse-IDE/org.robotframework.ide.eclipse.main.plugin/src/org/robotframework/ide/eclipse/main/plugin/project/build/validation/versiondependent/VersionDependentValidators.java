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
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;

public class VersionDependentValidators {

    public Iterable<VersionDependentModelUnitValidator> getVariableValidators(
            final FileValidationContext validationContext, final IVariableHolder variable,
            final ValidationReportingStrategy reporter) {
        final IFile file = validationContext.getFile();

        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new DictionaryExistenceValidator(file, variable, reporter),
                new ScalarAsListInOlderRobotValidator(file, variable, reporter),
                new ScalarAsListValidator(file, variable, reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()))
                .collect(toList());
    }

    public Iterable<VersionDependentModelUnitValidator> getGeneralSettingsValidators(
            final FileValidationContext validationContext, final SettingTable table,
            final ValidationReportingStrategy reporter) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new SettingsDuplicationInOldRfValidator<>(file, table::getTestTemplates, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestTemplates, reporter, ". No template will be used"),
                
                new SettingsDuplicationInOldRfValidator<>(file, table::getSuiteSetups, reporter),
                new SettingsDuplicationValidator<>(file, table::getSuiteSetups, reporter, ". No Suite Setup will be executed"),
                
                new SettingsDuplicationInOldRfValidator<>(file, table::getSuiteTeardowns, reporter),
                new SettingsDuplicationValidator<>(file, table::getSuiteTeardowns, reporter, ". No Suite Teardown will be executed"),
                
                new SettingsDuplicationInOldRfValidator<>(file, table::getTestSetups, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestSetups, reporter, ". No Test Setup will be executed"),
                
                new SettingsDuplicationInOldRfValidator<>(file, table::getTestTeardowns, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestTeardowns, reporter, ". No Test Teardown will be executed"),
                
                new SettingsDuplicationInOldRfValidator<>(file, table::getTestTimeouts, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestTimeouts, reporter, ". No timeout will be checked"),
                
                new SettingsDuplicationInOldRfValidator<>(file, table::getForceTags, reporter),
                new SettingsDuplicationValidator<>(file, table::getForceTags, reporter),
                
                new SettingsDuplicationInOldRfValidator<>(file, table::getDefaultTags, reporter),
                new SettingsDuplicationValidator<>(file, table::getDefaultTags, reporter),
                
                new SettingsDuplicationInOldRfValidator<>(file, table::getDocumentation, reporter),
                new SettingsDuplicationValidator<>(file, table::getDocumentation, reporter),
                
                new MetadataKeyInColumnOfSettingValidatorUntilRF30(file, table, reporter),
                new TimeoutMessageValidator<>(file, table::getTestTimeouts, timeout -> timeout.getMessageArguments(),
                        reporter),
                new LibraryAliasNotInUpperCaseValidator(file, table, reporter),
                new LibraryAliasNotInUpperCaseValidator31(file, table, reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()))
                .collect(toList());
    }

    public Stream<VersionDependentModelUnitValidator> getTestCaseSettingsValidators(
            final FileValidationContext validationContext, final TestCase testCase,
            final ValidationReportingStrategy reporter) {

        final IFile file = validationContext.getFile();
        final List<RobotLine> fileContent = testCase.getParent().getParent().getFileContent();
        return Stream
                .<VersionDependentModelUnitValidator> of(
                        new LocalSettingsDuplicationInOldRfValidator(file, fileContent, testCase.getBeginPosition(),
                                testCase.getEndPosition(), RobotTokenType.TEST_CASE_SETTING_NAME_DUPLICATION, reporter),
                        new SettingsDuplicationValidator<>(file, testCase::getSetups, reporter, ". No Setup will be executed"),
                        new SettingsDuplicationValidator<>(file, testCase::getTeardowns, reporter, ". No Teardown will be executed"),
                        new SettingsDuplicationValidator<>(file, testCase::getTemplates, reporter, ". No template will be used"),
                        new SettingsDuplicationValidator<>(file, testCase::getTimeouts, reporter, ". No timeout will be checked"),
                        new SettingsDuplicationValidator<>(file, testCase::getTags, reporter),
                        new SettingsDuplicationValidator<>(file, testCase::getDocumentation, reporter),
                        new TimeoutMessageValidator<>(file, testCase::getTimeouts, timeout -> timeout.getMessage(), reporter))
                .filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getKeywordSettingsValidators(
            final FileValidationContext validationContext, final UserKeyword keyword,
            final ValidationReportingStrategy reporter) {

        final IFile file = validationContext.getFile();
        final List<RobotLine> fileContent = keyword.getParent().getParent().getFileContent();
        return Stream
                .<VersionDependentModelUnitValidator> of(
                        new LocalSettingsDuplicationInOldRfValidator(file, fileContent,
                                keyword.getBeginPosition(), keyword.getEndPosition(),
                                RobotTokenType.KEYWORD_SETTING_NAME_DUPLICATION, reporter),
                        new SettingsDuplicationValidator<>(file, keyword::getArguments, reporter),
                        new SettingsDuplicationValidator<>(file, keyword::getTeardowns, reporter, ". No Teardown will be executed"),
                        new SettingsDuplicationValidator<>(file, keyword::getReturns, reporter),
                        new SettingsDuplicationValidator<>(file, keyword::getTimeouts, reporter, ". No timeout will be checked"),
                        new SettingsDuplicationValidator<>(file, keyword::getTags, reporter),
                        new SettingsDuplicationValidator<>(file, keyword::getDocumentation, reporter),
                        new TimeoutMessageValidator<>(file, keyword::getTimeouts, timeout -> timeout.getMessage(), reporter))
                .filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }
}
