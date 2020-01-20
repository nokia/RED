/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.setting.TaskTimeout;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

public class TimeoutMessageValidatorTest {

    @Test
    public void validatorIsOnlyApplicableToRf301Till31() {
        final TimeoutMessageValidator<?> validator = new TimeoutMessageValidator<>(null, null, null, null);

        assertThat(validator.isApplicableFor(new RobotVersion(2, 9, 9))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0, 1))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0, 5))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 1, 7))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 2))).isFalse();
    }

    @Test
    public void noProblemsAreReportedWhenElementsDoNotHaveAnyArgumentsGiven() throws CoreException {
        final LocalSetting<?> s1 = new LocalSetting<>(ModelType.TEST_CASE_TIMEOUT, RobotToken.create("[Timeout]"));
        final LocalSetting<?> s2 = new LocalSetting<>(ModelType.TASK_TIMEOUT, RobotToken.create("[Timeout]"));

        final MockReporter reporter = new MockReporter();
        final TimeoutMessageValidator<?> validator = new TimeoutMessageValidator<>(mock(IFile.class), () -> newArrayList(s1, s2),
                el -> new ArrayList<>(), reporter);
        validator.validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void problemsIsReportedWhenElementsHaveArgumentsGiven() throws CoreException {
        final TestTimeout s1 = new TestTimeout(RobotToken.create("Test Timeout"));
        final TaskTimeout s2 = new TaskTimeout(RobotToken.create("Task Timeout"));

        final MockReporter reporter = new MockReporter();
        final TimeoutMessageValidator<?> validator = new TimeoutMessageValidator<>(mock(IFile.class),
                () -> newArrayList(s1, s2), el -> newArrayList(RobotToken.create("arg")), reporter);
        validator.validate();

        assertThat(reporter.getReportedProblems()).hasSize(2)
                .extracting(Problem::getCause)
                .containsOnly(GeneralSettingsProblem.TIMEOUT_MESSAGE_DEPRECATED);
    }
}
