/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

import com.google.common.base.Function;

public class KeywordTableModelUpdaterTest {

    private final List<ModelType> testCaseModelTypes = newArrayList(ModelType.TEST_CASE_EXECUTABLE_ROW,
            ModelType.TEST_CASE_SETTING_UNKNOWN, ModelType.TEST_CASE_DOCUMENTATION, ModelType.TEST_CASE_TAGS,
            ModelType.TEST_CASE_TEARDOWN, ModelType.TEST_CASE_TIMEOUT, ModelType.TEST_CASE_SETUP,
            ModelType.TEST_CASE_TEMPLATE);

    private static KeywordTable keywordTable;

    private static KeywordTableModelUpdater modelUpdater;

    private UserKeyword userKeyword;

    @BeforeClass
    public static void setupModel() {
        final RobotFile model = NewRobotFileTestHelper.getModelFileToModify("2.9");
        model.includeKeywordTableSection();
        keywordTable = model.getKeywordTable();

        modelUpdater = new KeywordTableModelUpdater();
    }

    @Before
    public void setupKeyword() {
        userKeyword = keywordTable.createUserKeyword("UserKeyword");
    }

    @Test
    public void testOperationAvailabilityForDifferentTokenTypes() {
        assertThat(modelUpdater.getOperationHandler(RobotTokenType.KEYWORD_ACTION_NAME)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(RobotTokenType.KEYWORD_SETTING_TAGS)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(RobotTokenType.KEYWORD_SETTING_TEARDOWN)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(RobotTokenType.KEYWORD_SETTING_TIMEOUT)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(RobotTokenType.KEYWORD_SETTING_ARGUMENTS)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(RobotTokenType.KEYWORD_SETTING_RETURN)).isNotNull();
    }

    @Test
    public void testOperationAvailabilityForDifferentModelTypes() {
        assertThat(modelUpdater.getOperationHandler(ModelType.USER_KEYWORD_EXECUTABLE_ROW)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(ModelType.USER_KEYWORD_SETTING_UNKNOWN)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(ModelType.USER_KEYWORD_DOCUMENTATION)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(ModelType.USER_KEYWORD_TAGS)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(ModelType.USER_KEYWORD_TEARDOWN)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(ModelType.USER_KEYWORD_TIMEOUT)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(ModelType.USER_KEYWORD_SETTING_UNKNOWN)).isNotNull();
        assertThat(modelUpdater.getOperationHandler(ModelType.USER_KEYWORD_RETURN)).isNotNull();

        for (final ModelType kwModelType : testCaseModelTypes) {
            assertThat(modelUpdater.getOperationHandler(kwModelType)).isNotNull();
        }
    }

    @Test
    public void handlersForKeywordCannotCreateUpdateBulkUpdateOrRemoveAnything() {
        final AModelElement<?> element = mock(AModelElement.class);
        final UserKeyword keyword = mock(UserKeyword.class);
        for (final ModelType tcModelType : testCaseModelTypes) {
            final IExecutablesStepsHolderElementOperation<UserKeyword> handler = modelUpdater
                    .getOperationHandler(tcModelType);

            try {
                handler.create(keyword, 0, "action", newArrayList("1", "2"), "");
                fail("Expected exception");
            } catch (final UnsupportedOperationException e) {
                // we expected that
            }
            try {
                handler.update(element, 0, "arg");
                fail("Expected exception");
            } catch (final UnsupportedOperationException e) {
                // we expected that
            }
            try {
                handler.update(element, newArrayList("arg1", "arg2"));
                fail("Expected exception");
            } catch (final UnsupportedOperationException e) {
                // we expected that
            }
        }
        verifyZeroInteractions(keyword, element);
    }

    @Test
    public void testExecutableRowCRUD() {
        final List<String> execArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final String keywordName = "call";

        final AModelElement<?> modelElement = modelUpdater.createExecutableRow(userKeyword, 0, keywordName, comment,
                execArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW);
        final RobotExecutableRow<?> executable = (RobotExecutableRow<?>) modelElement;

        checkSetting(executable.getArguments(), execArgs, executable.getComment(), comment);

        final String newArg3 = "arg3";
        execArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        execArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.updateArgument(executable, 1, newArg3);
        modelUpdater.updateArgument(executable, 2, newArg4);
        modelUpdater.updateComment(executable, newComment);

        checkSetting(executable.getArguments(), execArgs, executable.getComment(), newComment);

        final List<String> newArgs = newArrayList("1", "2", "3");
        modelUpdater.setArguments(executable, newArgs);

        checkSetting(executable.getArguments(), newArgs, executable.getComment(), newComment);
    }

