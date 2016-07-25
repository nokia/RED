/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class KeywordTableModelUpdaterTest {

    private static KeywordTable keywordTable;

    private static KeywordTableModelUpdater modelUpdater;

    @BeforeClass
    public static void setup() {
        RobotFile model = NewRobotFileTestHelper.getModelFileToModify("2.9");
        model.includeKeywordTableSection();
        keywordTable = model.getKeywordTable();

        modelUpdater = new KeywordTableModelUpdater();
    }

    @Test
    public void testArgumentsCRUD() {
        final String keywordSettingName = "[Arguments]";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";

        final UserKeyword userKeyword = keywordTable.createUserKeyword("UserKeyword");

        final AModelElement<?> modelElement = modelUpdater.create(userKeyword, keywordSettingName, comment,
                settingArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_ARGUMENTS);
        KeywordArguments setting = (KeywordArguments) modelElement;

        checkSetting(setting.getArguments(), settingArgs, setting.getComment(), comment);

        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.update(setting, 1, newArg3);
        modelUpdater.update(setting, 2, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getArguments(), settingArgs, setting.getComment(), newComment);

        assertFalse(userKeyword.getArguments().isEmpty());
        modelUpdater.remove(userKeyword, modelElement);
        assertTrue(userKeyword.getArguments().isEmpty());
    }

    @Test
    public void testDocumentationCRUD() {
        final String keywordSettingName = "[Documentation]";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";

        final UserKeyword userKeyword = keywordTable.createUserKeyword("UserKeyword");

        final AModelElement<?> modelElement = modelUpdater.create(userKeyword, keywordSettingName, comment,
                settingArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_DOCUMENTATION);
        KeywordDocumentation setting = (KeywordDocumentation) modelElement;

        checkSetting(setting.getDocumentationText(), settingArgs, setting.getComment(), comment);

        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.update(setting, 1, newArg3);
        modelUpdater.update(setting, 2, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getDocumentationText(), settingArgs, setting.getComment(), newComment);

        assertFalse(userKeyword.getDocumentation().isEmpty());
        modelUpdater.remove(userKeyword, modelElement);
        assertTrue(userKeyword.getDocumentation().isEmpty());
    }

    @Test
    public void testTagsCRUD() {
        final String keywordSettingName = "[Tags]";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";

        final UserKeyword userKeyword = keywordTable.createUserKeyword("UserKeyword");

        final AModelElement<?> modelElement = modelUpdater.create(userKeyword, keywordSettingName, comment,
                settingArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_TAGS);
        KeywordTags setting = (KeywordTags) modelElement;

        checkSetting(setting.getTags(), settingArgs, setting.getComment(), comment);

        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.update(setting, 1, newArg3);
        modelUpdater.update(setting, 2, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getTags(), settingArgs, setting.getComment(), newComment);

        assertFalse(userKeyword.getTags().isEmpty());
        modelUpdater.remove(userKeyword, modelElement);
        assertTrue(userKeyword.getTags().isEmpty());
    }

    @Test
    public void testTimeoutCRUD() {
        final String keywordSettingName = "[Timeout]";
        final String timeout = "2 seconds";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final List<String> args = newArrayList(timeout);
        args.addAll(settingArgs);
        final String comment = "comment";

        final UserKeyword userKeyword = keywordTable.createUserKeyword("UserKeyword");

        final AModelElement<?> modelElement = modelUpdater.create(userKeyword, keywordSettingName, comment, args);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_TIMEOUT);
        KeywordTimeout setting = (KeywordTimeout) modelElement;

        checkSetting(setting.getTimeout(), timeout, setting.getMessage(), settingArgs, setting.getComment(), comment);

        final String newTimeout = "3 seconds";
        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newTimeout);
        modelUpdater.update(setting, 2, newArg3);
        modelUpdater.update(setting, 3, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getTimeout(), newTimeout, setting.getMessage(), settingArgs, setting.getComment(),
                newComment);

        assertFalse(userKeyword.getTimeouts().isEmpty());
        modelUpdater.remove(userKeyword, modelElement);
        assertTrue(userKeyword.getTimeouts().isEmpty());
    }

    @Test
    public void testTeardownCRUD() {
        final String keywordSettingName = "[Teardown]";
        final String teardown = "teardown";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final List<String> args = newArrayList(teardown);
        args.addAll(settingArgs);
        final String comment = "comment";

        final UserKeyword userKeyword = keywordTable.createUserKeyword("UserKeyword");

        final AModelElement<?> modelElement = modelUpdater.create(userKeyword, keywordSettingName, comment, args);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_TEARDOWN);
        KeywordTeardown setting = (KeywordTeardown) modelElement;

        checkSetting(setting.getKeywordName(), teardown, setting.getArguments(), settingArgs, setting.getComment(),
                comment);

        final String newTeardown = "teardown2";
        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newTeardown);
        modelUpdater.update(setting, 2, newArg3);
        modelUpdater.update(setting, 3, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getKeywordName(), newTeardown, setting.getArguments(), settingArgs, setting.getComment(),
                newComment);

        assertFalse(userKeyword.getTeardowns().isEmpty());
        modelUpdater.remove(userKeyword, modelElement);
        assertTrue(userKeyword.getTeardowns().isEmpty());
    }

    @Test
    public void testReturnCRUD() {
        final String keywordSettingName = "[Return]";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";

        final UserKeyword userKeyword = keywordTable.createUserKeyword("UserKeyword");

        final AModelElement<?> modelElement = modelUpdater.create(userKeyword, keywordSettingName, comment,
                settingArgs);

        assertTrue(modelElement.getModelType() == ModelType.USER_KEYWORD_RETURN);
        KeywordReturn setting = (KeywordReturn) modelElement;

        checkSetting(setting.getReturnValues(), settingArgs, setting.getComment(), comment);

        final String newArg3 = "arg3";
        settingArgs.set(1, newArg3);
        final String newArg4 = "arg4";
        settingArgs.add(newArg4);
        final String newComment = "new comment";

        modelUpdater.update(setting, 1, newArg3);
        modelUpdater.update(setting, 2, newArg4);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getReturnValues(), settingArgs, setting.getComment(), newComment);

        assertFalse(userKeyword.getReturns().isEmpty());
        modelUpdater.remove(userKeyword, modelElement);
        assertTrue(userKeyword.getReturns().isEmpty());
    }

    @Test
    public void testCreateWhenNoTableExists() {
        assertNull(modelUpdater.create(null, "Arguments", "", newArrayList("")));
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

    enum KeywordSettingName {
        ARGUMENTS("Arguments"),
        DOCUMENTATION("Documentation"),
        TAGS("Tags"),
        TIMEOUT("Timeout"),
        RETURN("Return"),
        TEARDOWN("Teardown");

        private String name;

        private KeywordSettingName(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
