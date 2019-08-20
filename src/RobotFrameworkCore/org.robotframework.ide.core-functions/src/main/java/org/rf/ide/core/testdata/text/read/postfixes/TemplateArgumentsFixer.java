/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.postfixes.PostProcessingFixActions.IPostProcessFixer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

class TemplateArgumentsFixer implements IPostProcessFixer {

    @Override
    public void applyFix(final RobotFileOutput parsingOutput) {
        final RobotFile model = parsingOutput.getFileModel();

        final TestCaseTable testCaseTable = model.getTestCaseTable();
        if (testCaseTable.isPresent()) {
            for (final TestCase testCase : testCaseTable.getTestCases()) {
                final boolean isTemplateUsed = testCase.getTemplateKeywordName().isPresent();
                for (final RobotExecutableRow<?> row : testCase.getExecutionContext()) {
                    row.fixTemplateArgumentsTypes(isTemplateUsed, RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
                }
            }
        }

        final TaskTable tasksTable = model.getTasksTable();
        if (tasksTable.isPresent()) {
            for (final Task task : tasksTable.getTasks()) {
                final boolean isTemplateUsed = task.getTemplateKeywordName().isPresent();
                for (final RobotExecutableRow<?> row : task.getExecutionContext()) {
                    row.fixTemplateArgumentsTypes(isTemplateUsed, RobotTokenType.TASK_TEMPLATE_ARGUMENT);
                }
            }
        }
    }
}
