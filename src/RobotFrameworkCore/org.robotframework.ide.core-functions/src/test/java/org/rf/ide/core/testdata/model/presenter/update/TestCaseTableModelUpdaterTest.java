package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
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
    public void handlersForKeywordCannotCreateAnything() {
        final TestCase testCase = mock(TestCase.class);
        for (final ModelType kwModelType : keywordModelTypes) {
            final ITestCaseTableElementOperation handler = updater.getOperationHandler(kwModelType);

            try {
                handler.create(testCase, "action", newArrayList("1", "2"), "");
                fail("Expected exception");
            } catch (final IllegalStateException e) {
                // we expected that
            }
        }
        verifyZeroInteractions(testCase);
    }

    @Test
    public void handlersForKeywordCannotUpdateAnything() {
        final AModelElement<?> element = mock(AModelElement.class);
        for (final ModelType kwModelType : keywordModelTypes) {
            final ITestCaseTableElementOperation handler = updater.getOperationHandler(kwModelType);

            try {
                handler.update(element, 1, "value");
                fail("Expected exception");
            } catch (final IllegalStateException e) {
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
            final ITestCaseTableElementOperation handler = updater.getOperationHandler(kwModelType);

            try {
                handler.remove(testCase, element);
                fail("Expected exception");
            } catch (final IllegalStateException e) {
                // we expected that
            }
        }
        verifyZeroInteractions(testCase, element);
    }
}
