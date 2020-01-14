/*
 * Copyright 2020 Nokia Solutions and Networks
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
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskNameMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskNameMapper mapper = new TaskNameMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void whenMapped_newTaskIsCreatedWithSingleTokenTypeAndParsingStateIsUpdated_inRf31() {
        final TaskNameMapper mapper = new TaskNameMapper();

        final RobotFileOutput output = createOutputModel(new RobotVersion(3, 1));
        final Stack<ParsingState> states = stack(ParsingState.TASKS_TABLE_INSIDE);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create("", RobotTokenType.VARIABLE_USAGE),
                null, "task ${var}");

        final Task task = getTask(output);
        assertThat(task.getDeclaration()).isSameAs(token);
        assertThat(token.getTypes()).containsOnly(RobotTokenType.TASK_NAME);
        assertThat(token.getText()).isEqualTo("task ${var}");

        assertThat(states).containsExactly(ParsingState.TASKS_TABLE_INSIDE, ParsingState.TASK_DECLARATION);
    }

    @Test
    public void whenMapped_newTaskIsCreatedWithPreviousTokenTypesAndParsingStateIsUpdated_inRf32() {
        final TaskNameMapper mapper = new TaskNameMapper();

        final RobotFileOutput output = createOutputModel(new RobotVersion(3, 2));
        final Stack<ParsingState> states = stack(ParsingState.TASKS_TABLE_INSIDE);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create("", RobotTokenType.VARIABLE_USAGE),
                null, "task ${var}");

        final Task task = getTask(output);
        assertThat(task.getDeclaration()).isSameAs(token);
        assertThat(token.getTypes()).containsOnly(RobotTokenType.TASK_NAME, RobotTokenType.VARIABLE_USAGE);
        assertThat(token.getText()).isEqualTo("task ${var}");

        assertThat(states).containsExactly(ParsingState.TASKS_TABLE_INSIDE, ParsingState.TASK_DECLARATION);
    }

    private static Stack<ParsingState> stack(final ParsingState... states) {
        final Stack<ParsingState> statesStack = new Stack<>();
        for (final ParsingState state : states) {
            statesStack.push(state);
        }
        return statesStack;
    }

    private static final RobotFileOutput createOutputModel(final RobotVersion version) {
        final RobotFileOutput output = new RobotFileOutput(version);
        final RobotFile fileModel = output.getFileModel();
        fileModel.includeTaskTableSection();
        return output;
    }

    private Task getTask(final RobotFileOutput output) {
        return output.getFileModel().getTasksTable().getTasks().get(0);
    }
}
