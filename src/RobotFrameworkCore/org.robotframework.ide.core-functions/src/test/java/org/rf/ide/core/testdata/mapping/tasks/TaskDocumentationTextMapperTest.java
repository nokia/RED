/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskDocumentationTextMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskDocumentationTextMapper mapper = new TaskDocumentationTextMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void mapperCannotMap_whenParserInUnkownState() {
        final TaskDocumentationTextMapper mapper = new TaskDocumentationTextMapper();

        final RobotFileOutput output = createOutputModel();
        final Stack<ParsingState> states = stack();
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCanMap_whenParserInTaskDocumentationState() {
        final TaskDocumentationTextMapper mapper = new TaskDocumentationTextMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskDocumentation(output, "doc");
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_DOCUMENTATION_DECLARATION);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void mapperCanMap_whenParserInTaskDocumentationTextState() {
        final TaskDocumentationTextMapper mapper = new TaskDocumentationTextMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskDocumentation(output, "doc1", "doc2");
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_DOCUMENTATION_TEXT);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void whenMapped_theDocumentationHasNewTextAddedAndParsingStateIsUpdated() {
        final TaskDocumentationTextMapper mapper = new TaskDocumentationTextMapper();

        final RobotFileOutput output = createOutputModel();
        final LocalSetting<Task> doc = addTaskDocumentation(output, "doc1");
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_DOCUMENTATION_TEXT);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create(""), null, "doc2");

        assertThat(doc.getTokensWithoutDeclaration()).last().isSameAs(token);
        assertThat(token.getTypes()).contains(RobotTokenType.TASK_SETTING_DOCUMENTATION_TEXT);
        assertThat(token.getText()).isEqualTo("doc2");

        assertThat(states).containsExactly(ParsingState.TASK_SETTING_DOCUMENTATION_TEXT,
                ParsingState.TASK_SETTING_DOCUMENTATION_TEXT);
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

    private static final LocalSetting<Task> addTaskDocumentation(final RobotFileOutput output,
            final String... settingCells) {
        final TaskTable table = output.getFileModel().getTasksTable();
        final Task task = table.getTasks().get(0);
        final LocalSetting<Task> doc = task.newDocumentation(task.getDocumentation().size());
        for (final String cell : settingCells) {
            doc.addToken(cell);
        }
        return doc;
    }
}
