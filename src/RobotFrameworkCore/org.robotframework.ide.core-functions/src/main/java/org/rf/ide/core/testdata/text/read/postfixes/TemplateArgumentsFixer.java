/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import java.util.List;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.postfixes.PostProcessingFixActions.IPostProcessFixer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

class TemplateArgumentsFixer implements IPostProcessFixer {

    @Override
    public void applyFix(final RobotFileOutput parsingOutput) {
        final RobotFile model = parsingOutput.getFileModel();

        final TestCaseTable testCaseTable = model.getTestCaseTable();
        if (testCaseTable.isPresent()) {
            for (final TestCase testCase : testCaseTable.getTestCases()) {
                if (testCase.getTemplateKeywordName().isPresent()) {
                    updateRows(testCase.getExecutionContext(), RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
                }
            }
        }

        final TaskTable tasksTable = model.getTasksTable();
        if (tasksTable.isPresent()) {
            for (final Task task : tasksTable.getTasks()) {
                if (task.getTemplateKeywordName().isPresent()) {
                    updateRows(task.getExecutionContext(), RobotTokenType.TASK_TEMPLATE_ARGUMENT);
                }
            }
        }
    }

    private static <T> void updateRows(final List<RobotExecutableRow<T>> rows,
            final RobotTokenType templateArgumentType) {
        for (final RobotExecutableRow<?> executableRow : rows) {
            if (!isForDeclarationRow(executableRow)) {
                for (final RobotToken robotToken : executableRow.getElementTokens()) {
                    if (isTemplateArgument(robotToken)) {
                        robotToken.getTypes().add(templateArgumentType);
                    }
                }
            }
        }
    }

    private static <T> boolean isForDeclarationRow(final RobotExecutableRow<T> row) {
        return row.getAction().getTypes().contains(RobotTokenType.FOR_TOKEN)
                || row.getAction().getTypes().contains(RobotTokenType.FOR_END_TOKEN);
    }

    private static boolean isTemplateArgument(final RobotToken robotToken) {
        return !robotToken.getTypes().contains(RobotTokenType.FOR_CONTINUE_TOKEN)
                && !robotToken.getTypes().contains(RobotTokenType.FOR_WITH_END_CONTINUATION)
                && !robotToken.getTypes().contains(RobotTokenType.START_HASH_COMMENT);
    }
}
