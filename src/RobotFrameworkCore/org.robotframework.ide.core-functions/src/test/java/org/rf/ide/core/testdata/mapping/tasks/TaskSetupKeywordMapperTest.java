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

public class TaskSetupKeywordMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskSetupKeywordMapper mapper = new TaskSetupKeywordMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void mapperCannotMap_whenParserInUnknownState() {
        final TaskSetupKeywordMapper mapper = new TaskSetupKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        final Stack<ParsingState> states = stack();
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCannotMap_whenParserInTaskSetupStateButLastTaskSetupHasKeywordAlready() {
        final TaskSetupKeywordMapper mapper = new TaskSetupKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskSetup(output);
        addTaskSetup(output, "kw");
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_SETUP);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCanMap_whenParserInTaskSetupStateAndLastTaskSetupHasNoKeywordYet() {
        final TaskSetupKeywordMapper mapper = new TaskSetupKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskSetup(output, "kw", "arg1", "arg2");
        addTaskSetup(output);
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_SETUP);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void whenMapped_theSetupHasNewKeywordAddedAndParsingStateIsUpdated() {
        final TaskSetupKeywordMapper mapper = new TaskSetupKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        final LocalSetting<Task> setup = addTaskSetup(output);
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_SETUP);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create(""), null, "keyword");

        assertThat(setup.getTokensWithoutDeclaration()).containsExactly(token);
        assertThat(token.getTypes()).contains(RobotTokenType.TASK_SETTING_SETUP_KEYWORD_NAME);
        assertThat(token.getText()).isEqualTo("keyword");

        assertThat(states).containsExactly(ParsingState.TASK_SETTING_SETUP,
                ParsingState.TASK_SETTING_SETUP_KEYWORD);
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

    private static final LocalSetting<Task> addTaskSetup(final RobotFileOutput output, final String... settingCells) {
        final TaskTable table = output.getFileModel().getTasksTable();
        final Task task = table.getTasks().get(0);
        final LocalSetting<Task> setup = task.newSetup(task.getSetups().size());
        for (final String cell : settingCells) {
            setup.addToken(cell);
        }
        return setup;
    }
}
