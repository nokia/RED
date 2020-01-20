/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.core.resources.IFile;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

public class SingleValuedSettingsHaveMultipleValuesProvidedValidatorTest {

    @Test
    public void validatorIsOnlyApplicableSinceRf32() {
        final SingleValuedSettingsHaveMultipleValuesProvidedValidator<?> validator = new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(
                null, null, null, null);

        assertThat(validator.isApplicableFor(new RobotVersion(2, 9, 9))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0, 5))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 1))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 1, 7))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 2))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 3))).isTrue();
    }

    @Test
    public void noProblemsAreReported_whenSettingHasOnlySingleValueOrNone() {
        final ResourceImport generalSetting = new ResourceImport(RobotToken.create("Resource"));

        final LocalSetting<?> tcSetting = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                RobotToken.create("Template"));
        tcSetting.addToken("kw");
        tcSetting.addCommentPart("# comment");

        final LocalSetting<?> taskSetting = new LocalSetting<>(ModelType.TASK_TIMEOUT, RobotToken.create("Timeout"));

        final LocalSetting<?> kwSetting = new LocalSetting<>(ModelType.USER_KEYWORD_TIMEOUT,
                RobotToken.create("Timeout"));
        kwSetting.addToken("2");

        final MockReporter reporter = new MockReporter();
        final SingleValuedSettingsHaveMultipleValuesProvidedValidator<?> validator = new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(
                mock(IFile.class), () -> newArrayList(generalSetting, tcSetting, taskSetting, kwSetting), reporter,
                ". Detailed explanation");

        validator.validate();

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void problemsAreReported_whenSettingsHaveTooManyValues() {
        final ResourceImport generalSetting = new ResourceImport(RobotToken.create("Resource"));
        generalSetting.setPathOrName("path");
        generalSetting.addUnexpectedTrashArgument("arg1");
        generalSetting.addUnexpectedTrashArgument("arg2");

        final LocalSetting<?> tcSetting = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                RobotToken.create("Template"));
        tcSetting.addToken("kw");
        tcSetting.addToken("a");
        tcSetting.addToken("b");

        final LocalSetting<?> taskSetting = new LocalSetting<>(ModelType.TASK_TIMEOUT, RobotToken.create("Timeout"));
        taskSetting.addToken("2");
        taskSetting.addToken("c");
        taskSetting.addToken("d");

        final LocalSetting<?> kwSetting = new LocalSetting<>(ModelType.USER_KEYWORD_TIMEOUT,
                RobotToken.create("Timeout"));
        kwSetting.addToken("2");
        kwSetting.addToken("e");
        kwSetting.addToken("f");

        final MockReporter reporter = new MockReporter();
        final SingleValuedSettingsHaveMultipleValuesProvidedValidator<?> validator = new SingleValuedSettingsHaveMultipleValuesProvidedValidator<>(
                mock(IFile.class), () -> newArrayList(generalSetting, tcSetting, taskSetting, kwSetting), reporter,
                ". Detailed explanation");

        validator.validate();

        assertThat(reporter.getReportedProblems()).hasSize(4)
                .extracting(Problem::getCause)
                .containsOnly(GeneralSettingsProblem.INVALID_NUMBER_OF_SETTING_VALUES);
    }

}
