/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTableModelUpdaterTest {

    private final List<ModelType> keywordModelTypes = newArrayList(ModelType.USER_KEYWORD_EXECUTABLE_ROW,
            ModelType.USER_KEYWORD_SETTING_UNKNOWN, ModelType.USER_KEYWORD_DOCUMENTATION, ModelType.USER_KEYWORD_TAGS,
            ModelType.USER_KEYWORD_TEARDOWN, ModelType.USER_KEYWORD_TIMEOUT, ModelType.USER_KEYWORD_ARGUMENTS,
            ModelType.USER_KEYWORD_RETURN);

    private final TestCaseTableModelUpdater updater = new TestCaseTableModelUpdater();

    @Test
    public void testOperationAvailabilityForDifferentTokenTypes() {
        assertThat(updater.getOperationHandler(RobotTokenType.TEST_CASE_ACTION_NAME)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TEST_CASE_SETTING_TEARDOWN)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TEST_CASE_SETTING_TIMEOUT)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TEST_CASE_SETTING_SETUP)).isNotNull();
        assertThat(updater.getOperationHandler(RobotTokenType.TEST_CASE_SETTING_TEMPLATE)).isNotNull();
    }

    @Test
    public void testOperationAvailabilityForDifferentModelTypes() {
        assertThat(updater.getOperationHandler(ModelType.TEST_CASE_EXECUTABLE_ROW)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TEST_CASE_SETTING_UNKNOWN)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TEST_CASE_DOCUMENTATION)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TEST_CASE_TAGS)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TEST_CASE_TEARDOWN)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TEST_CASE_TIMEOUT)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TEST_CASE_SETUP)).isNotNull();
        assertThat(updater.getOperationHandler(ModelType.TEST_CASE_TEMPLATE)).isNotNull();

        for (final ModelType kwModelType : keywordModelTypes) {
            assertThat(updater.getOperationHandler(kwModelType)).isNotNull();
        }
    }

    @Test
    public void executableRowOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getExecutionContext()).isEmpty();

        final AModelElement<?> row = updater.createExecutableRow(testCase, 0,
                newArrayList("some action", "a", "b", "c", "#comment"));

        assertThat(testCase.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<TestCase> addedRow = testCase.getExecutionContext().get(0);

        assertThat(addedRow).isSameAs(row);
        assertThat(addedRow.getParent()).isSameAs(testCase);
        assertThat(addedRow.getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertThat(addedRow.getAction().getText()).isEqualTo("some action");

        assertThat(addedRow.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("some action", "a", "b", "c", "#comment");

        addedRow.updateToken(4, "#new comment");
        assertThat(addedRow.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("some action", "a", "b", "c", "#new comment");

        addedRow.updateToken(3, "x");
        assertThat(addedRow.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("some action", "a", "b", "x", "#new comment");

        addedRow.createToken(4);
        addedRow.createToken(5);
        addedRow.createToken(6);
        addedRow.updateToken(6, "z");
        assertThat(addedRow.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("some action", "a", "b", "x", "", "", "z", "#new comment");

        addedRow.deleteToken(5);
        assertThat(addedRow.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("some action", "a", "b", "x", "", "z", "#new comment");

        updater.insert(testCase, 0, addedRow);
        assertThat(testCase.getExecutionContext()).hasSize(2);
        assertThat(addedRow).isSameAs(testCase.getExecutionContext().get(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfBoundsExceptionIsThrown_whenTryingToCreateExecutableRowWithMismatchingIndex() {
        final TestCase testCase = createCase();
        assertThat(testCase.getExecutionContext()).isEmpty();

        updater.createExecutableRow(testCase, 2, newArrayList("some action", "a", "b", "c", "#comment"));
    }

    @Test
    public void setupSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getSetups()).isEmpty();

        final LocalSetting<TestCase> setting = updater.createSetting(testCase, 0,
                newArrayList("[Setup]", "a", "b", "c", "#comment"));

        assertThat(testCase.getSetups()).hasSize(1);
        final LocalSetting<TestCase> addedSetting = testCase.getSetups().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETUP);

        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Setup]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Setup]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "kw");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Setup]", "kw", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Setup]", "kw", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Setup]", "kw", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Setup]", "kw", "b", "x", "", "z", "#new comment");

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getSetups()).hasSize(2);
        assertThat(addedSetting).isSameAs(testCase.getSetups().get(0));
    }

    @Test
    public void tagsSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTags()).isEmpty();

        final LocalSetting<TestCase> setting = updater.createSetting(testCase, 0,
                newArrayList("[Tags]", "a", "b", "c", "#comment"));

        assertThat(testCase.getTags()).hasSize(1);
        final LocalSetting<TestCase> addedSetting = testCase.getTags().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_TAGS);

        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Tags]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Tags]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Tags]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Tags]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Tags]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Tags]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getTags()).hasSize(2);
        assertThat(addedSetting).isSameAs(testCase.getTags().get(0));
    }

    @Test
    public void tagsTimeoutOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTimeouts()).isEmpty();

        final LocalSetting<TestCase> setting = updater.createSetting(testCase, 0,
                newArrayList("[Timeout]", "a", "b", "c", "#comment"));

        assertThat(testCase.getTimeouts()).hasSize(1);
        final LocalSetting<TestCase> addedSetting = testCase.getTimeouts().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_TIMEOUT);

        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getTimeouts()).hasSize(2);
        assertThat(addedSetting).isSameAs(testCase.getTimeouts().get(0));
    }

    @Test
    public void teardownsSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTeardowns()).isEmpty();

        final LocalSetting<TestCase> setting = updater.createSetting(testCase, 0,
                newArrayList("[Teardown]", "a", "b", "c", "#comment"));

        assertThat(testCase.getTeardowns()).hasSize(1);
        final LocalSetting<TestCase> addedSetting = testCase.getTeardowns().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_TEARDOWN);

        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getTeardowns()).hasSize(2);
        assertThat(addedSetting).isSameAs(testCase.getTeardowns().get(0));
    }

    @Test
    public void templateSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTeardowns()).isEmpty();

        final LocalSetting<TestCase> setting = updater.createSetting(testCase, 0,
                newArrayList("[Template]", "a", "b", "c", "#comment"));

        assertThat(testCase.getTemplates()).hasSize(1);
        final LocalSetting<TestCase> addedSetting = testCase.getTemplates().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_TEMPLATE);

        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Template]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Template]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Template]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Template]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Template]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Template]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getTemplates()).hasSize(2);
        assertThat(addedSetting).isSameAs(testCase.getTemplates().get(0));
    }

    @Test
    public void unknownSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTeardowns()).isEmpty();

        final LocalSetting<TestCase> setting = updater.createSetting(testCase, 0,
                newArrayList("[unknown]", "a", "b", "c", "#comment"));

        assertThat(testCase.getUnknownSettings()).hasSize(1);
        final LocalSetting<TestCase> addedSetting = testCase.getUnknownSettings().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETTING_UNKNOWN);

        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[unknown]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[unknown]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[unknown]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[unknown]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[unknown]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[unknown]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getUnknownSettings()).hasSize(2);
        assertThat(addedSetting).isSameAs(testCase.getUnknownSettings().get(0));
    }

    @Test
    public void documentationSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getDocumentation()).isEmpty();

        final LocalSetting<TestCase> setting = updater.createSetting(testCase, 0,
                newArrayList("[Documentation]", "a", "b", "c", "#comment"));

        assertThat(testCase.getDocumentation()).hasSize(1);
        final LocalSetting<TestCase> addedSetting = testCase.getDocumentation().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_DOCUMENTATION);

        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "a", "b", "c", "#comment");

        addedSetting.updateToken(4, "#new comment");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "a", "b", "c", "#new comment");

        addedSetting.updateToken(1, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "x", "b", "c", "#new comment");

        addedSetting.updateToken(3, "x");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "x", "b", "x", "#new comment");

        addedSetting.createToken(4);
        addedSetting.createToken(5);
        addedSetting.createToken(6);
        addedSetting.updateToken(6, "z");
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "x", "b", "x", "", "", "z", "#new comment");

        addedSetting.deleteToken(5);
        assertThat(addedSetting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "x", "b", "x", "", "z", "#new comment");

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getDocumentation()).hasSize(2);
        assertThat(addedSetting).isSameAs(testCase.getDocumentation().get(0));
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

        final TestCase testCase = createCase();

        assertThat(testCase.getExecutionContext()).isEmpty();
        updater.insert(testCase, 0, kwExecutionRow);

        assertThat(testCase.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<TestCase> row = testCase.getExecutionContext().get(0);

        assertThat(row.getParent()).isSameAs(testCase);
        assertThat(row.getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);

        assertThat(row.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("action", "a", "b", "#comment");
        assertThat(row.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                        RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordArgumentsSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Arguments]", "a", "b", "c", "#comment"));

        final TestCase testCase = createCase();

        assertThat(testCase.getUnknownSettings()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getUnknownSettings()).hasSize(1);
        final LocalSetting<TestCase> setting = testCase.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETTING_UNKNOWN);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Arguments]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION,
                        RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordReturnSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Return]", "a", "b", "c", "#comment"));

        final TestCase testCase = createCase();

        assertThat(testCase.getUnknownSettings()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getUnknownSettings()).hasSize(1);
        final LocalSetting<TestCase> setting = testCase.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETTING_UNKNOWN);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Return]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION,
                        RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordTagsSettingIsProperlyMorphedIntoTagsSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Tags]", "a", "b", "c", "#comment"));

        final TestCase testCase = createCase();

        assertThat(testCase.getTags()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getTags()).hasSize(1);
        final LocalSetting<TestCase> setting = testCase.getTags().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_TAGS);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Tags]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION,
                        RobotTokenType.TEST_CASE_SETTING_TAGS, RobotTokenType.TEST_CASE_SETTING_TAGS,
                        RobotTokenType.TEST_CASE_SETTING_TAGS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordTeardownSettingIsProperlyMorphedIntoTeardownSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Teardown]", "a", "b", "c", "#comment"));

        final TestCase testCase = createCase();

        assertThat(testCase.getTeardowns()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getTeardowns()).hasSize(1);
        final LocalSetting<TestCase> setting = testCase.getTeardowns().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_TEARDOWN);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_SETTING_TEARDOWN,
                        RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME,
                        RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT,
                        RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordTimeoutSettingIsProperlyMorphedIntoTimeoutSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Timeout]", "a", "b", "c", "#comment"));

        final TestCase testCase = createCase();

        assertThat(testCase.getTimeouts()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getTimeouts()).hasSize(1);
        final LocalSetting<TestCase> setting = testCase.getTimeouts().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_TIMEOUT);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_SETTING_TIMEOUT,
                        RobotTokenType.TEST_CASE_SETTING_TIMEOUT_VALUE,
                        RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE,
                        RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordDocumentationSettingIsProperlyMorphedIntoDocumentationSetting_whenInserted() {
        final LocalSetting<?> keywordSetting = (LocalSetting<?>) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, newArrayList("[Documentation]", "a", "b", "c", "#comment"));

        final TestCase testCase = createCase();

        assertThat(testCase.getDocumentation()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getDocumentation()).hasSize(1);
        final LocalSetting<TestCase> setting = testCase.getDocumentation().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_DOCUMENTATION);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION,
                        RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT,
                        RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT,
                        RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordUnknownSetupSettingIsProperlyMorphedIntoSetupSetting_whenInserted() {
        final LocalSetting<UserKeyword> keywordSetting = new LocalSetting<>(ModelType.USER_KEYWORD_SETTING_UNKNOWN,
                RobotToken.create("[Setup]"));
        keywordSetting.addToken("a");
        keywordSetting.addToken("b");
        keywordSetting.addToken("c");
        keywordSetting.setComment("comment");

        final TestCase testCase = createCase();

        assertThat(testCase.getSetups()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getSetups()).hasSize(1);
        final LocalSetting<TestCase> setting = testCase.getSetups().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETUP);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Setup]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_SETTING_SETUP,
                        RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME,
                        RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT,
                        RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordUnknownTemplateSettingIsProperlyMorphedIntoTemplateSetting_whenInserted() {
        final LocalSetting<UserKeyword> keywordSetting = new LocalSetting<>(ModelType.USER_KEYWORD_SETTING_UNKNOWN,
                RobotToken.create("[Template]"));
        keywordSetting.addToken("a");
        keywordSetting.addToken("b");
        keywordSetting.addToken("c");
        keywordSetting.setComment("comment");

        final TestCase testCase = createCase();

        assertThat(testCase.getTemplates()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getTemplates()).hasSize(1);
        final LocalSetting<TestCase> setting = testCase.getTemplates().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_TEMPLATE);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Template]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_SETTING_TEMPLATE,
                        RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME,
                        RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT,
                        RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT,
                        RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordUnknownTemplateSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<UserKeyword> keywordSetting = new LocalSetting<>(ModelType.USER_KEYWORD_SETTING_UNKNOWN,
                RobotToken.create("[something]"));
        keywordSetting.addToken("a");
        keywordSetting.addToken("b");
        keywordSetting.addToken("c");
        keywordSetting.setComment("comment");

        final TestCase testCase = createCase();

        assertThat(testCase.getUnknownSettings()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getUnknownSettings()).hasSize(1);
        final LocalSetting<TestCase> setting = testCase.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETTING_UNKNOWN);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[something]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION,
                        RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void executableRowWithTemplateCreationTest() {
        final TestCase testCase = createCase();
        final LocalSetting<TestCase> template = testCase.newTemplate(0);
        template.addToken("Some Kw");

        assertThat(testCase.getExecutionContext()).isEmpty();

        final AModelElement<?> row = updater.createExecutableRow(testCase, 1,
                newArrayList("a1", "a2", "a3", "a4", "#comment"));

        assertThat(testCase.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<TestCase> addedRow = testCase.getExecutionContext().get(0);

        assertThat(addedRow).isSameAs(row);
        assertThat(addedRow.getParent()).isSameAs(testCase);
        assertThat(addedRow.getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);

        assertThat(addedRow.getElementTokens())
                .filteredOn(token -> token.getTypes().contains(RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT))
                .extracting(RobotToken::getText)
                .containsExactly("a1", "a2", "a3", "a4");

    }

    @Test
    public void testInsertingExecutableRowFromTestWithoutTemplateToTestWithTemplate() {
        final TestCase testCase = createCase();
        final LocalSetting<TestCase> template = testCase.newTemplate(0);
        template.addToken("Some Kw");

        assertThat(testCase.getExecutionContext()).isEmpty();

        final RobotExecutableRow<TestCase> rowToInsert = new RobotExecutableRow<>();
        rowToInsert.setAction(RobotToken.create("Kw Call", RobotTokenType.TEST_CASE_ACTION_NAME));
        rowToInsert.setArgument(0, RobotToken.create("a1"));
        rowToInsert.setArgument(1, RobotToken.create("a2"));
        rowToInsert.setArgument(2, RobotToken.create("a3"));
        final AModelElement<?> insertedRow = updater.insert(testCase, 1, rowToInsert);

        assertThat(testCase.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<TestCase> addedRow = testCase.getExecutionContext().get(0);

        assertThat(addedRow).isSameAs(insertedRow);
        assertThat(addedRow.getParent()).isSameAs(testCase);
        assertThat(addedRow.getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);

        assertThat(addedRow.getElementTokens())
                .filteredOn(token -> token.getTypes().contains(RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT))
                .extracting(RobotToken::getText)
                .containsExactly("Kw Call", "a1", "a2", "a3");

    }

    @Test
    public void testInsertingExecutableRowFromTestWithTemplateToTestWithoutTemplate() {
        final TestCase testCase = createCase();

        assertThat(testCase.getExecutionContext()).isEmpty();

        final RobotExecutableRow<TestCase> rowToInsert = new RobotExecutableRow<>();
        rowToInsert.setAction(RobotToken.create("Kw Call", RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT));
        rowToInsert.setArgument(0, RobotToken.create("a1", RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT));
        rowToInsert.setArgument(1, RobotToken.create("a2", RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT));
        rowToInsert.setArgument(2, RobotToken.create("a3", RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT));
        final AModelElement<?> insertedRow = updater.insert(testCase, 0, rowToInsert);

        assertThat(testCase.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<TestCase> addedRow = testCase.getExecutionContext().get(0);

        assertThat(addedRow).isSameAs(insertedRow);
        assertThat(addedRow.getParent()).isSameAs(testCase);
        assertThat(addedRow.getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);

        assertThat(addedRow.getElementTokens())
                .filteredOn(token -> token.getTypes().contains(RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT))
                .isEmpty();

    }

    private static TestCase createCase() {
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
}
