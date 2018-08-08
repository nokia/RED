/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.tasks;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskFinder {

    public Task findOrCreateNearestTask(final RobotLine currentLine, final RobotFileOutput robotFileOutput) {
        final RobotFile fileModel = robotFileOutput.getFileModel();
        final TaskTable taskTable = fileModel.getTasksTable();

        final List<Task> tasksAfterLastHeader = tasksAfterLastHeader(taskTable);
        if (!tasksAfterLastHeader.isEmpty()) {
            return tasksAfterLastHeader.get(tasksAfterLastHeader.size() - 1);
        } else {
            final Task task = createArtificialTestCase(robotFileOutput, taskTable);
            taskTable.addTask(task);

            final RobotLine lineToModify = findRobotLineInModel(fileModel, task.getBeginPosition().getLine(),
                    currentLine);
            lineToModify.addLineElementAt(0, task.getName());
            return task;
        }
    }

    private List<Task> tasksAfterLastHeader(final TaskTable taskTable) {
        final List<TableHeader<? extends ARobotSectionTable>> headers = taskTable.getHeaders();
        if (!headers.isEmpty()) {
            final int lastHeaderLineNumber = headers.get(headers.size() - 1).getTableHeader().getLineNumber();

            return taskTable.getTasks()
                    .stream()
                    .filter(task -> task.getBeginPosition().getLine() > lastHeaderLineNumber)
                    .collect(toList());
        }
        return new ArrayList<>();
    }

    private Task createArtificialTestCase(final RobotFileOutput robotFileOutput, final TaskTable taskTable) {

        final List<TableHeader<? extends ARobotSectionTable>> headers = taskTable.getHeaders();
        final TableHeader<?> tableHeader = headers.get(headers.size() - 1);

        final RobotToken artificialNameToken = new RobotToken();
        artificialNameToken.setLineNumber(tableHeader.getTableHeader().getLineNumber() + 1);
        artificialNameToken.setText("");
        artificialNameToken.setStartColumn(0);
        final RobotLine robotLine = robotFileOutput.getFileModel()
                .getFileContent()
                .get(tableHeader.getTableHeader().getLineNumber() - 1);
        final IRobotLineElement endOfLine = robotLine.getEndOfLine();
        artificialNameToken.setStartOffset(endOfLine.getStartOffset() + endOfLine.getText().length());
        artificialNameToken.setType(RobotTokenType.TASK_NAME);

        return new Task(artificialNameToken);
    }

    private RobotLine findRobotLineInModel(final RobotFile fileModel, final int taskLine, final RobotLine currentLine) {
        if (currentLine.getLineNumber() != taskLine) {
            final List<RobotLine> fileContent = fileModel.getFileContent();
            for (final RobotLine line : fileContent) {
                if (taskLine == line.getLineNumber()) {
                    return line;
                }
            }
        }
        return currentLine;
    }
}
