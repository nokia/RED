/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
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
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

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
    public void testExecutableRowCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createExecutableRow(userKeyword, 0,
                newArrayList("call", "arg1", "arg2", "#comment"));

        assertThat(modelElement.getModelType()).isEqualTo(ModelType.USER_KEYWORD_EXECUTABLE_ROW);
        final RobotExecutableRow<?> executable = (RobotExecutableRow<?>) modelElement;

        assertThat(cellsOf(executable)).containsExactly("call", "arg1", "arg2", "#comment");

        executable.updateToken(2, "arg3");
        executable.createToken(3);
        executable.updateToken(3, "arg4");
        executable.updateToken(4, "#new comment");

        assertThat(cellsOf(executable)).containsExactly("call", "arg1", "arg3", "arg4", "#new comment");
    }

    @Test
    public void testArgumentsCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0,
                newArrayList("[Arguments]", "arg1", "arg2", "#comment"));

        assertThat(modelElement.getModelType()).isEqualTo(ModelType.USER_KEYWORD_ARGUMENTS);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Arguments]", "arg1", "arg2", "#comment");

        setting.updateToken(2, "arg3");
        setting.createToken(3);
        setting.updateToken(3, "arg4");
        setting.updateToken(4, "#new comment");

        assertThat(cellsOf(setting)).containsExactly("[Arguments]", "arg1", "arg3", "arg4", "#new comment");
    }

    @Test
    public void testDocumentationCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0,
                newArrayList("[Documentation]", "arg1", "arg2", "#comment"));

        assertThat(modelElement.getModelType()).isEqualTo(ModelType.USER_KEYWORD_DOCUMENTATION);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Documentation]", "arg1", "arg2", "#comment");

        setting.updateToken(2, "arg3");
        setting.updateToken(3, "#new comment");

        assertThat(cellsOf(setting)).containsExactly("[Documentation]", "arg1", "arg3", "#new comment");
    }

    @Test
    public void testTagsCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0,
                newArrayList("[Tags]", "arg1", "arg2", "#comment"));

        assertThat(modelElement.getModelType()).isEqualTo(ModelType.USER_KEYWORD_TAGS);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Tags]", "arg1", "arg2", "#comment");

        setting.updateToken(2, "arg3");
        setting.createToken(3);
        setting.updateToken(3, "arg4");
        setting.updateToken(4, "#new comment");

        assertThat(cellsOf(setting)).containsExactly("[Tags]", "arg1", "arg3", "arg4", "#new comment");
    }

    @Test
    public void testTimeoutCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0,
                newArrayList("[Timeout]", "2 seconds", "arg1", "arg2", "#comment"));

        assertThat(modelElement.getModelType()).isEqualTo(ModelType.USER_KEYWORD_TIMEOUT);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Timeout]", "2 seconds", "arg1", "arg2", "#comment");

        setting.updateToken(1, "3 seconds");
        setting.updateToken(3, "arg3");
        setting.createToken(4);
        setting.updateToken(4, "arg4");
        setting.updateToken(5, "#new comment");

        assertThat(cellsOf(setting)).containsExactly("[Timeout]", "3 seconds", "arg1", "arg3", "arg4", "#new comment");
    }

    @Test
    public void testTeardownCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0,
                newArrayList("[Teardown]", "teardown", "arg1", "arg2", "#comment"));

        assertThat(modelElement.getModelType()).isEqualTo(ModelType.USER_KEYWORD_TEARDOWN);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Teardown]", "teardown", "arg1", "arg2", "#comment");

        setting.updateToken(1, "teardown2");
        setting.updateToken(3, "arg3");
        setting.createToken(4);
        setting.updateToken(4, "arg4");
        setting.updateToken(5, "#new comment");

        assertThat(cellsOf(setting)).containsExactly("[Teardown]", "teardown2", "arg1", "arg3", "arg4", "#new comment");
    }

    @Test
    public void testReturnCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0,
                newArrayList("[Return]", "arg1", "arg2", "#comment"));

        assertThat(modelElement.getModelType()).isEqualTo(ModelType.USER_KEYWORD_RETURN);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Return]", "arg1", "arg2", "#comment");

        setting.updateToken(2, "arg3");
        setting.createToken(3);
        setting.updateToken(3, "arg4");
        setting.updateToken(4, "#new comment");

        assertThat(cellsOf(setting)).containsExactly("[Return]", "arg1", "arg3", "arg4", "#new comment");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfBoundsExceptionIsThrown_whenTryingToCreateExecutableRowWithMismatchingIndex() {
        assertThat(userKeyword.getExecutionContext()).isEmpty();

        modelUpdater.createExecutableRow(userKeyword, 2, newArrayList("some action", "a", "b", "c", "#comment"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenCreatingSettingForNullCase() {
        modelUpdater.createSetting(null, 0, newArrayList("Setup", "a", "b", "c", "#comment"));
    }

    @Test
    public void testUnknownCRUD() {
        final AModelElement<?> modelElement = modelUpdater.createSetting(userKeyword, 0,
                newArrayList("[Unknown]", "arg1", "arg2", "#comment"));

        assertThat(modelElement.getModelType()).isEqualTo(ModelType.USER_KEYWORD_SETTING_UNKNOWN);
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;

        assertThat(cellsOf(setting)).containsExactly("[Unknown]", "arg1", "arg2", "#comment");

        setting.updateToken(2, "arg3");
        setting.createToken(3);
        setting.updateToken(3, "arg4");
        setting.updateToken(4, "#new comment");

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

        assertThat(userKeyword.getArguments()).contains(args);
        assertThat(userKeyword.getDocumentation()).contains(doc);
        assertThat(userKeyword.getTags()).contains(tags);
        assertThat(userKeyword.getTimeouts()).contains(timeout);
        assertThat(userKeyword.getTeardowns()).contains(teardown);
        assertThat(userKeyword.getReturns()).contains(returnValue);
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

        assertThat(row.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("action", "a", "b", "#comment");
        assertThat(row.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_ACTION_NAME, RobotTokenType.KEYWORD_ACTION_ARGUMENT,
                        RobotTokenType.KEYWORD_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseSetupSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<TestCase> setupSetting = new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, newArrayList("[Setup]", "a", "b", "c", "#comment"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getUnknownSettings()).isEmpty();
        modelUpdater.insert(keyword, 0, setupSetting);

        assertThat(keyword.getUnknownSettings()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_SETTING_UNKNOWN);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Setup]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION,
                        RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTemplateSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final LocalSetting<TestCase> templateSetting = new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, newArrayList("[Template]", "a", "b", "c", "#comment"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getUnknownSettings()).isEmpty();
        modelUpdater.insert(keyword, 0, templateSetting);

        assertThat(keyword.getUnknownSettings()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_SETTING_UNKNOWN);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Template]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION,
                        RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTagsSettingIsProperlyMorphedIntoTagsSetting_whenInserted() {
        final LocalSetting<TestCase> tagsSettingSetting = new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, newArrayList("[Tags]", "a", "b", "c", "#comment"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getTags()).isEmpty();
        modelUpdater.insert(keyword, 0, tagsSettingSetting);

        assertThat(keyword.getTags()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getTags().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_TAGS);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Tags]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_SETTING_TAGS, RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME,
                        RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME, RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME,
                        RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTeardownSettingIsProperlyMorphedIntoTeardownSetting_whenInserted() {
        final LocalSetting<TestCase> teardownSetting = new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, newArrayList("[Teardown]", "a", "b", "c", "#comment"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getTeardowns()).isEmpty();
        modelUpdater.insert(keyword, 0, teardownSetting);

        assertThat(keyword.getTeardowns()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getTeardowns().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_TEARDOWN);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_SETTING_TEARDOWN,
                        RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME,
                        RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT,
                        RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseTimeoutSettingIsProperlyMorphedIntoTimeoutSetting_whenInserted() {
        final LocalSetting<TestCase> timeoutSetting = new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, newArrayList("[Timeout]", "a", "b", "c", "#comment"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getTimeouts()).isEmpty();
        modelUpdater.insert(keyword, 0, timeoutSetting);

        assertThat(keyword.getTimeouts()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getTimeouts().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_TIMEOUT);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_SETTING_TIMEOUT, RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE,
                        RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE, RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE,
                        RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void testCaseDocumentationSettingIsProperlyMorphedIntoDocumentationSetting_whenInserted() {
        final LocalSetting<TestCase> docSetting = new TestCaseTableModelUpdater()
                .createSetting(createCase(), 0, newArrayList("[Documentation]", "a", "b", "c", "#comment"));

        final UserKeyword keyword = createKeyword();

        assertThat(keyword.getDocumentation()).isEmpty();
        modelUpdater.insert(keyword, 0, docSetting);

        assertThat(keyword.getDocumentation()).hasSize(1);
        final LocalSetting<UserKeyword> setting = keyword.getDocumentation().get(0);

        assertThat(setting.getParent()).isSameAs(keyword);
        assertThat(setting.getModelType()).isEqualTo(ModelType.USER_KEYWORD_DOCUMENTATION);

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION,
                        RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT,
                        RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT,
                        RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT, RobotTokenType.START_HASH_COMMENT);
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

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Arguments]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_SETTING_ARGUMENTS, RobotTokenType.KEYWORD_SETTING_ARGUMENT,
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

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[Return]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_SETTING_RETURN, RobotTokenType.KEYWORD_SETTING_RETURN_VALUE,
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

        assertThat(setting.getElementTokens()).extracting(RobotToken::getText)
                .containsExactly("[something]", "a", "b", "c", "#comment");
        assertThat(setting.getElementTokens()).extracting(token -> token.getTypes().get(0))
                .containsExactly(RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION,
                        RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS,
                        RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    private static List<String> cellsOf(final AModelElement<?> setting) {
        return setting.getElementTokens().stream().map(RobotToken::getText).collect(toList());
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
