/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTableModelUpdaterTest {

    private final List<ModelType> keywordModelTypes = newArrayList(ModelType.USER_KEYWORD_EXECUTABLE_ROW,
            ModelType.USER_KEYWORD_SETTING_UNKNOWN, ModelType.USER_KEYWORD_DOCUMENTATION, ModelType.USER_KEYWORD_TAGS,
            ModelType.USER_KEYWORD_TEARDOWN, ModelType.USER_KEYWORD_TIMEOUT, ModelType.USER_KEYWORD_ARGUMENTS,
            ModelType.USER_KEYWORD_RETURN);

    private final List<ModelType> testCaseModelTypes = newArrayList(ModelType.TEST_CASE_EXECUTABLE_ROW,
            ModelType.TEST_CASE_SETTING_UNKNOWN, ModelType.TEST_CASE_DOCUMENTATION, ModelType.TEST_CASE_TAGS,
            ModelType.TEST_CASE_SETUP, ModelType.TEST_CASE_TEARDOWN, ModelType.TEST_CASE_TIMEOUT,
            ModelType.TEST_CASE_TEMPLATE);

    private final TaskTableModelUpdater updater = new TaskTableModelUpdater();

    @Test
    public void testOperationAvailabilityForDifferentTokenTypes() {
        assertThat(updater.getOperationHandler(RobotTokenType.TASK_ACTION_NAME)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TASK_SETTING_DOCUMENTATION)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TASK_SETTING_TAGS_DECLARATION)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TASK_SETTING_TEARDOWN)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TASK_SETTING_TIMEOUT)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TASK_SETTING_SETUP)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TASK_SETTING_TEMPLATE)).isNotNull();
    }

    @Test
    public void testOperationAvailabilityForDifferentModelTypes() {
        assertThat(updater.getOperationHandler(ModelType.TASK_EXECUTABLE_ROW)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TASK_SETTING_UNKNOWN)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TASK_DOCUMENTATION)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TASK_TAGS)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TASK_TEARDOWN)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TASK_TIMEOUT)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TASK_SETUP)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TASK_TEMPLATE)).isNotNull();

        for (final ModelType kwModelType : keywordModelTypes) {
            assertThat(updater.getOperationHandler(kwModelType)).isNotNull();
        }
        for (final ModelType tcModelType : testCaseModelTypes) {
            assertThat(updater.getOperationHandler(tcModelType)).isNotNull();
        }
    }

    @Test
    public void executableRowOperationsTest() {
        final Task task = createTask();

        assertThat(task.getExecutionContext()).isEmpty();

        final AModelElement<?> row = updater.createExecutableRow(task, 0,
                newArrayList("some action", "a", "b", "c", "#comment"));

        assertThat(task.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<Task> addedRow = task.getExecutionContext().get(0);

        assertThat(addedRow).isSameAs(row);
        assertThat(addedRow.getParent()).isSameAs(task);
        assertThat(addedRow.getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);
        assertThat(addedRow.getAction().getText()).isEqualTo("some action");

        assertThat(cellsOf(addedRow)).containsExactly("some action", "a", "b", "c", "#comment");

        addedRow.updateToken(4, "#new comment");
        assertThat(cellsOf(addedRow)).containsExactly("some action", "a", "b", "c", "#new comment");

        addedRow.updateToken(3, "x");
        assertThat(cellsOf(addedRow)).containsExactly("some action", "a", "b", "x", "#new comment");

        addedRow.createToken(4);
        addedRow.createToken(5);
        addedRow.createToken(6);
        addedRow.updateToken(6, "z");
        assertThat(cellsOf(addedRow)).containsExactly("some action", "a", "b", "x", "", "", "z", "#new comment");

        addedRow.deleteToken(5);
        assertThat(cellsOf(addedRow)).containsExactly("some action", "a", "b", "x", "", "z", "#new comment");

        updater.insert(task, 0, addedRow);
        assertThat(task.getExecutionContext()).hasSize(2);
        assertThat(addedRow).isSameAs(task.getExecutionContext().get(0));
     }

    @Test
    public void outOfBoundsExceptionIsThrown_whenTryingToCreateExecutableRowWithMismatchingIndex() {
        final Task task = createTask();
        assertThat(task.getExecutionContext()).isEmpty();

        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(
                () -> updater.createExecutableRow(task, 2, newArrayList("some action", "a", "b", "c", "#comment")));
    }

    @Test
    public void setupSettingOperationsTest() {
        final Task task = createTask();

        assertThat(task.getSetups()).isEmpty();

        final LocalSetting<Task> setting = (LocalSetting<Task>) updater.createSetting(task, 0,
                newArrayList("[Setup]", "a", "b", "c", "#comment"));

        assertThat(task.getSetups()).hasSize(1);
        final LocalSetting<Task> addedSetting = task.getSetups().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(task);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TASK_SETUP);

        assertThat(cellsOf(addedSetting)).containsExactly("[Setup]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(cellsOf(addedSetting)).containsExactly("[Setup]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "kw");
        assertThat(cellsOf(addedSetting)).containsExactly("[Setup]", "kw", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Setup]", "kw", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(cellsOf(addedSetting)).containsExactly("[Setup]", "kw", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(cellsOf(addedSetting)).containsExactly("[Setup]", "kw", "b", "x", "", "z", "#new comment");

        updater.insert(task, 0, addedSetting);
        assertThat(task.getSetups()).hasSize(2);
        assertThat(addedSetting).isSameAs(task.getSetups().get(0));
     }

    @Test
    public void tagsSettingOperationsTest() {
        final Task task = createTask();

        assertThat(task.getTags()).isEmpty();

        final LocalSetting<Task> setting = (LocalSetting<Task>) updater.createSetting(task, 0,
                newArrayList("[Tags]", "a", "b", "c", "#comment"));

        assertThat(task.getTags()).hasSize(1);
        final LocalSetting<Task> addedSetting = task.getTags().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(task);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TASK_TAGS);

        assertThat(cellsOf(addedSetting)).containsExactly("[Tags]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(cellsOf(addedSetting)).containsExactly("[Tags]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Tags]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Tags]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(cellsOf(addedSetting)).containsExactly("[Tags]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(cellsOf(addedSetting)).containsExactly("[Tags]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(task, 0, addedSetting);
        assertThat(task.getTags()).hasSize(2);
        assertThat(addedSetting).isSameAs(task.getTags().get(0));
    }

    @Test
    public void timeoutSettingOperationsTest() {
        final Task task = createTask();

        assertThat(task.getTimeouts()).isEmpty();

        final LocalSetting<Task> setting = (LocalSetting<Task>) updater.createSetting(task, 0,
                newArrayList("[Timeout]", "a", "b", "c", "#comment"));

        assertThat(task.getTimeouts()).hasSize(1);
        final LocalSetting<Task> addedSetting = task.getTimeouts().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(task);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TASK_TIMEOUT);

        assertThat(cellsOf(addedSetting)).containsExactly("[Timeout]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(cellsOf(addedSetting)).containsExactly("[Timeout]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Timeout]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Timeout]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(cellsOf(addedSetting)).containsExactly("[Timeout]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(cellsOf(addedSetting)).containsExactly("[Timeout]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(task, 0, addedSetting);
        assertThat(task.getTimeouts()).hasSize(2);
        assertThat(addedSetting).isSameAs(task.getTimeouts().get(0));
    }

    @Test
    public void teardownsSettingOperationsTest() {
        final Task task = createTask();

        assertThat(task.getTeardowns()).isEmpty();

        final LocalSetting<Task> setting = (LocalSetting<Task>) updater.createSetting(task, 0,
                newArrayList("[Teardown]", "a", "b", "c", "#comment"));

        assertThat(task.getTeardowns()).hasSize(1);
        final LocalSetting<Task> addedSetting = task.getTeardowns().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(task);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TASK_TEARDOWN);

        assertThat(cellsOf(addedSetting)).containsExactly("[Teardown]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(cellsOf(addedSetting)).containsExactly("[Teardown]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Teardown]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Teardown]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(cellsOf(addedSetting)).containsExactly("[Teardown]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(cellsOf(addedSetting)).containsExactly("[Teardown]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(task, 0, addedSetting);
        assertThat(task.getTeardowns()).hasSize(2);
        assertThat(addedSetting).isSameAs(task.getTeardowns().get(0));
    }

    @Test
    public void templateSettingOperationsTest() {
        final Task task = createTask();

        assertThat(task.getTeardowns()).isEmpty();

        final LocalSetting<Task> setting = (LocalSetting<Task>) updater.createSetting(task, 0,
                newArrayList("[Template]", "a", "b", "c", "#comment"));

        assertThat(task.getTemplates()).hasSize(1);
        final LocalSetting<Task> addedSetting = task.getTemplates().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(task);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TASK_TEMPLATE);

        assertThat(cellsOf(addedSetting)).containsExactly("[Template]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(cellsOf(addedSetting)).containsExactly("[Template]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Template]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Template]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(cellsOf(addedSetting)).containsExactly("[Template]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(cellsOf(addedSetting)).containsExactly("[Template]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(task, 0, addedSetting);
        assertThat(task.getTemplates()).hasSize(2);
        assertThat(addedSetting).isSameAs(task.getTemplates().get(0));
    }

    @Test
    public void unknownSettingOperationsTest() {
        final Task task = createTask();

        assertThat(task.getTeardowns()).isEmpty();

        final LocalSetting<Task> setting = (LocalSetting<Task>) updater.createSetting(task, 0,
                newArrayList("[unknown]", "a", "b", "c", "#comment"));

        assertThat(task.getUnknownSettings()).hasSize(1);
        final LocalSetting<Task> addedSetting = task.getUnknownSettings().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(task);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TASK_SETTING_UNKNOWN);

        assertThat(cellsOf(addedSetting)).containsExactly("[unknown]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(cellsOf(addedSetting)).containsExactly("[unknown]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[unknown]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[unknown]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(cellsOf(addedSetting)).containsExactly("[unknown]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(cellsOf(addedSetting)).containsExactly("[unknown]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(task, 0, addedSetting);
        assertThat(task.getUnknownSettings()).hasSize(2);
        assertThat(addedSetting).isSameAs(task.getUnknownSettings().get(0));
    }

    @Test
    public void documentationSettingOperationsTest() {
        final Task task = createTask();

        assertThat(task.getDocumentation()).isEmpty();

        final LocalSetting<Task> setting = (LocalSetting<Task>) updater.createSetting(task, 0,
                newArrayList("[Documentation]", "a", "b", "c", "#comment"));

        assertThat(task.getDocumentation()).hasSize(1);
        final LocalSetting<Task> addedSetting = task.getDocumentation().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(task);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TASK_DOCUMENTATION);

        assertThat(cellsOf(addedSetting)).containsExactly("[Documentation]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(cellsOf(addedSetting)).containsExactly("[Documentation]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(cellsOf(addedSetting)).containsExactly("[Documentation]", "x", "b", "c", "#new comment");

        addedSetting.createToken(2);
        addedSetting.updateToken(2, "y");
        assertThat(cellsOf(addedSetting)).containsExactly("[Documentation]", "x", "y", "b", "c", "#new comment");

        addedSetting.deleteToken(1);
        assertThat(cellsOf(addedSetting)).containsExactly("[Documentation]", "y", "b", "c", "#new comment");

        addedSetting.deleteToken(2);
        assertThat(cellsOf(addedSetting)).containsExactly("[Documentation]", "y", "c", "#new comment");

        updater.insert(task, 0, addedSetting);
        assertThat(task.getDocumentation()).hasSize(2);
        assertThat(addedSetting).isSameAs(task.getDocumentation().get(0));
    }

    @Test
    public void keywordExecutableRowIsProperlyMorphedIntoExecutableRow_whenInserted() {
        final RobotExecutableRow<UserKeyword> kwExecutionRow = new RobotExecutableRow<>();
        kwExecutionRow.setAction(RobotToken.create("action"));
        kwExecutionRow.addArgument(RobotToken.create("a"));
        kwExecutionRow.addArgument(RobotToken.create("b"));
        kwExecutionRow.setComment("comment");
        final UserKeyword keyword = createKeyword();
        keyword.addElement(kwExecutionRow);

        final Task task = createTask();

        assertThat(task.getExecutionContext()).isEmpty();
        updater.insert(task, 0, kwExecutionRow);

        assertThat(task.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<Task> row = task.getExecutionContext().get(0);

        assertThat(row.getParent()).isSameAs(task);
        assertThat(row.getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);

        assertThat(cellsOf(row)).containsExactly("action", "a", "b", "#comment");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.TASK_ACTION_NAME, RobotTokenType.TASK_ACTION_ARGUMENT,
                RobotTokenType.TASK_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordArgumentsSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Arguments]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getUnknownSettings()).isEmpty();
        updater.insert(task, 0, keywordSetting);

        assertThat(task.getUnknownSettings()).hasSize(1);
        final LocalSetting<Task> setting = task.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_SETTING_UNKNOWN);

        assertThat(cellsOf(setting)).containsExactly("[Arguments]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION,
                RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordReturnSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Return]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getUnknownSettings()).isEmpty();
        updater.insert(task, 0, keywordSetting);

        assertThat(task.getUnknownSettings()).hasSize(1);
        final LocalSetting<Task> setting = task.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_SETTING_UNKNOWN);

        assertThat(cellsOf(setting)).containsExactly("[Return]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION,
                RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordTagsSettingIsProperlyMorphedIntoTagsSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Tags]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getTags()).isEmpty();
        updater.insert(task, 0, keywordSetting);

        assertThat(task.getTags()).hasSize(1);
        final LocalSetting<Task> setting = task.getTags().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_TAGS);

        assertThat(cellsOf(setting)).containsExactly("[Tags]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_TAGS_DECLARATION,
                RobotTokenType.TASK_SETTING_TAGS, RobotTokenType.TASK_SETTING_TAGS, RobotTokenType.TASK_SETTING_TAGS,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordTeardownSettingIsProperlyMorphedIntoTeardownSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Teardown]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getTeardowns()).isEmpty();
        updater.insert(task, 0, keywordSetting);

        assertThat(task.getTeardowns()).hasSize(1);
        final LocalSetting<Task> setting = task.getTeardowns().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_TEARDOWN);

        assertThat(cellsOf(setting)).containsExactly("[Teardown]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_TEARDOWN,
                RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_NAME,
                RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_ARGUMENT,
                RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordTimeoutSettingIsProperlyMorphedIntoTimeoutSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Timeout]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getTimeouts()).isEmpty();
        updater.insert(task, 0, keywordSetting);

        assertThat(task.getTimeouts()).hasSize(1);
        final LocalSetting<Task> setting = task.getTimeouts().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_TIMEOUT);

        assertThat(cellsOf(setting)).containsExactly("[Timeout]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_TIMEOUT,
                RobotTokenType.TASK_SETTING_TIMEOUT_VALUE, RobotTokenType.TASK_SETTING_TIMEOUT_MESSAGE,
                RobotTokenType.TASK_SETTING_TIMEOUT_MESSAGE, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordDocumentationSettingIsProperlyMorphedIntoDocumentationSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Documentation]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getDocumentation()).isEmpty();
        updater.insert(task, 0, keywordSetting);

        assertThat(task.getDocumentation()).hasSize(1);
        final LocalSetting<Task> setting = task.getDocumentation().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_DOCUMENTATION);

        assertThat(cellsOf(setting)).containsExactly("[Documentation]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_DOCUMENTATION,
                RobotTokenType.TASK_SETTING_DOCUMENTATION_TEXT, RobotTokenType.TASK_SETTING_DOCUMENTATION_TEXT,
                RobotTokenType.TASK_SETTING_DOCUMENTATION_TEXT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordUnknownSetupSettingIsProperlyMorphedIntoSetupSetting_whenInserted() {
        final LocalSetting<UserKeyword> keywordSetting = new LocalSetting<>(ModelType.USER_KEYWORD_SETTING_UNKNOWN,
                RobotToken.create("[Setup]"));
        keywordSetting.addToken("a");
        keywordSetting.addToken("b");
        keywordSetting.addToken("c");
        keywordSetting.setComment("comment");

        final Task task = createTask();

        assertThat(task.getSetups()).isEmpty();
        updater.insert(task, 0, keywordSetting);

        assertThat(task.getSetups()).hasSize(1);
        final LocalSetting<Task> setting = task.getSetups().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_SETUP);

        assertThat(cellsOf(setting)).containsExactly("[Setup]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_SETUP,
                RobotTokenType.TASK_SETTING_SETUP_KEYWORD_NAME, RobotTokenType.TASK_SETTING_SETUP_KEYWORD_ARGUMENT,
                RobotTokenType.TASK_SETTING_SETUP_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordUnknownTemplateSettingIsProperlyMorphedIntoTemplateSetting_whenInserted() {
        final LocalSetting<UserKeyword> keywordSetting = new LocalSetting<>(ModelType.USER_KEYWORD_SETTING_UNKNOWN,
                RobotToken.create("[Template]"));
        keywordSetting.addToken("a");
        keywordSetting.addToken("b");
        keywordSetting.addToken("c");
        keywordSetting.setComment("comment");

        final Task task = createTask();

        assertThat(task.getTemplates()).isEmpty();
        updater.insert(task, 0, keywordSetting);

        assertThat(task.getTemplates()).hasSize(1);
        final LocalSetting<Task> setting = task.getTemplates().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_TEMPLATE);

        assertThat(cellsOf(setting)).containsExactly("[Template]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_TEMPLATE,
                RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_NAME,
                RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT,
                RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordUnknownTemplateSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<UserKeyword> keywordSetting = new LocalSetting<>(ModelType.USER_KEYWORD_SETTING_UNKNOWN,
                RobotToken.create("[something]"));
        keywordSetting.addToken("a");
        keywordSetting.addToken("b");
        keywordSetting.addToken("c");
        keywordSetting.setComment("comment");

        final Task task = createTask();

        assertThat(task.getUnknownSettings()).isEmpty();
        updater.insert(task, 0, keywordSetting);

        assertThat(task.getUnknownSettings()).hasSize(1);
        final LocalSetting<Task> setting = task.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_SETTING_UNKNOWN);

        assertThat(cellsOf(setting)).containsExactly("[something]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION,
                RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseExecutableRowIsProperlyMorphedIntoExecutableRow_whenInserted() {
        final RobotExecutableRow<TestCase> tcExecutionRow = new RobotExecutableRow<>();
        tcExecutionRow.setAction(RobotToken.create("action"));
        tcExecutionRow.addArgument(RobotToken.create("a"));
        tcExecutionRow.addArgument(RobotToken.create("b"));
        tcExecutionRow.setComment("comment");
        final TestCase testCase = createTestCase();
        testCase.addElement(tcExecutionRow);

        final Task task = createTask();

        assertThat(task.getExecutionContext()).isEmpty();
        updater.insert(task, 0, tcExecutionRow);

        assertThat(task.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<Task> row = task.getExecutionContext().get(0);

        assertThat(row.getParent()).isSameAs(task);
        assertThat(row.getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);

        assertThat(cellsOf(row)).containsExactly("action", "a", "b", "#comment");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.TASK_ACTION_NAME, RobotTokenType.TASK_ACTION_ARGUMENT,
                RobotTokenType.TASK_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTemplateArgumentsSettingIsProperlyMorphedIntoTemplateSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new TestCaseTableModelUpdater()
                .createSetting(createTestCase(), 0, newArrayList("[Template]", "a", "b", "#comment"));

        final Task task = createTask();

        assertThat(task.getTemplates()).isEmpty();
        updater.insert(task, 0, tcSetting);

        assertThat(task.getTemplates()).hasSize(1);
        final LocalSetting<Task> setting = task.getTemplates().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_TEMPLATE);

        assertThat(cellsOf(setting)).containsExactly("[Template]", "a", "b", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_TEMPLATE,
                RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_NAME,
                RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTagsSettingIsProperlyMorphedIntoTagsSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new TestCaseTableModelUpdater()
                .createSetting(createTestCase(), 0, newArrayList("[Tags]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getTags()).isEmpty();
        updater.insert(task, 0, tcSetting);

        assertThat(task.getTags()).hasSize(1);
        final LocalSetting<Task> setting = task.getTags().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_TAGS);

        assertThat(cellsOf(setting)).containsExactly("[Tags]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_TAGS_DECLARATION,
                RobotTokenType.TASK_SETTING_TAGS, RobotTokenType.TASK_SETTING_TAGS, RobotTokenType.TASK_SETTING_TAGS,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseSetupSettingIsProperlyMorphedIntoSetupSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new TestCaseTableModelUpdater()
                .createSetting(createTestCase(), 0, newArrayList("[Setup]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getSetups()).isEmpty();
        updater.insert(task, 0, tcSetting);

        assertThat(task.getSetups()).hasSize(1);
        final LocalSetting<Task> setting = task.getSetups().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_SETUP);

        assertThat(cellsOf(setting)).containsExactly("[Setup]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_SETUP,
                RobotTokenType.TASK_SETTING_SETUP_KEYWORD_NAME, RobotTokenType.TASK_SETTING_SETUP_KEYWORD_ARGUMENT,
                RobotTokenType.TASK_SETTING_SETUP_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTeardownSettingIsProperlyMorphedIntoTeardownSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new TestCaseTableModelUpdater()
                .createSetting(createTestCase(), 0, newArrayList("[Teardown]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getTeardowns()).isEmpty();
        updater.insert(task, 0, tcSetting);

        assertThat(task.getTeardowns()).hasSize(1);
        final LocalSetting<Task> setting = task.getTeardowns().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_TEARDOWN);

        assertThat(cellsOf(setting)).containsExactly("[Teardown]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_TEARDOWN,
                RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_NAME,
                RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_ARGUMENT,
                RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTimeoutSettingIsProperlyMorphedIntoTimeoutSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new TestCaseTableModelUpdater()
                .createSetting(createTestCase(), 0, newArrayList("[Timeout]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getTimeouts()).isEmpty();
        updater.insert(task, 0, tcSetting);

        assertThat(task.getTimeouts()).hasSize(1);
        final LocalSetting<Task> setting = task.getTimeouts().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_TIMEOUT);

        assertThat(cellsOf(setting)).containsExactly("[Timeout]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_TIMEOUT,
                RobotTokenType.TASK_SETTING_TIMEOUT_VALUE, RobotTokenType.TASK_SETTING_TIMEOUT_MESSAGE,
                RobotTokenType.TASK_SETTING_TIMEOUT_MESSAGE, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseDocumentationSettingIsProperlyMorphedIntoDocumentationSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new TestCaseTableModelUpdater()
                .createSetting(createTestCase(), 0, newArrayList("[Documentation]", "a", "b", "c", "#comment"));

        final Task task = createTask();

        assertThat(task.getDocumentation()).isEmpty();
        updater.insert(task, 0, tcSetting);

        assertThat(task.getDocumentation()).hasSize(1);
        final LocalSetting<Task> setting = task.getDocumentation().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_DOCUMENTATION);

        assertThat(cellsOf(setting)).containsExactly("[Documentation]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_DOCUMENTATION,
                RobotTokenType.TASK_SETTING_DOCUMENTATION_TEXT, RobotTokenType.TASK_SETTING_DOCUMENTATION_TEXT,
                RobotTokenType.TASK_SETTING_DOCUMENTATION_TEXT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseUnknownSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new LocalSetting<>(ModelType.TEST_CASE_SETTING_UNKNOWN,
                RobotToken.create("[something]"));
        tcSetting.addToken("a");
        tcSetting.addToken("b");
        tcSetting.addToken("c");
        tcSetting.setComment("comment");

        final Task task = createTask();

        assertThat(task.getUnknownSettings()).isEmpty();
        updater.insert(task, 0, tcSetting);

        assertThat(task.getUnknownSettings()).hasSize(1);
        final LocalSetting<Task> setting = task.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(task);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TASK_SETTING_UNKNOWN);

        assertThat(cellsOf(setting)).containsExactly("[something]", "a", "b", "c", "#comment");
        assertThat(typesOf(setting)).containsExactly(RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION,
                RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void executableRowWithTemplateCreationTest() {
        final Task task = createTask();
        final LocalSetting<Task> template = task.newTemplate(0);
        template.addToken("Some Kw");

        assertThat(task.getExecutionContext()).isEmpty();

        final AModelElement<?> row = updater.createExecutableRow(task, 1,
                newArrayList("a1", "a2", "a3", "a4", "#comment"));

        assertThat(task.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<Task> addedRow = task.getExecutionContext().get(0);

        assertThat(addedRow).isSameAs(row);
        assertThat(addedRow.getParent()).isSameAs(task);
        assertThat(addedRow.getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);

        assertThat(addedRow.getElementTokens())
                .filteredOn(token -> token.getTypes().contains(RobotTokenType.TASK_TEMPLATE_ARGUMENT))
                .extracting(RobotToken::getText)
                .containsExactly("a1", "a2", "a3", "a4");

    }

    @Test
    public void testInsertingExecutableRowFromTaskWithoutTemplateToTaskWithTemplate() {
        final Task task = createTask();
        final LocalSetting<Task> template = task.newTemplate(0);
        template.addToken("Some Kw");

        assertThat(task.getExecutionContext()).isEmpty();

        final RobotExecutableRow<Task> rowToInsert = new RobotExecutableRow<>();
        rowToInsert.setAction(RobotToken.create("Kw Call", RobotTokenType.TASK_ACTION_NAME));
        rowToInsert.setArgument(0, RobotToken.create("a1"));
        rowToInsert.setArgument(1, RobotToken.create("a2"));
        rowToInsert.setArgument(2, RobotToken.create("a3"));
        final AModelElement<?> insertedRow = updater.insert(task, 1, rowToInsert);

        assertThat(task.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<Task> addedRow = task.getExecutionContext().get(0);

        assertThat(addedRow).isSameAs(insertedRow);
        assertThat(addedRow.getParent()).isSameAs(task);
        assertThat(addedRow.getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);

        assertThat(addedRow.getElementTokens())
                .filteredOn(token -> token.getTypes().contains(RobotTokenType.TASK_TEMPLATE_ARGUMENT))
                .extracting(RobotToken::getText)
                .containsExactly("Kw Call", "a1", "a2", "a3");

    }

    @Test
    public void testInsertingExecutableRowFromTestWithTemplateToTestWithoutTemplate() {
        final Task task = createTask();

        assertThat(task.getExecutionContext()).isEmpty();

        final RobotExecutableRow<TestCase> rowToInsert = new RobotExecutableRow<>();
        rowToInsert.setAction(
                RobotToken.create("Kw Call", RobotTokenType.TASK_ACTION_NAME, RobotTokenType.TASK_TEMPLATE_ARGUMENT));
        rowToInsert.setArgument(0,
                RobotToken.create("a1", RobotTokenType.TASK_ACTION_ARGUMENT, RobotTokenType.TASK_TEMPLATE_ARGUMENT));
        rowToInsert.setArgument(1,
                RobotToken.create("a2", RobotTokenType.TASK_ACTION_ARGUMENT, RobotTokenType.TASK_TEMPLATE_ARGUMENT));
        rowToInsert.setArgument(2,
                RobotToken.create("a3", RobotTokenType.TASK_ACTION_ARGUMENT, RobotTokenType.TASK_TEMPLATE_ARGUMENT));
        final AModelElement<?> insertedRow = updater.insert(task, 0, rowToInsert);

        assertThat(task.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<Task> addedRow = task.getExecutionContext().get(0);

        assertThat(addedRow).isSameAs(insertedRow);
        assertThat(addedRow.getParent()).isSameAs(task);
        assertThat(addedRow.getModelType()).isEqualTo(ModelType.TASK_EXECUTABLE_ROW);

        assertThat(addedRow.getElementTokens())
                .filteredOn(token -> token.getTypes().contains(RobotTokenType.TASK_TEMPLATE_ARGUMENT))
                .isEmpty();

    }

    private static Task createTask() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(RobotVersion.from("3.1.0"));
        final RobotFile parent = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(parent);

        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);
        table.addTask(task);
        return task;
    }

    private static TestCase createTestCase() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(RobotVersion.from("3.0.0"));
        final RobotFile parent = new RobotFile(parentFileOutput);
        final TestCaseTable table = new TestCaseTable(parent);

        final TestCase testCase = new TestCase(RobotToken.create("case"));
        testCase.setParent(table);
        table.addTest(testCase);
        return testCase;
    }

    private static UserKeyword createKeyword() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(RobotVersion.from("3.0.0"));
        final RobotFile parent = new RobotFile(parentFileOutput);
        final KeywordTable table = new KeywordTable(parent);

        final UserKeyword keyword = new UserKeyword(RobotToken.create("kw"));
        keyword.setParent(table);
        table.addKeyword(keyword);
        return keyword;
    }

    private static List<String> cellsOf(final AModelElement<?> element) {
        return element.getElementTokens().stream().map(RobotToken::getText).collect(toList());
    }

    private static List<IRobotTokenType> typesOf(final AModelElement<?> element) {
        return element.getElementTokens().stream().map(t -> t.getTypes().get(0)).collect(toList());
    }
}
