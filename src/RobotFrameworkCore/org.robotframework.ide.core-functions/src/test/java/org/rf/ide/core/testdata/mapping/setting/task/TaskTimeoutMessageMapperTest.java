/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTimeout;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTimeoutMessageMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskTimeoutMessageMapper mapper = new TaskTimeoutMessageMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void mapperCannotMap_whenParserNotInTaskTimeoutState() {
        final TaskTimeoutMessageMapper mapper = new TaskTimeoutMessageMapper();

        final RobotFileOutput output = createOutputModel();
        final Stack<ParsingState> states = stack();
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCannotMap_whenParserInTaskTimeoutStateAndLastTaskTimeoutHasNoValueYet() {
        final TaskTimeoutMessageMapper mapper = new TaskTimeoutMessageMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTimeout(output, "val");
        addTaskTimeout(output);
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TIMEOUT);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCanMap_whenParserInTaskTimeoutStateButLastTaskTimeoutHasValueAlready() {
        final TaskTimeoutMessageMapper mapper = new TaskTimeoutMessageMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTimeout(output);
        addTaskTimeout(output, "val");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TIMEOUT);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void mapperCanMap_whenParserInTaskTimeoutValueState() {
        final TaskTimeoutMessageMapper mapper = new TaskTimeoutMessageMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTimeout(output, "val");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TIMEOUT_VALUE);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void mapperCanMap_whenParserInTaskTimeoutMessageState() {
        final TaskTimeoutMessageMapper mapper = new TaskTimeoutMessageMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTimeout(output, "val", "msg1");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TIMEOUT_MESSAGE_ARGUMENTS);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void whenMapped_theTimouetHasNewValueAddedAndParsingStateIsUpdated() {
        final TaskTimeoutMessageMapper mapper = new TaskTimeoutMessageMapper();

        final RobotFileOutput output = createOutputModel();
        final TaskTimeout timeout = addTaskTimeout(output, "val");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TIMEOUT);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create(""), null, "msg");

        assertThat(timeout.getMessageArguments()).containsExactly(token);
        assertThat(token.getTypes()).contains(RobotTokenType.SETTING_TASK_TIMEOUT_MESSAGE);
        assertThat(token.getText()).isEqualTo("msg");

        assertThat(states).containsExactly(ParsingState.SETTING_TASK_TIMEOUT,
                ParsingState.SETTING_TASK_TIMEOUT_MESSAGE_ARGUMENTS);
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
        fileModel.includeSettingTableSection();
        return output;
    }

    private static final TaskTimeout addTaskTimeout(final RobotFileOutput output, final String... settingCells) {
        final SettingTable table = output.getFileModel().getSettingTable();
        final TaskTimeout timeout = table.newTaskTimeout();
        for (int i = 0; i < settingCells.length; i++) {
            if (i == 0) {
                timeout.setTimeout(settingCells[i]);
            } else {
                timeout.addMessageArgument(settingCells[i]);
            }
        }
        return timeout;
    }
}
