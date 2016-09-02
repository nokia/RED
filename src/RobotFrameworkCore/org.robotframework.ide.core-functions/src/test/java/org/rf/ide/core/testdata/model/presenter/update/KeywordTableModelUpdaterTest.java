/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class KeywordTableModelUpdaterTest {

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

}
