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

public class TaskTagsTagNameMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskTagsTagNameMapper mapper = new TaskTagsTagNameMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void mapperCannotMap_whenParserInUnkownState() {
        final TaskTagsTagNameMapper mapper = new TaskTagsTagNameMapper();

        final RobotFileOutput output = createOutputModel();
        final Stack<ParsingState> states = stack();
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCanMap_whenParserInTaskTagsState() {
        final TaskTagsTagNameMapper mapper = new TaskTagsTagNameMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTags(output, "tag");
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_TAGS);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void mapperCanMap_whenParserInTaskTagsTagState() {
        final TaskTagsTagNameMapper mapper = new TaskTagsTagNameMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTags(output, "tag1", "tag2");
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_TAGS_TAG_NAME);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void whenMapped_theDocumentationHasNewTextAddedAndParsingStateIsUpdated() {
        final TaskTagsTagNameMapper mapper = new TaskTagsTagNameMapper();

        final RobotFileOutput output = createOutputModel();
        final LocalSetting<Task> doc = addTaskTags(output, "tag1");
        final Stack<ParsingState> states = stack(ParsingState.TASK_SETTING_TAGS);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create(""), null, "tag2");

        assertThat(doc.getTokensWithoutDeclaration()).last().isSameAs(token);
        assertThat(token.getTypes()).contains(RobotTokenType.TASK_SETTING_TAGS);
        assertThat(token.getText()).isEqualTo("tag2");

        assertThat(states).containsExactly(ParsingState.TASK_SETTING_TAGS, ParsingState.TASK_SETTING_TAGS_TAG_NAME);
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

    private static final LocalSetting<Task> addTaskTags(final RobotFileOutput output,
            final String... settingCells) {
        final TaskTable table = output.getFileModel().getTasksTable();
        final Task task = table.getTasks().get(0);
        final LocalSetting<Task> tags = task.newTags(task.getTags().size());
        for (final String cell : settingCells) {
            tags.addToken(cell);
        }
        return tags;
    }
}
