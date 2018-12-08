/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class EndTerminatedForLoopsFixer implements IPostProcessFixAction {

    @Override
    public void applyFix(final RobotFileOutput parsingOutput) {
        final RobotFile fileModel = parsingOutput.getFileModel();
        final TestCaseTable testCaseTable = fileModel.getTestCaseTable();
        final TaskTable tasksTable = fileModel.getTasksTable();
        final KeywordTable keywordTable = fileModel.getKeywordTable();

        if (testCaseTable.isPresent()) {
            final List<TestCase> testCases = testCaseTable.getTestCases();
            for (final TestCase execUnit : testCases) {
                fixExecutables(execUnit);
            }
        }

        if (tasksTable.isPresent()) {
            final List<Task> tasks = tasksTable.getTasks();
            for (final Task execUnit : tasks) {
                fixExecutables(execUnit);
            }
        }

        if (keywordTable.isPresent()) {
            final List<UserKeyword> keywords = keywordTable.getKeywords();
            for (final UserKeyword execUnit : keywords) {
                fixExecutables(execUnit);
            }
        }
    }

    private <T extends AModelElement<? extends ARobotSectionTable>> void fixExecutables(
            final IExecutableStepsHolder<T> execUnit) {
        final List<RobotExecutableRow<T>> executionContext = execUnit.getExecutionContext();
        
        final List<RobotExecutableRow<T>> toMark = new ArrayList<>();
        boolean forDetected = false;
        for (int i = 0; i < executionContext.size();i++) {
            final RobotExecutableRow<T> row = executionContext.get(i);
            if (row.getAction().getTypes().contains(RobotTokenType.FOR_TOKEN) && "FOR".equals(row.getAction().getText())) {
                if (forDetected) {
                    toMark.clear();
                }
                forDetected = true;

            } else if (row.getAction().getTypes().contains(RobotTokenType.FOR_END_TOKEN)) {
                if (!forDetected) {
                    row.getAction().getTypes().remove(RobotTokenType.FOR_END_TOKEN);
                } else {
                    if (row.getArguments().isEmpty()) {
                        for (final RobotExecutableRow<T> r : toMark) {
                            final FilePosition fp = r.getAction().getFilePosition().copy();
                            r.insertValueAt("", 0);
                            r.getAction().setFilePosition(fp);
                            r.getAction().getTypes().add(RobotTokenType.FOR_WITH_END_CONTINUATION);
                        }
                        forDetected = false;
                    } else {
                        row.getAction().getTypes().remove(RobotTokenType.FOR_END_TOKEN);
                    }
                }

                toMark.clear();
            } else if (forDetected) {
                toMark.add(row);
            }
        }
    }
}
