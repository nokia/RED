/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.newBuiltInKeyword;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.prepareContext;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.TemplateSetting;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

public class TemplateSettingValidatorTest {

    @Test
    public void validatorIsOnlyApplicableFromRf32() {
        final TemplateSettingValidator validator = new TemplateSettingValidator(null, null, null);

        assertThat(validator.isApplicableFor(new RobotVersion(2, 9, 9))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 0, 5))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 1))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 1, 7))).isFalse();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 2))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 2, 5))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(3, 3))).isTrue();
        assertThat(validator.isApplicableFor(new RobotVersion(4, 1))).isTrue();
    }

    @Test
    public void noProblemsAreReported_whenTemplateHasNoKeyword() {
        final TestTemplate testTemplate = new TestTemplate(RobotToken.create("Test Template"));
        final TaskTemplate taskTemplate = new TaskTemplate(RobotToken.create("Task Template"));
        final LocalSetting<?> localTestTemplate = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                RobotToken.create("Template"));
        final LocalSetting<?> localTaskTemplate = new LocalSetting<>(ModelType.TASK_TEMPLATE,
                RobotToken.create("Template"));

        final List<TemplateSetting> allTemplates = newArrayList(testTemplate, taskTemplate,
                localTestTemplate.adaptTo(TemplateSetting.class), localTaskTemplate.adaptTo(TemplateSetting.class));

        final MockReporter reporter = new MockReporter();
        final TemplateSettingValidator validator = new TemplateSettingValidator(mock(FileValidationContext.class),
                allTemplates, reporter);

        validator.validate();

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void noProblemsAreReported_whenTemplateUsesDefinedKeywords() {
        final TestTemplate testTemplate = new TestTemplate(RobotToken.create("Test Template"));
        testTemplate.setKeywordName("kw1");
        final TaskTemplate taskTemplate = new TaskTemplate(RobotToken.create("Task Template"));
        taskTemplate.setKeywordName("kw2");
        final LocalSetting<?> localTestTemplate = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                RobotToken.create("Template"));
        localTestTemplate.addToken("kw3");
        final LocalSetting<?> localTaskTemplate = new LocalSetting<>(ModelType.TASK_TEMPLATE,
                RobotToken.create("Template"));
        localTaskTemplate.addToken("kw4");

        final List<TemplateSetting> allTemplates = newArrayList(testTemplate, taskTemplate,
                localTestTemplate.adaptTo(TemplateSetting.class), localTaskTemplate.adaptTo(TemplateSetting.class));

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("kw1"), newBuiltInKeyword("kw2"),
                newBuiltInKeyword("kw3"), newBuiltInKeyword("kw4"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final MockReporter reporter = new MockReporter();
        final TemplateSettingValidator validator = new TemplateSettingValidator(context, allTemplates, reporter);

        validator.validate();

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void noProblemsAreReported_whenTemplateUsesNoneInsteadOfKeyword() {
        final TestTemplate testTemplate = new TestTemplate(RobotToken.create("Test Template"));
        testTemplate.setKeywordName("none");
        final TaskTemplate taskTemplate = new TaskTemplate(RobotToken.create("Task Template"));
        taskTemplate.setKeywordName("NONE");
        final LocalSetting<?> localTestTemplate = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                RobotToken.create("Template"));
        localTestTemplate.addToken("NoNe");
        final LocalSetting<?> localTaskTemplate = new LocalSetting<>(ModelType.TASK_TEMPLATE,
                RobotToken.create("Template"));
        localTaskTemplate.addToken("nOnE");

        final List<TemplateSetting> allTemplates = newArrayList(testTemplate, taskTemplate,
                localTestTemplate.adaptTo(TemplateSetting.class), localTaskTemplate.adaptTo(TemplateSetting.class));

        final List<KeywordEntity> accessibleKws = newArrayList(newBuiltInKeyword("kw1"), newBuiltInKeyword("kw2"),
                newBuiltInKeyword("kw3"), newBuiltInKeyword("kw4"));
        final FileValidationContext context = prepareContext(accessibleKws);

        final MockReporter reporter = new MockReporter();
        final TemplateSettingValidator validator = new TemplateSettingValidator(context, allTemplates, reporter);

        validator.validate();

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void noProblemsAreReported_whenThereAreUnexpectedArgumentsInTemplate() {
        final TestTemplate testTemplate = new TestTemplate(RobotToken.create("Test Template"));
        testTemplate.setKeywordName("kw1");
        testTemplate.addUnexpectedTrashArgument("a");
        final TaskTemplate taskTemplate = new TaskTemplate(RobotToken.create("Task Template"));
        taskTemplate.setKeywordName("kw2");
        taskTemplate.addUnexpectedTrashArgument("b");
        final LocalSetting<?> localTestTemplate = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                RobotToken.create("Template"));
        localTestTemplate.addToken("kw3");
        localTestTemplate.addToken("c");
        final LocalSetting<?> localTaskTemplate = new LocalSetting<>(ModelType.TASK_TEMPLATE,
                RobotToken.create("Template"));
        localTaskTemplate.addToken("kw4");
        localTaskTemplate.addToken("d");

        final List<TemplateSetting> allTemplates = newArrayList(testTemplate, taskTemplate,
                localTestTemplate.adaptTo(TemplateSetting.class), localTaskTemplate.adaptTo(TemplateSetting.class));

        final MockReporter reporter = new MockReporter();
        final TemplateSettingValidator validator = new TemplateSettingValidator(prepareContext(), allTemplates,
                reporter);

        validator.validate();

        assertThat(reporter.getReportedProblems()).isEmpty();
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void keywordProblemsAreReported_whenTemplateUsesMissingKeywords() {
        final TestTemplate testTemplate = new TestTemplate(RobotToken.create("Test Template"));
        testTemplate.setKeywordName("kw1");
        final TaskTemplate taskTemplate = new TaskTemplate(RobotToken.create("Task Template"));
        taskTemplate.setKeywordName("kw2");
        final LocalSetting<?> localTestTemplate = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                RobotToken.create("Template"));
        localTestTemplate.addToken("kw3");
        final LocalSetting<?> localTaskTemplate = new LocalSetting<>(ModelType.TASK_TEMPLATE,
                RobotToken.create("Template"));
        localTaskTemplate.addToken("kw4");

        final List<TemplateSetting> allTemplates = newArrayList(testTemplate, taskTemplate,
                localTestTemplate.adaptTo(TemplateSetting.class), localTaskTemplate.adaptTo(TemplateSetting.class));

        final MockReporter reporter = new MockReporter();
        final TemplateSettingValidator validator = new TemplateSettingValidator(prepareContext(), allTemplates,
                reporter);

        validator.validate();

        assertThat(reporter.getReportedProblems()).hasSize(
                4)
                .extracting(Problem::getCause)
                .containsOnly(KeywordsProblem.UNKNOWN_KEYWORD);
        assertThat(reporter.getReportedProblems()).extracting(Problem::getMessage)
                .containsOnly("Unknown keyword 'kw1'", "Unknown keyword 'kw2'", "Unknown keyword 'kw3'",
                        "Unknown keyword 'kw4'");
    }
}
