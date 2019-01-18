/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import java.util.List;

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
import org.rf.ide.core.testdata.text.read.postfixes.PostProcessingFixActions.IPostProcessFixer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class UnknownSettingsInExecutableTablesFixer implements IPostProcessFixer {

    @Override
    public void applyFix(final RobotFileOutput parsingOutput) {
        final RobotFile fileModel = parsingOutput.getFileModel();
        final TestCaseTable testCaseTable = fileModel.getTestCaseTable();
        final TaskTable taskTable = fileModel.getTasksTable();
        final KeywordTable keywordTable = fileModel.getKeywordTable();

        if (testCaseTable.isPresent()) {
            fixInTestCaseTable(testCaseTable);
        }
        if (taskTable.isPresent()) {
            fixInTaskTable(taskTable);
        }
        if (keywordTable.isPresent()) {
            fixInUserKeywordTable(keywordTable);
        }
    }

    private void fixInUserKeywordTable(final KeywordTable keywordTable) {
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        for (final UserKeyword userKeyword : keywords) {
            final List<RobotExecutableRow<UserKeyword>> execRows = userKeyword.getExecutionContext();
            for (int i = 0; i < execRows.size(); i++) {
                final RobotExecutableRow<UserKeyword> robotExecutableRow = execRows.get(i);
                final String text = robotExecutableRow.getAction().getText().trim();
                if (text.startsWith("[") && text.endsWith("]")) {

                    final RobotToken declarationToken = robotExecutableRow.getDeclaration();
                    final List<IRobotTokenType> types = declarationToken.getTypes();
                    types.clear();
                    types.add(RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION);

                    final LocalSetting<UserKeyword> keywordUnknownSettings = new LocalSetting<>(
                            ModelType.USER_KEYWORD_SETTING_UNKNOWN, declarationToken);

                    for (final RobotToken argument : robotExecutableRow.getArguments()) {
                        keywordUnknownSettings.addToken(argument);
                    }
                    for (final RobotToken commentPart : robotExecutableRow.getComment()) {
                        keywordUnknownSettings.addCommentPart(commentPart);
                    }
                    userKeyword.replaceElement(robotExecutableRow, keywordUnknownSettings);
                }
            }
        }
    }

    private void fixInTestCaseTable(final TestCaseTable testCaseTable) {
        final List<TestCase> testCases = testCaseTable.getTestCases();
        for (final TestCase testCase : testCases) {
            final List<RobotExecutableRow<TestCase>> execRows = testCase.getExecutionContext();
            for (int i = 0; i < execRows.size(); i++) {
                final RobotExecutableRow<TestCase> robotExecutableRow = execRows.get(i);
                final String text = robotExecutableRow.getAction().getText().trim();
                if (text.startsWith("[") && text.endsWith("]")) {

                    final RobotToken declarationToken = robotExecutableRow.getDeclaration();
                    final List<IRobotTokenType> types = declarationToken.getTypes();
                    types.clear();
                    types.add(RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION);

                    final LocalSetting<TestCase> testCaseUnknownSetting = new LocalSetting<>(
                            ModelType.TEST_CASE_SETTING_UNKNOWN, declarationToken);

                    for (final RobotToken argument : robotExecutableRow.getArguments()) {
                        testCaseUnknownSetting.addToken(argument);
                    }
                    for (final RobotToken commentPart : robotExecutableRow.getComment()) {
                        testCaseUnknownSetting.addCommentPart(commentPart);
                    }
                    testCase.replaceElement(robotExecutableRow, testCaseUnknownSetting);
                }
            }
        }
    }

    private void fixInTaskTable(final TaskTable taskTable) {
        final List<Task> tasks = taskTable.getTasks();
        for (final Task task : tasks) {
            final List<RobotExecutableRow<Task>> execRows = task.getExecutionContext();
            for (int i = 0; i < execRows.size(); i++) {
                final RobotExecutableRow<Task> robotExecutableRow = execRows.get(i);
                final String text = robotExecutableRow.getAction().getText().trim();
                if (text.startsWith("[") && text.endsWith("]")) {

                    final RobotToken declarationToken = robotExecutableRow.getDeclaration();
                    final List<IRobotTokenType> types = declarationToken.getTypes();
                    types.clear();
                    types.add(RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION);

                    final LocalSetting<Task> taskUnknownSetting = new LocalSetting<>(ModelType.TASK_SETTING_UNKNOWN,
                            declarationToken);

                    for (final RobotToken argument : robotExecutableRow.getArguments()) {
                        taskUnknownSetting.addToken(argument);
                    }
                    for (final RobotToken commentPart : robotExecutableRow.getComment()) {
                        taskUnknownSetting.addCommentPart(commentPart);
                    }
                    task.replaceElement(robotExecutableRow, taskUnknownSetting);
                }
            }
        }
    }
}
