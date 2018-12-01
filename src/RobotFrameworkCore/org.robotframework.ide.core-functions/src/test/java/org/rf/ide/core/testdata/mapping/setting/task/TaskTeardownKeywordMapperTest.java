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
import org.rf.ide.core.testdata.model.table.setting.TaskTeardown;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTeardownKeywordMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskTeardownKeywordMapper mapper = new TaskTeardownKeywordMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void mapperCannotMap_whenParserNotInTaskTeardownState() {
        final TaskTeardownKeywordMapper mapper = new TaskTeardownKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        final Stack<ParsingState> states = stack();
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCannotMap_whenParserInTaskTeardownStateButLastTaskTeardownHasKeywordAlready() {
        final TaskTeardownKeywordMapper mapper = new TaskTeardownKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTeardown(output);
        addTaskTeardown(output, "kw");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TEARDOWN);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCanMap_whenParserInTaskTeardownStateAndLastTaskTeardownHasNoKeywordYet() {
        final TaskTeardownKeywordMapper mapper = new TaskTeardownKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTeardown(output, "kw", "arg1", "arg2");
        addTaskTeardown(output);
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TEARDOWN);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void whenMapped_theTeardownHasNewKeywordAddedAndParsingStateIsUpdated() {
        final TaskTeardownKeywordMapper mapper = new TaskTeardownKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        final TaskTeardown teardown = addTaskTeardown(output);
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TEARDOWN);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create(""), null, "keyword");

        assertThat(teardown.getKeywordName()).isSameAs(token);
        assertThat(token.getTypes()).contains(RobotTokenType.SETTING_TASK_TEARDOWN_KEYWORD_NAME);
        assertThat(token.getText()).isEqualTo("keyword");

        assertThat(states).containsExactly(ParsingState.SETTING_TASK_TEARDOWN,
                ParsingState.SETTING_TASK_TEARDOWN_KEYWORD);
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

    private static final TaskTeardown addTaskTeardown(final RobotFileOutput output, final String... settingCells) {
        final SettingTable table = output.getFileModel().getSettingTable();
        final TaskTeardown teardown = table.newTaskTeardown();
        for (int i = 0; i < settingCells.length; i++) {
            if (i == 0) {
                teardown.setKeywordName(settingCells[i]);
            } else {
                teardown.addArgument(settingCells[i]);
            }
        }
        return teardown;
    }
}
