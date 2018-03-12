/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

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

import com.google.common.base.Function;

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
    public void handlersForKeywordCannotCreateAnything() {
        final TestCase testCase = mock(TestCase.class);
        for (final ModelType kwModelType : keywordModelTypes) {
            final IExecutablesStepsHolderElementOperation<TestCase> handler = updater.getOperationHandler(kwModelType);

            try {
                handler.create(testCase, 0, "action", newArrayList("1", "2"), "");
                fail("Expected exception");
            } catch (final UnsupportedOperationException e) {
                // we expected that
            }
        }
        verifyZeroInteractions(testCase);
    }

    @Test
    public void handlersForKeywordCannotUpdateAnything() {
        final AModelElement<?> element = mock(AModelElement.class);
        for (final ModelType kwModelType : keywordModelTypes) {
            final IExecutablesStepsHolderElementOperation<TestCase> handler = updater.getOperationHandler(kwModelType);

            try {
                handler.update(element, 1, "value");
                fail("Expected exception");
            } catch (final UnsupportedOperationException e) {
                // we expected that
            }
        }
        verifyZeroInteractions(element);
    }

    @Test
    public void handlersForKeywordCannotBulkUpdateAnything() {
        final AModelElement<?> element = mock(AModelElement.class);
        for (final ModelType kwModelType : keywordModelTypes) {
            final IExecutablesStepsHolderElementOperation<TestCase> handler = updater.getOperationHandler(kwModelType);

            try {
                handler.update(element, newArrayList("a", "b", "c"));
                fail("Expected exception");
            } catch (final UnsupportedOperationException e) {
                // we expected that
            }
        }
        verifyZeroInteractions(element);
    }

    @Test
    public void handlersForKeywordCannotRemoveAnything() {
        final TestCase testCase = mock(TestCase.class);
        final AModelElement<?> element = mock(AModelElement.class);
        for (final ModelType kwModelType : keywordModelTypes) {
            final IExecutablesStepsHolderElementOperation<TestCase> handler = updater.getOperationHandler(kwModelType);

            try {
                handler.remove(testCase, element);
                fail("Expected exception");
            } catch (final UnsupportedOperationException e) {
                // we expected that
            }
        }
        verifyZeroInteractions(testCase, element);
    }

    @Test
    public void executableRowOpreationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getExecutionContext()).isEmpty();

        final AModelElement<?> row = updater.createExecutableRow(testCase, 0, "some action", "comment",
                newArrayList("a", "b", "c"));

        assertThat(testCase.getExecutionContext()).hasSize(1);
        final RobotExecutableRow<TestCase> addedRow = testCase.getExecutionContext().get(0);

        assertThat(addedRow).isSameAs(row);
        assertThat(addedRow.getParent()).isSameAs(testCase);
        assertThat(addedRow.getModelType()).isEqualTo(ModelType.TEST_CASE_EXECUTABLE_ROW);
        assertThat(addedRow.getAction().getText()).isEqualTo("some action");

        assertThat(transform(addedRow.getElementTokens(), toText())).containsExactly("some action", "a", "b", "c",
                "#comment");

        updater.updateComment(addedRow, "new comment");
        assertThat(transform(addedRow.getElementTokens(), toText())).containsExactly("some action", "a", "b", "c",
                "#new comment");

        updater.updateArgument(addedRow, 2, "x");
        assertThat(transform(addedRow.getElementTokens(), toText())).containsExactly("some action", "a", "b", "x",
                "#new comment");

        updater.updateArgument(addedRow, 5, "z");
        assertThat(transform(addedRow.getElementTokens(), toText())).containsExactly("some action", "a", "b", "x", "",
                "", "z", "#new comment");

        updater.updateArgument(addedRow, 3, null);
        assertThat(transform(addedRow.getElementTokens(), toText())).containsExactly("some action", "a", "b", "x", "",
                "z", "#new comment");

        updater.setArguments(addedRow, newArrayList("1", "2", "3"));
        assertThat(transform(addedRow.getElementTokens(), toText())).containsExactly("some action", "1", "2", "3",
                "#new comment");

        updater.remove(testCase, addedRow);
        assertThat(testCase.getExecutionContext()).isEmpty();

        updater.insert(testCase, 0, addedRow);
        assertThat(testCase.getExecutionContext()).hasSize(1);
        assertThat(addedRow).isSameAs(testCase.getExecutionContext().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenCreatingExecutableRowForNullCase() {
        updater.createExecutableRow(null, 0, "some action", "comment", newArrayList("a", "b", "c"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfBoundsExceptionIsThrown_whenTryingToCreateExecutableRowWithMismatchingIndex() {
        final TestCase testCase = createCase();
        assertThat(testCase.getExecutionContext()).isEmpty();

        updater.createExecutableRow(testCase, 2, "some action", "comment", newArrayList("a", "b", "c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenCreatingSettingForNullCase() {
        updater.createSetting(null, 0, "Setup", "comment", newArrayList("a", "b", "c"));
    }

    @Test
    public void setupSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getSetups()).isEmpty();

        final TestCaseSetup setting = (TestCaseSetup) updater.createSetting(testCase, 0, "[Setup]", "comment",
                newArrayList("a", "b", "c"));

        assertThat(testCase.getSetups()).hasSize(1);
        final TestCaseSetup addedSetting = testCase.getSetups().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETUP);

        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Setup]", "a", "b", "c",
                "#comment");

        updater.updateComment(addedSetting, "new comment");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Setup]", "a", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 0, "kw");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Setup]", "kw", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 2, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Setup]", "kw", "b", "x",
                "#new comment");

        updater.updateArgument(addedSetting, 5, "z");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Setup]", "kw", "b", "x", "",
                "", "z", "#new comment");

        updater.updateArgument(addedSetting, 3, null);
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Setup]", "kw", "b", "x", "",
                "z", "#new comment");

        updater.setArguments(addedSetting, newArrayList("1", "2", "3"));
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Setup]", "1", "2", "3",
                "#new comment");

        updater.remove(testCase, addedSetting);
        assertThat(testCase.getSetups()).isEmpty();

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getSetups()).hasSize(1);
        assertThat(addedSetting).isSameAs(testCase.getSetups().get(0));
    }

    @Test
    public void tagsSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTags()).isEmpty();

        final TestCaseTags setting = (TestCaseTags) updater.createSetting(testCase, 0, "[Tags]", "comment",
                newArrayList("a", "b", "c"));

        assertThat(testCase.getTags()).hasSize(1);
        final TestCaseTags addedSetting = testCase.getTags().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_TAGS);

        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Tags]", "a", "b", "c",
                "#comment");

        updater.updateComment(addedSetting, "new comment");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Tags]", "a", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 0, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Tags]", "x", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 2, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Tags]", "x", "b", "x",
                "#new comment");

        updater.updateArgument(addedSetting, 5, "z");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Tags]", "x", "b", "x", "",
                "", "z", "#new comment");

        updater.updateArgument(addedSetting, 3, null);
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Tags]", "x", "b", "x", "",
                "z", "#new comment");

        updater.setArguments(addedSetting, newArrayList("1", "2", "3"));
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Tags]", "1", "2", "3",
                "#new comment");

        updater.remove(testCase, addedSetting);
        assertThat(testCase.getTags()).isEmpty();

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getTags()).hasSize(1);
        assertThat(addedSetting).isSameAs(testCase.getTags().get(0));
    }

    @Test
    public void tagsTimeoutOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTimeouts()).isEmpty();

        final TestCaseTimeout setting = (TestCaseTimeout) updater.createSetting(testCase, 0, "[Timeout]", "comment",
                newArrayList("a", "b", "c"));

        assertThat(testCase.getTimeouts()).hasSize(1);
        final TestCaseTimeout addedSetting = testCase.getTimeouts().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_TIMEOUT);

        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Timeout]", "a", "b", "c",
                "#comment");

        updater.updateComment(addedSetting, "new comment");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Timeout]", "a", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 0, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Timeout]", "x", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 2, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Timeout]", "x", "b", "x",
                "#new comment");

        updater.updateArgument(addedSetting, 5, "z");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Timeout]", "x", "b", "x", "",
                "", "z", "#new comment");

        updater.updateArgument(addedSetting, 3, null);
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Timeout]", "x", "b", "x", "",
                "z", "#new comment");

        updater.setArguments(addedSetting, newArrayList("1", "2", "3"));
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Timeout]", "1", "2", "3",
                "#new comment");

        updater.remove(testCase, addedSetting);
        assertThat(testCase.getTags()).isEmpty();

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getTimeouts()).hasSize(1);
        assertThat(addedSetting).isSameAs(testCase.getTimeouts().get(0));
    }

    @Test
    public void teardownsSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTeardowns()).isEmpty();

        final TestCaseTeardown setting = (TestCaseTeardown) updater.createSetting(testCase, 0, "[Teardown]", "comment",
                newArrayList("a", "b", "c"));

        assertThat(testCase.getTeardowns()).hasSize(1);
        final TestCaseTeardown addedSetting = testCase.getTeardowns().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_TEARDOWN);

        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Teardown]", "a", "b", "c",
                "#comment");

        updater.updateComment(addedSetting, "new comment");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Teardown]", "a", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 0, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Teardown]", "x", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 2, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Teardown]", "x", "b", "x",
                "#new comment");

        updater.updateArgument(addedSetting, 5, "z");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Teardown]", "x", "b", "x",
                "", "", "z", "#new comment");

        updater.updateArgument(addedSetting, 3, null);
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Teardown]", "x", "b", "x",
                "", "z", "#new comment");

        updater.setArguments(addedSetting, newArrayList("1", "2", "3"));
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Teardown]", "1", "2", "3",
                "#new comment");

        updater.remove(testCase, addedSetting);
        assertThat(testCase.getTeardowns()).isEmpty();

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getTeardowns()).hasSize(1);
        assertThat(addedSetting).isSameAs(testCase.getTeardowns().get(0));
    }

    @Test
    public void templateSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTeardowns()).isEmpty();

        final TestCaseTemplate setting = (TestCaseTemplate) updater.createSetting(testCase, 0, "[Template]", "comment",
                newArrayList("a", "b", "c"));

        assertThat(testCase.getTemplates()).hasSize(1);
        final TestCaseTemplate addedSetting = testCase.getTemplates().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_TEMPLATE);

        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Template]", "a", "b", "c",
                "#comment");

        updater.updateComment(addedSetting, "new comment");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Template]", "a", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 0, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Template]", "x", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 2, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Template]", "x", "b", "x",
                "#new comment");

        updater.updateArgument(addedSetting, 5, "z");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Template]", "x", "b", "x",
                "", "", "z", "#new comment");

        updater.updateArgument(addedSetting, 3, null);
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Template]", "x", "b", "x",
                "", "z", "#new comment");

        updater.setArguments(addedSetting, newArrayList("1", "2", "3"));
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Template]", "1", "2", "3",
                "#new comment");

        updater.remove(testCase, addedSetting);
        assertThat(testCase.getTemplates()).isEmpty();

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getTemplates()).hasSize(1);
        assertThat(addedSetting).isSameAs(testCase.getTemplates().get(0));
    }

    @Test
    public void unknownSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getTeardowns()).isEmpty();

        final TestCaseUnknownSettings setting = (TestCaseUnknownSettings) updater.createSetting(testCase, 0,
                "[unknown]",
                "comment", newArrayList("a", "b", "c"));

        assertThat(testCase.getUnknownSettings()).hasSize(1);
        final TestCaseUnknownSettings addedSetting = testCase.getUnknownSettings().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETTING_UNKNOWN);

        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[unknown]", "a", "b", "c",
                "#comment");

        updater.updateComment(addedSetting, "new comment");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[unknown]", "a", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 0, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[unknown]", "x", "b", "c",
                "#new comment");

        updater.updateArgument(addedSetting, 2, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[unknown]", "x", "b", "x",
                "#new comment");

        updater.updateArgument(addedSetting, 5, "z");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[unknown]", "x", "b", "x",
                "", "", "z", "#new comment");

        updater.updateArgument(addedSetting, 3, null);
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[unknown]", "x", "b", "x",
                "", "z", "#new comment");

        updater.setArguments(addedSetting, newArrayList("1", "2", "3"));
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[unknown]", "1", "2", "3",
                "#new comment");

        updater.remove(testCase, addedSetting);
        assertThat(testCase.getUnknownSettings()).isEmpty();

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getUnknownSettings()).hasSize(1);
        assertThat(addedSetting).isSameAs(testCase.getUnknownSettings().get(0));
    }

    @Test
    public void documentationSettingOperationsTest() {
        final TestCase testCase = createCase();

        assertThat(testCase.getDocumentation()).isEmpty();

        final TestDocumentation setting = (TestDocumentation) updater.createSetting(testCase, 0, "[Documentation]",
                "comment", newArrayList("a", "b", "c"));

        assertThat(testCase.getDocumentation()).hasSize(1);
        final TestDocumentation addedSetting = testCase.getDocumentation().get(0);

        assertThat(addedSetting).isSameAs(setting);
        assertThat(addedSetting.getParent()).isSameAs(testCase);
        assertThat(addedSetting.getModelType()).isEqualTo(ModelType.TEST_CASE_DOCUMENTATION);

        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Documentation]", "a", "b",
                "c", "#comment");

        updater.updateComment(addedSetting, "new comment");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Documentation]", "a", "b",
                "c", "#new comment");

        updater.updateArgument(addedSetting, 0, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Documentation]", "x",
                "#new comment");

        updater.updateArgument(addedSetting, 2, "x");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Documentation]", "x",
                "#new comment");

        updater.updateArgument(addedSetting, 5, "z");
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Documentation]", "x",
                "#new comment");

        updater.updateArgument(addedSetting, 3, null);
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Documentation]",
                "#new comment");

        updater.setArguments(addedSetting, newArrayList("1", "2", "3"));
        assertThat(transform(addedSetting.getElementTokens(), toText())).containsExactly("[Documentation]", "1",
                "#new comment");

        updater.remove(testCase, addedSetting);
        assertThat(testCase.getDocumentation()).isEmpty();

        updater.insert(testCase, 0, addedSetting);
        assertThat(testCase.getDocumentation()).hasSize(1);
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

        assertThat(transform(row.getElementTokens(), toText())).containsExactly("action", "a", "b",
                "#comment");
        assertThat(transform(row.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordArgumentsSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final KeywordArguments keywordSetting = (KeywordArguments) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, "[Arguments]", "comment", newArrayList("a", "b", "c"));

        final TestCase testCase = createCase();

        assertThat(testCase.getUnknownSettings()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getUnknownSettings()).hasSize(1);
        final TestCaseUnknownSettings setting = testCase.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETTING_UNKNOWN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Arguments]", "a", "b",
                "c", "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION,
                RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordReturnSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final KeywordReturn keywordSetting = (KeywordReturn) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, "[Return]", "comment", newArrayList("a", "b", "c"));

        final TestCase testCase = createCase();

        assertThat(testCase.getUnknownSettings()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getUnknownSettings()).hasSize(1);
        final TestCaseUnknownSettings setting = testCase.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETTING_UNKNOWN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Return]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION,
                RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordTagsSettingIsProperlyMorphedIntoTagsSetting_whenInserted() {
        final KeywordTags keywordSetting = (KeywordTags) new KeywordTableModelUpdater().createSetting(createKeyword(),
                0, "[Tags]", "comment", newArrayList("a", "b", "c"));

        final TestCase testCase = createCase();

        assertThat(testCase.getTags()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getTags()).hasSize(1);
        final TestCaseTags setting = testCase.getTags().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_TAGS);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Tags]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION, RobotTokenType.TEST_CASE_SETTING_TAGS,
                RobotTokenType.TEST_CASE_SETTING_TAGS, RobotTokenType.TEST_CASE_SETTING_TAGS,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordTeardownSettingIsProperlyMorphedIntoTeardownSetting_whenInserted() {
        final KeywordTeardown keywordSetting = (KeywordTeardown) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, "[Teardown]", "comment", newArrayList("a", "b", "c"));

        final TestCase testCase = createCase();

        assertThat(testCase.getTeardowns()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getTeardowns()).hasSize(1);
        final TestCaseTeardown setting = testCase.getTeardowns().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_TEARDOWN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Teardown]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_SETTING_TEARDOWN, RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME,
                RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT,
                RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordTimeoutSettingIsProperlyMorphedIntoTimeoutSetting_whenInserted() {
        final KeywordTimeout keywordSetting = (KeywordTimeout) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, "[Timeout]", "comment", newArrayList("a", "b", "c"));

        final TestCase testCase = createCase();

        assertThat(testCase.getTimeouts()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getTimeouts()).hasSize(1);
        final TestCaseTimeout setting = testCase.getTimeouts().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_TIMEOUT);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Timeout]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_SETTING_TIMEOUT, RobotTokenType.TEST_CASE_SETTING_TIMEOUT_VALUE,
                RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE, RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordDocumentationSettingIsProperlyMorphedIntoDocumentationSetting_whenInserted() {
        final KeywordDocumentation keywordSetting = (KeywordDocumentation) new KeywordTableModelUpdater()
                .createSetting(createKeyword(), 0, "[Documentation]", "comment", newArrayList("a", "b", "c"));

        final TestCase testCase = createCase();

        assertThat(testCase.getDocumentation()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getDocumentation()).hasSize(1);
        final TestDocumentation setting = testCase.getDocumentation().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_DOCUMENTATION);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Documentation]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION, RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT,
                RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT,
                RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordUnknownSetupSettingIsProperlyMorphedIntoSetupSetting_whenInserted() {
        final KeywordUnknownSettings keywordSetting = new KeywordUnknownSettings(RobotToken.create("[Setup]"));
        keywordSetting.addArgument(RobotToken.create("a"));
        keywordSetting.addArgument(RobotToken.create("b"));
        keywordSetting.addArgument(RobotToken.create("c"));
        keywordSetting.setComment("comment");

        final TestCase testCase = createCase();

        assertThat(testCase.getSetups()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getSetups()).hasSize(1);
        final TestCaseSetup setting = testCase.getSetups().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETUP);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Setup]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_SETTING_SETUP, RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME,
                RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT,
                RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordUnknownTemplateSettingIsProperlyMorphedIntoTemplateSetting_whenInserted() {
        final KeywordUnknownSettings keywordSetting = new KeywordUnknownSettings(RobotToken.create("[Template]"));
        keywordSetting.addArgument(RobotToken.create("a"));
        keywordSetting.addArgument(RobotToken.create("b"));
        keywordSetting.addArgument(RobotToken.create("c"));
        keywordSetting.setComment("comment");

        final TestCase testCase = createCase();

        assertThat(testCase.getTemplates()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getTemplates()).hasSize(1);
        final TestCaseTemplate setting = testCase.getTemplates().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_TEMPLATE);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[Template]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_SETTING_TEMPLATE, RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME,
                RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT,
                RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void keywordUnknownTemplateSettingIsProperlyMorphedIntoUnknownSetting_whenInserted() {
        final KeywordUnknownSettings keywordSetting = new KeywordUnknownSettings(RobotToken.create("[something]"));
        keywordSetting.addArgument(RobotToken.create("a"));
        keywordSetting.addArgument(RobotToken.create("b"));
        keywordSetting.addArgument(RobotToken.create("c"));
        keywordSetting.setComment("comment");

        final TestCase testCase = createCase();

        assertThat(testCase.getUnknownSettings()).isEmpty();
        updater.insert(testCase, 0, keywordSetting);

        assertThat(testCase.getUnknownSettings()).hasSize(1);
        final TestCaseUnknownSettings setting = testCase.getUnknownSettings().get(0);

        assertThat(setting.getParent()).isSameAs(testCase);
        assertThat(setting.getModelType()).isEqualTo(ModelType.TEST_CASE_SETTING_UNKNOWN);

        assertThat(transform(setting.getElementTokens(), toText())).containsExactly("[something]", "a", "b", "c",
                "#comment");
        assertThat(transform(setting.getElementTokens(), toType())).containsExactly(
                RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION,
                RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS,
                RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS, RobotTokenType.START_HASH_COMMENT);
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
