/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext;

public class VersionDependentValidatorsTest {

    @Nested
    public static class GeneralSettingsTableValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder29() {
            assertThat(getGeneralSettingsTableValidators("2.8.0")).hasSize(11)
                    .hasOnlyElementsOfTypes(SettingsDuplicationInOldRfValidator.class,
                            TemplateSettingUntilRf31Validator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion29() {
            assertThat(getGeneralSettingsTableValidators("2.9")).hasSize(12)
                    .hasOnlyElementsOfTypes(SettingsDuplicationInOldRfValidator.class,
                            MetadataKeyInColumnOfSettingValidatorUntilRF30.class,
                            TemplateSettingUntilRf31Validator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion30() {
            assertThat(getGeneralSettingsTableValidators("3.0.4")).hasSize(19)
                    .hasOnlyElementsOfTypes(DeprecatedGeneralSettingsTableHeaderValidator.class,
                            SettingsDuplicationValidator.class, DeprecatedGeneralSettingNameValidator.class,
                            TimeoutMessageValidator.class, LibraryAliasNotInUpperCaseValidator.class,
                            TemplateSettingUntilRf31Validator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion31() {
            assertThat(getGeneralSettingsTableValidators("3.1")).hasSize(17)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class, TimeoutMessageValidator.class,
                            LibraryAliasNotInUpperCaseValidator31.class, TemplateSettingUntilRf31Validator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getGeneralSettingsTableValidators("3.2")).hasSize(19)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class, TimeoutMessageValidator.class,
                            LibraryAliasNotInUpperCaseValidator31.class,
                            SingleValuedSettingsHaveMultipleValuesProvidedValidator.class);
        }

        private Stream<VersionDependentModelUnitValidator> getGeneralSettingsTableValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            return validators.getGeneralSettingsTableValidators(mock(SettingTable.class));
        }

    }

    @Nested
    public static class KeywordTableValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder30() {
            assertThat(getKeywordTableValidators("2.9")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion30() {
            assertThat(getKeywordTableValidators("3.0.4")).hasSize(1)
                    .hasOnlyElementsOfTypes(DeprecatedKeywordTableHeaderValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion31() {
            assertThat(getKeywordTableValidators("3.1")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getKeywordTableValidators("3.2")).isEmpty();
        }

        private Stream<VersionDependentModelUnitValidator> getKeywordTableValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            return validators.getKeywordTableValidators(mock(KeywordTable.class));
        }

    }

    @Nested
    public static class TestValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder30() {
            assertThat(getTestCaseValidators("2.9")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion30() {
            assertThat(getTestCaseValidators("3.0.4")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion31() {
            assertThat(getTestCaseValidators("3.1")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getTestCaseValidators("3.2")).hasSize(1)
                    .hasOnlyElementsOfTypes(VariableUsageInTokenValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion33() {
            assertThat(getTestCaseValidators("3.3")).hasSize(1)
                    .hasOnlyElementsOfTypes(VariableUsageInTokenValidator.class);
        }

        private Stream<VersionDependentModelUnitValidator> getTestCaseValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            final TestCase testCase = mock(TestCase.class);
            final TestCaseTable testCaseTable = mock(TestCaseTable.class);
            final RobotFile robotFile = mock(RobotFile.class);
            when(testCase.getParent()).thenReturn(testCaseTable);
            when(testCaseTable.getParent()).thenReturn(robotFile);
            return validators.getTestCaseValidators(testCase);
        }
    }

    @Nested
    public static class TestCaseSettingsValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder30() {
            assertThat(getTestCaseSettingsValidators("2.9")).hasSize(2)
                    .hasOnlyElementsOfTypes(LocalSettingsDuplicationInOldRfValidator.class,
                            TemplateSettingUntilRf31Validator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion30() {
            assertThat(getTestCaseSettingsValidators("3.0.4")).hasSize(9)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class,
                            DeprecatedTestCaseSettingNameValidator.class, TemplateSettingUntilRf31Validator.class,
                            TimeoutMessageValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion31() {
            assertThat(getTestCaseSettingsValidators("3.1")).hasSize(8)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class, TemplateSettingUntilRf31Validator.class,
                            TimeoutMessageValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getTestCaseSettingsValidators("3.2")).hasSize(8)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class,
                            SingleValuedSettingsHaveMultipleValuesProvidedValidator.class);
        }

        private Stream<VersionDependentModelUnitValidator> getTestCaseSettingsValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            final TestCase testCase = mock(TestCase.class);
            final TestCaseTable testCaseTable = mock(TestCaseTable.class);
            final RobotFile robotFile = mock(RobotFile.class);
            when(testCase.getParent()).thenReturn(testCaseTable);
            when(testCaseTable.getParent()).thenReturn(robotFile);
            return validators.getTestCaseSettingsValidators(testCase);
        }

    }

    @Nested
    public static class TaskValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder30() {
            assertThat(getTaskValidators("2.9")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion30() {
            assertThat(getTaskValidators("3.0.4")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion31() {
            assertThat(getTaskValidators("3.1")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getTaskValidators("3.2")).hasSize(1)
                    .hasOnlyElementsOfTypes(VariableUsageInTokenValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion33() {
            assertThat(getTaskValidators("3.3")).hasSize(1).hasOnlyElementsOfTypes(VariableUsageInTokenValidator.class);
        }

        private Stream<VersionDependentModelUnitValidator> getTaskValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            final Task task = mock(Task.class);
            final TaskTable taskTable = mock(TaskTable.class);
            final RobotFile robotFile = mock(RobotFile.class);
            when(task.getParent()).thenReturn(taskTable);
            when(taskTable.getParent()).thenReturn(robotFile);
            return validators.getTaskValidators(task);
        }
    }

    @Nested
    public static class TaskSettingsValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder30() {
            assertThat(getTaskSettingsValidators("2.9")).hasSize(1)
                    .hasOnlyElementsOfTypes(TemplateSettingUntilRf31Validator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion30() {
            assertThat(getTaskSettingsValidators("3.0.4")).hasSize(8)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class,
                            TemplateSettingUntilRf31Validator.class, TimeoutMessageValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion31() {
            assertThat(getTaskSettingsValidators("3.1")).hasSize(8)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class, TemplateSettingUntilRf31Validator.class,
                            TimeoutMessageValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getTaskSettingsValidators("3.2")).hasSize(8)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class,
                            SingleValuedSettingsHaveMultipleValuesProvidedValidator.class);
        }

        private Stream<VersionDependentModelUnitValidator> getTaskSettingsValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            final Task task = mock(Task.class);
            final TaskTable taskTable = mock(TaskTable.class);
            final RobotFile robotFile = mock(RobotFile.class);
            when(task.getParent()).thenReturn(taskTable);
            when(taskTable.getParent()).thenReturn(robotFile);
            return validators.getTaskSettingsValidators(task);
        }

    }

    @Nested
    public static class KeywordSettingsValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder30() {
            assertThat(getKeywordSettingsValidators("2.9")).hasSize(1)
                    .hasOnlyElementsOfTypes(LocalSettingsDuplicationInOldRfValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion30() {
            assertThat(getKeywordSettingsValidators("3.0.4")).hasSize(8)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class,
                            DeprecatedKeywordSettingNameValidator.class, TimeoutMessageValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion31() {
            assertThat(getKeywordSettingsValidators("3.1")).hasSize(7)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class, TimeoutMessageValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getKeywordSettingsValidators("3.2")).hasSize(7)
                    .hasOnlyElementsOfTypes(SettingsDuplicationValidator.class,
                            SingleValuedSettingsHaveMultipleValuesProvidedValidator.class);
        }

        private Stream<VersionDependentModelUnitValidator> getKeywordSettingsValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            final UserKeyword userKeyword = mock(UserKeyword.class);
            final KeywordTable keywordTable = mock(KeywordTable.class);
            final RobotFile robotFile = mock(RobotFile.class);
            when(userKeyword.getParent()).thenReturn(keywordTable);
            when(keywordTable.getParent()).thenReturn(robotFile);
            return validators.getKeywordSettingsValidators(userKeyword);
        }

    }

    @Nested
    public static class TestSuiteFileValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder31() {
            assertThat(getTestSuiteFileValidators("3.0")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion31() {
            assertThat(getTestSuiteFileValidators("3.1")).hasSize(1)
                    .hasOnlyElementsOfTypes(DeprecatedSuiteFileExtensionValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion312() {
            assertThat(getTestSuiteFileValidators("3.1.2")).hasSize(1)
                    .hasOnlyElementsOfTypes(DeprecatedSuiteFileExtensionValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getTestSuiteFileValidators("3.2")).hasSize(2)
                    .hasOnlyElementsOfTypes(UnsupportedSuiteFileExtensionValidator.class,
                            DeprecatedVariableCollectionElementUseValidator.class);
        }

        private Stream<VersionDependentModelUnitValidator> getTestSuiteFileValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            return validators.getFileValidators(null, RobotCasesSection.class);
        }
    }

    @Nested
    public static class TaskSuiteFileValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder31() {
            assertThat(getTaskSuiteFileValidators("3.0")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion31() {
            assertThat(getTaskSuiteFileValidators("3.1")).hasSize(1)
                    .hasOnlyElementsOfTypes(DeprecatedSuiteFileExtensionValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion312() {
            assertThat(getTaskSuiteFileValidators("3.1.2")).hasSize(1)
                    .hasOnlyElementsOfTypes(DeprecatedSuiteFileExtensionValidator.class);
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getTaskSuiteFileValidators("3.2")).hasSize(2)
                    .hasOnlyElementsOfTypes(UnsupportedSuiteFileExtensionValidator.class,
                            DeprecatedVariableCollectionElementUseValidator.class);
        }

        private Stream<VersionDependentModelUnitValidator> getTaskSuiteFileValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            return validators.getFileValidators(null, RobotTasksSection.class);
        }
    }

    @Nested
    public static class ResourceFileValidatorsTest {

        @Test
        public void properValidatorsAreReturnedForVersionUnder32() {
            assertThat(getResourceValidators("3.0")).isEmpty();
        }

        @Test
        public void properValidatorsAreReturnedForVersion32() {
            assertThat(getResourceValidators("3.2")).hasSize(1)
                    .hasOnlyElementsOfTypes(DeprecatedVariableCollectionElementUseValidator.class);
        }

        private Stream<VersionDependentModelUnitValidator> getResourceValidators(final String version) {
            final VersionDependentValidators validators = new VersionDependentValidators(prepareContext(version), null);
            return validators.getResourceValidators(null);
        }
    }

    private static FileValidationContext prepareContext(final String version) {
        final ValidationContext context = new ValidationContext(null, null, RobotVersion.from(version), null, null);
        return new FileValidationContext(context, mock(IFile.class));
    }
}
