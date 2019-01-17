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
import org.rf.ide.core.testdata.text.read.postfixes.PostProcessingFixActions.IPostProcessFixer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


class EndTerminatedForLoopsFixer implements IPostProcessFixer {

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
        RobotExecutableRow<T> forDeclarationRow = null;

        for (final RobotExecutableRow<T> row : executionContext) {
            if (isForDeclarationRow(row)) {
                if (forDeclarationRow != null) {
                    toMark.clear();
                }
                forDeclarationRow = row;

            } else if (isForEndRow(row)) {
                if (forDeclarationRow == null) {
                    removeType(row, RobotTokenType.FOR_END_TOKEN);

                } else {
                    if (row.getArguments().isEmpty()) {
                        addType(forDeclarationRow, RobotTokenType.FOR_WITH_END);
                        for (final RobotExecutableRow<T> r : toMark) {
                            final FilePosition fp = r.getAction().getFilePosition().copy();
                            r.insertValueAt("", 0);
                            r.getAction().setFilePosition(fp);
                            addType(r, RobotTokenType.FOR_WITH_END_CONTINUATION);
                        }
                    } else {
                        removeType(row, RobotTokenType.FOR_END_TOKEN);
                    }
                }
                toMark.clear();
                forDeclarationRow = null;

            } else if (forDeclarationRow != null && isIndentedForContinuationRow(row)) {
                forDeclarationRow = null;
                toMark.clear();

            } else if (forDeclarationRow != null && !isIndentedForContinuationRow(row)) {
                toMark.add(row);
            }
        }
        
        if (forDeclarationRow != null && executionContext.get(executionContext.size() - 1) == forDeclarationRow
                && forDeclarationRow.getAction().getText().equals("FOR")) {
            addType(forDeclarationRow, RobotTokenType.FOR_WITH_END);
        }
    }

    private static <T> boolean isForDeclarationRow(final RobotExecutableRow<T> row) {
        return row.getAction().getTypes().contains(RobotTokenType.FOR_TOKEN);
    }

    private static <T> boolean isIndentedForContinuationRow(final RobotExecutableRow<T> row) {
        return row.getAction().getTypes().contains(RobotTokenType.FOR_CONTINUE_TOKEN)
                || row.getAction().getTypes().contains(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN);
    }

    private static <T> boolean isForEndRow(final RobotExecutableRow<T> row) {
        return row.getAction().getTypes().contains(RobotTokenType.FOR_END_TOKEN);
    }

    private static <T> boolean addType(final RobotExecutableRow<T> row, final RobotTokenType type) {
        return row.getAction().getTypes().add(type);
    }

    private static <T> boolean removeType(final RobotExecutableRow<T> row, final RobotTokenType type) {
        return row.getAction().getTypes().remove(type);
    }
}
