/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTimeoutValueMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskTimeoutValueMapper mapper = new TaskTimeoutValueMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void mapperCannotMap_whenParserNotInTaskTimeoutState() {
        final TaskTimeoutValueMapper mapper = new TaskTimeoutValueMapper();

        final RobotFileOutput output = createOutputModel();
        final Stack<ParsingState> states = stack();
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCannotMap_whenParserInTaskTimeoutStateButLastTaskTimeoutHasValueAlready() {
        final TaskTimeoutValueMapper mapper = new TaskTimeoutValueMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTimeout(output);
        addTaskTimeout(output, "val");
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_TASK_TIMEOUT);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCanMap_whenParserInTaskTimeoutStateAndLastTaskTimeoutHasNoValueYet() {
        final TaskTimeoutValueMapper mapper = new TaskTimeoutValueMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTimeout(output, "val", "msg1", "msg2");
        addTaskTimeout(output);
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_TASK_TIMEOUT);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void whenMapped_theTimeoutHasNewValueAddedAndParsingStateIsUpdated() {
        final TaskTimeoutValueMapper mapper = new TaskTimeoutValueMapper();

        final RobotFileOutput output = createOutputModel();
        final LocalSetting<Task> timeout = addTaskTimeout(output);
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_TASK_TIMEOUT);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create(""), null, "value");

        assertThat(timeout.getTokensWithoutDeclaration()).containsExactly(token);
        assertThat(token.getTypes()).contains(RobotTokenType.TASK_SETTING_TIMEOUT_VALUE);
        assertThat(token.getText()).isEqualTo("value");

        assertThat(states).containsExactly(ParsingState.TASK_SETTING_TASK_TIMEOUT,
                ParsingState.TASK_SETTING_TASK_TIMEOUT_VALUE);
    }

    private static Stack<ParsingState> stack(final ParsingState... states) {
        final Stack<ParsingState> statesStack = new Stack<>();
        for (final ParsingState state : states) {
            statesStack.push(state);
        }
        return statesStack;
    }

    private static final RobotFileOutput createOutputModel() {
        final RobotFileOutput output = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile fileModel = output.getFileModel();
        fileModel.includeTaskTableSection();
        fileModel.getTasksTable().createTask("task");
        return output;
    }

    private static final LocalSetting<Task> addTaskTimeout(final RobotFileOutput output, final String... settingCells) {
        final TaskTable table = output.getFileModel().getTasksTable();
        final Task task = table.getTasks().get(0);
        final LocalSetting<Task> timeout = task.newTimeout(task.getTimeouts().size());
        for (final String cell : settingCells) {
            timeout.addToken(cell);
        }
        return timeout;
    }
}