    @Test
    public void testArgumentsCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0, "[Arguments]", "comment",
                newArrayList("arg1", "arg2"));

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_ARGUMENTS);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Arguments]", "arg1", "arg2", "#comment");

        modelUpdater.updateArgument(setting, 1, "arg3");
        modelUpdater.updateArgument(setting, 2, "arg4");
        modelUpdater.updateComment(setting, "new comment");

        assertThat(cellsOf(setting)).containsExactly("[Arguments]", "arg1", "arg3", "arg4", "#new comment");

        final List<String> newArgs = newArrayList("1", "2", "3");
        modelUpdater.setArguments(setting, newArgs);

        assertThat(cellsOf(setting)).containsExactly("[Arguments]", "1", "2", "3", "#new comment");
    }

    @Test
    public void testDocumentationCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0, "[Documentation]", "comment",
                newArrayList("arg1", "arg2"));

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_DOCUMENTATION);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Documentation]", "arg1", "arg2", "#comment");

        modelUpdater.updateArgument(setting, 0, "arg3");
        modelUpdater.updateComment(setting, "new comment");

        assertThat(cellsOf(setting)).containsExactly("[Documentation]", "arg3", "#new comment");

        modelUpdater.setArguments(setting, newArrayList("1", "2", "3"));

        assertThat(cellsOf(setting)).containsExactly("[Documentation]", "1", "#new comment");
    }

    @Test
    public void testTagsCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0, "[Tags]", "comment",
                newArrayList("arg1", "arg2"));

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_TAGS);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Tags]", "arg1", "arg2", "#comment");

        modelUpdater.updateArgument(setting, 1, "arg3");
        modelUpdater.updateArgument(setting, 2, "arg4");
        modelUpdater.updateComment(setting, "new comment");

        assertThat(cellsOf(setting)).containsExactly("[Tags]", "arg1", "arg3", "arg4", "#new comment");

        modelUpdater.setArguments(setting, newArrayList("1", "2", "3"));

        assertThat(cellsOf(setting)).containsExactly("[Tags]", "1", "2", "3", "#new comment");
    }

    @Test
    public void testTimeoutCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0, "[Timeout]", "comment",
                newArrayList("2 seconds", "arg1", "arg2"));

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_TIMEOUT);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Timeout]", "2 seconds", "arg1", "arg2", "#comment");

        modelUpdater.updateArgument(setting, 0, "3 seconds");
        modelUpdater.updateArgument(setting, 2, "arg3");
        modelUpdater.updateArgument(setting, 3, "arg4");
        modelUpdater.updateComment(setting, "new comment");

        assertThat(cellsOf(setting)).containsExactly("[Timeout]", "3 seconds", "arg1", "arg3", "arg4", "#new comment");

        modelUpdater.setArguments(setting, newArrayList("1", "2", "3"));

        assertThat(cellsOf(setting)).containsExactly("[Timeout]", "1", "2", "3", "#new comment");
    }

    @Test
    public void testTeardownCRUD() {
        final List<String> args = newArrayList("teardown", "arg1", "arg2");

        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0, "[Teardown]", "comment",
                args);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_TEARDOWN);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Teardown]", "teardown", "arg1", "arg2", "#comment");

        modelUpdater.updateArgument(setting, 0, "teardown2");
        modelUpdater.updateArgument(setting, 2, "arg3");
        modelUpdater.updateArgument(setting, 3, "arg4");
        modelUpdater.updateComment(setting, "new comment");

        assertThat(cellsOf(setting)).containsExactly("[Teardown]", "teardown2", "arg1", "arg3", "arg4", "#new comment");

        modelUpdater.setArguments(setting, newArrayList("1", "2", "3"));

        assertThat(cellsOf(setting)).containsExactly("[Teardown]", "1", "2", "3", "#new comment");
    }

    @Test
    public void testReturnCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0, "[Return]", "comment",
                newArrayList("arg1", "arg2"));

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_RETURN);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Return]", "arg1", "arg2", "#comment");

        modelUpdater.updateArgument(setting, 1, "arg3");
        modelUpdater.updateArgument(setting, 2, "arg4");
        modelUpdater.updateComment(setting, "new comment");

        assertThat(cellsOf(setting)).containsExactly("[Return]", "arg1", "arg3", "arg4", "#new comment");

        modelUpdater.setArguments(setting, newArrayList("1", "2", "3"));

        assertThat(cellsOf(setting)).containsExactly("[Return]", "1", "2", "3", "#new comment");
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenCreatingExecutableRowForNullCase() {
        modelUpdater.createExecutableRow(null, 0, "some action", "comment", newArrayList("a", "b", "c"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfBoundsExceptionIsThrown_whenTryingToCreateExecutableRowWithMismatchingIndex() {
        assertThat(userKeyword.getExecutionContext()).isEmpty();

        modelUpdater.createExecutableRow(userKeyword, 2, "some action", "comment", newArrayList("a", "b", "c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenCreatingSettingForNullCase() {
        modelUpdater.createSetting(null, 0, "Setup", "comment", newArrayList("a", "b", "c"));
    }

    @Test
    public void testUnknownCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0, "[Unknown]", "comment",
                newArrayList("arg1", "arg2"));

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_SETTING_UNKNOWN);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Unknown]", "arg1", "arg2", "#comment");

        modelUpdater.updateArgument(setting, 1, "arg3");
        modelUpdater.updateArgument(setting, 2, "arg4");
        modelUpdater.updateComment(setting, "new comment");

        assertThat(cellsOf(setting)).containsExactly("[Unknown]", "arg1", "arg3", "arg4", "#new comment");
    }

    @Test
    public void testUpdateParent() {
        final RobotToken declaration = new RobotToken();

        final LocalSetting<UserKeyword> args = new LocalSetting<>(ModelType.USER_KEYWORD_ARGUMENTS, declaration);
        final LocalSetting<UserKeyword> doc = new LocalSetting<>(ModelType.USER_KEYWORD_DOCUMENTATION, declaration);
        final LocalSetting<UserKeyword> tags = new LocalSetting<>(ModelType.USER_KEYWORD_TAGS, declaration);
        final LocalSetting<UserKeyword> timeout = new LocalSetting<>(ModelType.USER_KEYWORD_TIMEOUT, declaration);
        final LocalSetting<UserKeyword> teardown = new LocalSetting<>(ModelType.USER_KEYWORD_TEARDOWN, declaration);
        final LocalSetting<UserKeyword> returnValue = new LocalSetting<>(ModelType.USER_KEYWORD_RETURN, declaration);

        modelUpdater.insert(userKeyword, 0, args);
        modelUpdater.insert(userKeyword, 0, doc);
        modelUpdater.insert(userKeyword, 0, tags);
        modelUpdater.insert(userKeyword, 0, timeout);
        modelUpdater.insert(userKeyword, 0, teardown);
        modelUpdater.insert(userKeyword, 0, returnValue);

        assertTrue(userKeyword.getArguments().contains(args));
        assertTrue(userKeyword.getDocumentation().contains(doc));
        assertTrue(userKeyword.getTags().contains(tags));
        assertTrue(userKeyword.getTimeouts().contains(timeout));
        assertTrue(userKeyword.getTeardowns().contains(teardown));
        assertTrue(userKeyword.getReturns().contains(returnValue));
    }

    @Test
    public void testCaseExecutableRowIsProperlyMorphedIntoExecutableRow_whenInserted() {
        final RobotExecutableRow<TestCase> tcExecutionRow = new RobotExecutableRow<>();
        tcExecutionRow.setAction(RobotToken.create("action"));
        tcExecutionRow.addArgument(RobotToken.create("a"));
        tcExecutionRow.addArgument(RobotToken.create("b"));
        tcExecutionRow.setComment("comment");
        final TestCase testCase = createCase();
        testCase.addElement(tcExecutionRow);

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getExecutionContext()).isEmpty();
        modelUpdater.insert(keyword, 0, tcExecutionRow);

        assertThat(keyword.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<UserKeyword> row = keyword.getExecutionContext().get(0);

        assertThat(row.getParent()).isSameAs(keyword);
        assertThat(row.getModelType()).isEqualTo(ModelType.USER_KEYWORD_EXECUTABLE_ROW);

        assertThat(transform(row.getElementTokens(), toText())).containsExactly("action", "a", "b", "#comment");
        assertThat(transform(row.getElementTokens(), toType())).containsExactly(RobotTokenType.KEYWORD_ACTION_NAME,
                RobotTokenType.KEYWORD_ACTION_ARGUMENT, RobotTokenType.KEYWORD_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseSetupSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<TestCase> setupSetting = (LocalSetting<TestCase>) new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, "[Setup]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getUnknownSettings()).isEmpty();
        modelUpdater.insert(keyword, 0, setupSetting);

        assertThat(keyword.getUnknownSettings()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_SETTING_UNKNOWN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Setup]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION, RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTemplateSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<TestCase> templateSetting = (LocalSetting<TestCase>) new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, "[Template]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getUnknownSettings()).isEmpty();
        modelUpdater.insert(keyword, 0, templateSetting);

        assertThat(keyword.getUnknownSettings()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_SETTING_UNKNOWN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Template]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION, RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTagsSettingIsProperlyMorphedIntoTagsSetting_whenInserted() {
        final LocalSetting<TestCase> tagsSettingSetting = (LocalSetting<TestCase>) new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, "[Tags]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getTags()).isEmpty();
        modelUpdater.insert(keyword, 0, tagsSettingSetting);

        assertThat(keyword.getTags()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getTags().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_TAGS);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Tags]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_TAGS, RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME,
                RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME, RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTeardownSettingIsProperlyMorphedIntoTeardownSetting_whenInserted() {
        final LocalSetting<TestCase> teardownSetting = (LocalSetting<TestCase>) new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, "[Teardown]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getTeardowns()).isEmpty();
        modelUpdater.insert(keyword, 0, teardownSetting);

        assertThat(keyword.getTeardowns()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getTeardowns().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_TEARDOWN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Teardown]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_TEARDOWN, RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME,
                RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT,
                RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTimeoutSettingIsProperlyMorphedIntoTimeoutSetting_whenInserted() {
        final LocalSetting<TestCase> timeoutSetting = (LocalSetting<TestCase>) new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, "[Timeout]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getTimeouts()).isEmpty();
        modelUpdater.insert(keyword, 0, timeoutSetting);

        assertThat(keyword.getTimeouts()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getTimeouts().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_TIMEOUT);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Timeout]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_TIMEOUT, RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE,
                RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE, RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseDocumentationSettingIsProperlyMorphedIntoDocumentationSetting_whenInserted() {
        final LocalSetting<TestCase> docSetting = (LocalSetting<TestCase>) new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, "[Documentation]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getDocumentation()).isEmpty();
        modelUpdater.insert(keyword, 0, docSetting);

        assertThat(keyword.getDocumentation()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getDocumentation().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_DOCUMENTATION);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Documentation]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_DOCUMENTATION, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT,
                RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseUnknownSetupSettingIsProperlyMorphedIntoArgumentsSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new LocalSetting<>(ModelType.TEST_CASE_SETTING_UNKNOWN,
                RobotToken.create("[Arguments]"));
        tcSetting.addToken("a");
        tcSetting.addToken("b");
        tcSetting.addToken("c");
        tcSetting.setComment("comment");

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getArguments()).isEmpty();
        modelUpdater.insert(keyword, 0, tcSetting);

        assertThat(keyword.getArguments()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getArguments().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_ARGUMENTS);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Arguments]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_ARGUMENTS, RobotTokenType.KEYWORD_SETTING_ARGUMENT,
                RobotTokenType.KEYWORD_SETTING_ARGUMENT, RobotTokenType.KEYWORD_SETTING_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseUnknownTemplateSettingIsProperlyMorphedIntoReturnSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new LocalSetting<>(ModelType.TEST_CASE_SETTING_UNKNOWN,
                RobotToken.create("[Return]"));
        tcSetting.addToken("a");
        tcSetting.addToken("b");
        tcSetting.addToken("c");
        tcSetting.setComment("comment");

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getReturns()).isEmpty();
        modelUpdater.insert(keyword, 0, tcSetting);

        assertThat(keyword.getReturns()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getReturns().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_RETURN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Return]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_RETURN, RobotTokenType.KEYWORD_SETTING_RETURN_VALUE,
                RobotTokenType.KEYWORD_SETTING_RETURN_VALUE, RobotTokenType.KEYWORD_SETTING_RETURN_VALUE,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseUnknownTemplateSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<TestCase> tcSetting = new LocalSetting<>(ModelType.TEST_CASE_SETTING_UNKNOWN,
                RobotToken.create("[something]"));
        tcSetting.addToken("a");
        tcSetting.addToken("b");
        tcSetting.addToken("c");
        tcSetting.setComment("comment");

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getUnknownSettings()).isEmpty();
        modelUpdater.insert(keyword, 0, tcSetting);

        assertThat(keyword.getUnknownSettings()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_SETTING_UNKNOWN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[something]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION, RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.START_HASH_COMMENT);
    }

    private static List<String> cellsOf(final LocalSetting<?> setting) {
        return setting.getElementTokens().stream().map(RobotToken::getText).collect(toList());
    }

    private void checkSetting(final List<RobotToken> actualArguments, final List<String> expectedArguments,
            final List<RobotToken> actualComments, final String expectedComment) {
        checkSettingArguments(actualArguments, expectedArguments);
        checkSettingComment(actualComments, expectedComment);
    }

    private void checkSettingComment(final List<RobotToken> actualComments, final String expectedComment) {
        assertTrue(actualComments.get(0).getText().equals("#" + expectedComment));
    }

    private void checkSettingArguments(final List<RobotToken> actualArguments, final List<String> expectedArguments) {
        assertEquals(expectedArguments.size(), actualArguments.size());
        for (int i = 0; i < actualArguments.size(); i++) {
            assertTrue(actualArguments.get(i).getText().equals(expectedArguments.get(i)));
        }
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

    private static Function<RobotToken, String> toText() {
        return new Function<RobotToken, String>() {

            @Override
            public String apply(final RobotToken token) {
                return token.getText();
            }
        };
    }

    private static Function<RobotToken, IRobotTokenType> toType() {
        return new Function<RobotToken, IRobotTokenType>() {

            @Override
            public IRobotTokenType apply(final RobotToken token) {
                return token.getTypes().get(0);
            }
        };
    }
}
