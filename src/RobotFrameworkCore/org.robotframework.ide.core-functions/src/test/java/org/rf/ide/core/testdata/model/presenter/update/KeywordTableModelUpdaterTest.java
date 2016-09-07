/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
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
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
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
                handler.create(keyword, "action", newArrayList("1", "2"), "");
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
            try {
                handler.remove(keyword, element);
                fail("Expected exception");
            } catch (final UnsupportedOperationException e) {
                // we expected that
            }
        }
        verifyZeroInteractions(keyword, element);
    }

    @Test
    public void testExecutableRowCRUD() {
        final ArrayList<String> execArgs = newArrayList("arg1", "arg2");
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

        final ArrayList<String> newArgs = newArrayList("1", "2", "3");
        modelUpdater.setArguments(executable, newArgs);

        checkSetting(executable.getArguments(), newArgs, executable.getComment(), newComment);

        checkRemoveMethod(userKeyword.getKeywordExecutionRows(), modelElement);
    }

    @Test
    public void testArgumentsCRUD() {
        final String keywordSettingName = "[Arguments]";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";

        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, keywordSettingName, comment,
                settingArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_ARGUMENTS);
        final KeywordArguments setting = (KeywordArguments) modelElement;

        checkSetting(setting.getArguments(), settingArgs, setting.getComment(), comment);

        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.updateArgument(setting, 1, newArg3);
        modelUpdater.updateArgument(setting, 2, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getArguments(), settingArgs, setting.getComment(), newComment);

        final ArrayList<String> newArgs = newArrayList("1", "2", "3");
        modelUpdater.setArguments(setting, newArgs);

        checkSetting(setting.getArguments(), newArgs, setting.getComment(), newComment);

        checkRemoveMethod(userKeyword.getArguments(), modelElement);
    }

    @Test
    public void testDocumentationCRUD() {
        final String keywordSettingName = "[Documentation]";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";

        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, keywordSettingName, comment,
                settingArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_DOCUMENTATION);
        final KeywordDocumentation setting = (KeywordDocumentation) modelElement;

        checkSetting(setting.getDocumentationText(), settingArgs, setting.getComment(), comment);

        settingArgs.clear();
        final String newArg3 = "arg3";
        settingArgs.add(newArg3);
        final String newComment = "new comment";

        modelUpdater.updateArgument(setting, 0, newArg3);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getDocumentationText(), newArrayList(newArg3), setting.getComment(), newComment);

        modelUpdater.setArguments(setting, newArrayList("1", "2", "3"));

        checkSetting(setting.getDocumentationText(), newArrayList("1"), setting.getComment(), newComment);

        checkRemoveMethod(userKeyword.getDocumentation(), modelElement);
    }

    @Test
    public void testTagsCRUD() {
        final String keywordSettingName = "[Tags]";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";

        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, keywordSettingName, comment,
                settingArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_TAGS);
        final KeywordTags setting = (KeywordTags) modelElement;

        checkSetting(setting.getTags(), settingArgs, setting.getComment(), comment);

        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.updateArgument(setting, 1, newArg3);
        modelUpdater.updateArgument(setting, 2, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getTags(), settingArgs, setting.getComment(), newComment);

        final ArrayList<String> newArgs = newArrayList("1", "2", "3");
        modelUpdater.setArguments(setting, newArgs);

        checkSetting(setting.getTags(), newArgs, setting.getComment(), newComment);

        checkRemoveMethod(userKeyword.getTags(), modelElement);
    }

    @Test
    public void testTimeoutCRUD() {
        final String keywordSettingName = "[Timeout]";
        final String timeout = "2 seconds";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final List<String> args = newArrayList(timeout);
        args.addAll(settingArgs);
        final String comment = "comment";

        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, keywordSettingName, comment,
                args);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_TIMEOUT);
        final KeywordTimeout setting = (KeywordTimeout) modelElement;

        checkSetting(setting.getTimeout(), timeout, setting.getMessage(), settingArgs, setting.getComment(), comment);

        final String newTimeout = "3 seconds";
        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.updateArgument(setting, 0, newTimeout);
        modelUpdater.updateArgument(setting, 2, newArg3);
        modelUpdater.updateArgument(setting, 3, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getTimeout(), newTimeout, setting.getMessage(), settingArgs, setting.getComment(),
                newComment);

        modelUpdater.setArguments(setting, newArrayList("1", "2", "3"));

        checkSetting(setting.getTimeout(), "1", setting.getMessage(), newArrayList("2", "3"), setting.getComment(),
                newComment);

        checkRemoveMethod(userKeyword.getTimeouts(), modelElement);
    }

    @Test
    public void testTeardownCRUD() {
        final String keywordSettingName = "[Teardown]";
        final String teardown = "teardown";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final List<String> args = newArrayList(teardown);
        args.addAll(settingArgs);
        final String comment = "comment";

        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, keywordSettingName, comment,
                args);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_TEARDOWN);
        final KeywordTeardown setting = (KeywordTeardown) modelElement;

        checkSetting(setting.getKeywordName(), teardown, setting.getArguments(), settingArgs, setting.getComment(),
                comment);

        final String newTeardown = "teardown2";
        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.updateArgument(setting, 0, newTeardown);
        modelUpdater.updateArgument(setting, 2, newArg3);
        modelUpdater.updateArgument(setting, 3, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getKeywordName(), newTeardown, setting.getArguments(), settingArgs, setting.getComment(),
                newComment);

        modelUpdater.setArguments(setting, newArrayList("1", "2", "3"));

        checkSetting(setting.getKeywordName(), "1", setting.getArguments(), newArrayList("2", "3"),
                setting.getComment(), newComment);

        checkRemoveMethod(userKeyword.getTeardowns(), modelElement);
    }

    @Test
    public void testReturnCRUD() {
        final String keywordSettingName = "[Return]";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";

        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, keywordSettingName, comment,
                settingArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_RETURN);
        final KeywordReturn setting = (KeywordReturn) modelElement;

        checkSetting(setting.getReturnValues(), settingArgs, setting.getComment(), comment);

        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.updateArgument(setting, 1, newArg3);
        modelUpdater.updateArgument(setting, 2, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getReturnValues(), settingArgs, setting.getComment(), newComment);

        final ArrayList<String> newArgs = newArrayList("1", "2", "3");
        modelUpdater.setArguments(setting, newArgs);

        checkSetting(setting.getReturnValues(), newArgs, setting.getComment(), newComment);

        checkRemoveMethod(userKeyword.getReturns(), modelElement);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenCreatingExecutableRowForNullCase() {
        modelUpdater.createExecutableRow(null, 0, "some action", "comment", newArrayList("a", "b", "c"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfBoundsExceptionIsThrown_whenTryingToCreateExecutableRowWithMismatchingIndex() {
        assertThat(userKeyword.getKeywordExecutionRows()).isEmpty();

        modelUpdater.createExecutableRow(userKeyword, 2, "some action", "comment", newArrayList("a", "b", "c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenCreatingSettingForNullCase() {
        modelUpdater.createSetting(null, "Setup", "comment", newArrayList("a", "b", "c"));
    }

    @Test
    public void testUnknownCRUD() {
        final ArrayList<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final String keywordSettingName = "[Unknown]";

        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, keywordSettingName, comment,
                settingArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_SETTING_UNKNOWN);
        final KeywordUnknownSettings setting = (KeywordUnknownSettings) modelElement;

        checkSetting(setting.getArguments(), settingArgs, setting.getComment(), comment);

        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.updateArgument(setting, 1, newArg3);
        modelUpdater.updateArgument(setting, 2, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getArguments(), settingArgs, setting.getComment(), newComment);

        checkRemoveMethod(userKeyword.getUnknownSettings(), modelElement);
    }

    @Test
    public void testUpdateParent() {
        final RobotToken declaration = new RobotToken();

        final KeywordArguments args = new KeywordArguments(declaration);
        final KeywordDocumentation doc = new KeywordDocumentation(declaration);
        final KeywordTags tags = new KeywordTags(declaration);
        final KeywordTimeout timeout = new KeywordTimeout(declaration);
        final KeywordTeardown teardown = new KeywordTeardown(declaration);
        final KeywordReturn returnValue = new KeywordReturn(declaration);

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
        testCase.addTestExecutionRow(tcExecutionRow);

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getKeywordExecutionRows()).isEmpty();
        modelUpdater.insert(keyword, 0, tcExecutionRow);

        assertThat(keyword.getKeywordExecutionRows()).hasSize(1);
        final RobotExecutableRow<UserKeyword> row = keyword.getKeywordExecutionRows().get(0);

        assertThat(row.getParent()).isSameAs(keyword);
        assertThat(row.getModelType()).isEqualTo(ModelType.USER_KEYWORD_EXECUTABLE_ROW);

        assertThat(transform(row.getElementTokens(), toText())).containsExactly("action", "a", "b", "#comment");
        assertThat(transform(row.getElementTokens(), toType())).containsExactly(RobotTokenType.KEYWORD_ACTION_NAME,
                RobotTokenType.KEYWORD_ACTION_ARGUMENT, RobotTokenType.KEYWORD_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseSetupSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final TestCaseSetup setupSetting = (TestCaseSetup) new TestCaseTableModelUpdater()
                .createSetting(createCase(), "[Setup]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getUnknownSettings()).isEmpty();
        modelUpdater.insert(keyword, 0, setupSetting);

        assertThat(keyword.getUnknownSettings()).hasSize(1);
        final KeywordUnknownSettings setting = keyword.getUnknownSettings().get(0);

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
        final TestCaseTemplate templateSetting = (TestCaseTemplate) new TestCaseTableModelUpdater()
                .createSetting(createCase(), "[Template]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getUnknownSettings()).isEmpty();
        modelUpdater.insert(keyword, 0, templateSetting);

        assertThat(keyword.getUnknownSettings()).hasSize(1);
        final KeywordUnknownSettings setting = keyword.getUnknownSettings().get(0);

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
        final TestCaseTags tagsSettingSetting = (TestCaseTags) new TestCaseTableModelUpdater()
                .createSetting(createCase(), "[Tags]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getTags()).isEmpty();
        modelUpdater.insert(keyword, 0, tagsSettingSetting);

        assertThat(keyword.getTags()).hasSize(1);
        final KeywordTags setting = keyword.getTags().get(0);

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
        final TestCaseTeardown teardownSetting = (TestCaseTeardown) new TestCaseTableModelUpdater()
                .createSetting(createCase(), "[Teardown]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getTeardowns()).isEmpty();
        modelUpdater.insert(keyword, 0, teardownSetting);

        assertThat(keyword.getTeardowns()).hasSize(1);
        final KeywordTeardown setting = keyword.getTeardowns().get(0);

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
        final TestCaseTimeout timeoutSetting = (TestCaseTimeout) new TestCaseTableModelUpdater()
                .createSetting(createCase(), "[Timeout]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getTimeouts()).isEmpty();
        modelUpdater.insert(keyword, 0, timeoutSetting);

        assertThat(keyword.getTimeouts()).hasSize(1);
        final KeywordTimeout setting = keyword.getTimeouts().get(0);

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
        final TestDocumentation docSetting = (TestDocumentation) new TestCaseTableModelUpdater()
                .createSetting(createCase(), "[Documentation]", "comment", newArrayList("a", "b", "c"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getDocumentation()).isEmpty();
        modelUpdater.insert(keyword, 0, docSetting);

        assertThat(keyword.getDocumentation()).hasSize(1);
        final KeywordDocumentation setting = keyword.getDocumentation().get(0);

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
        final TestCaseUnknownSettings tcSetting = new TestCaseUnknownSettings(RobotToken.create("[Arguments]"));
        tcSetting.addArgument(RobotToken.create("a"));
        tcSetting.addArgument(RobotToken.create("b"));
        tcSetting.addArgument(RobotToken.create("c"));
        tcSetting.setComment("comment");

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getArguments()).isEmpty();
        modelUpdater.insert(keyword, 0, tcSetting);

        assertThat(keyword.getArguments()).hasSize(1);
        final KeywordArguments setting = keyword.getArguments().get(0);

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
        final TestCaseUnknownSettings tcSetting = new TestCaseUnknownSettings(RobotToken.create("[Return]"));
        tcSetting.addArgument(RobotToken.create("a"));
        tcSetting.addArgument(RobotToken.create("b"));
        tcSetting.addArgument(RobotToken.create("c"));
        tcSetting.setComment("comment");

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getReturns()).isEmpty();
        modelUpdater.insert(keyword, 0, tcSetting);

        assertThat(keyword.getReturns()).hasSize(1);
        final KeywordReturn setting = keyword.getReturns().get(0);

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
        final TestCaseUnknownSettings tcSetting = new TestCaseUnknownSettings(RobotToken.create("[something]"));
        tcSetting.addArgument(RobotToken.create("a"));
        tcSetting.addArgument(RobotToken.create("b"));
        tcSetting.addArgument(RobotToken.create("c"));
        tcSetting.setComment("comment");

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getUnknownSettings()).isEmpty();
        modelUpdater.insert(keyword, 0, tcSetting);

        assertThat(keyword.getUnknownSettings()).hasSize(1);
        final KeywordUnknownSettings setting = keyword.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_SETTING_UNKNOWN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[something]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION, RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.START_HASH_COMMENT);
    }

    private void checkSetting(final RobotToken actualKeywordName, final String expectedKeywordName,
            final List<RobotToken> actualArguments, final List<String> expectedArguments,
            final List<RobotToken> actualComments, final String expectedComment) {
        assertTrue(actualKeywordName.getText().equals(expectedKeywordName));
        checkSettingArguments(actualArguments, expectedArguments);
        checkSettingComment(actualComments, expectedComment);
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

    private void checkRemoveMethod(final List<?> keywordSettings, final AModelElement<?> modelElement) {
        assertFalse(keywordSettings.isEmpty());
        modelUpdater.remove(userKeyword, modelElement);
        assertTrue(keywordSettings.isEmpty());
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
