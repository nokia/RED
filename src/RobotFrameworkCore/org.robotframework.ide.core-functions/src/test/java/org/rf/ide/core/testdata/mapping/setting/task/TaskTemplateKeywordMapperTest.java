package org.rf.ide.core.testdata.mapping.setting.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTemplateKeywordMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskTemplateKeywordMapper mapper = new TaskTemplateKeywordMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void mapperCannotMap_whenParserNotInTaskTemplateState() {
        final TaskTemplateKeywordMapper mapper = new TaskTemplateKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        final Stack<ParsingState> states = stack();
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCannotMap_whenParserInTaskTemplateStateButLastTaskTemplateHasKeywordAlready() {
        final TaskTemplateKeywordMapper mapper = new TaskTemplateKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTemplate(output);
        addTaskTemplate(output, "kw");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TEMPLATE);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCanMap_whenParserInTaskTemplateStateAndLastTaskTemplateHasNoKeywordYet() {
        final TaskTemplateKeywordMapper mapper = new TaskTemplateKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskTemplate(output, "kw", "arg1", "arg2");
        addTaskTemplate(output);
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TEMPLATE);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void whenMapped_theTemplateHasNewKeywordAddedAndParsingStateIsUpdated() {
        final TaskTemplateKeywordMapper mapper = new TaskTemplateKeywordMapper();

        final RobotFileOutput output = createOutputModel();
        final TaskTemplate template = addTaskTemplate(output);
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_TEMPLATE);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create(""), null, "keyword");

        assertThat(template.getKeywordName()).isSameAs(token);
        assertThat(token.getTypes()).contains(RobotTokenType.SETTING_TASK_TEMPLATE_KEYWORD_NAME);
        assertThat(token.getText()).isEqualTo("keyword");

        assertThat(states).containsExactly(ParsingState.SETTING_TASK_TEMPLATE,
                ParsingState.SETTING_TASK_TEMPLATE_KEYWORD);
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

    private static final TaskTemplate addTaskTemplate(final RobotFileOutput output, final String... settingCells) {
        final SettingTable table = output.getFileModel().getSettingTable();
        final TaskTemplate template = table.newTaskTemplate();
        for (int i = 0; i < settingCells.length; i++) {
            if (i == 0) {
                template.setKeywordName(settingCells[i]);
            } else {
                template.addUnexpectedTrashArgument(settingCells[i]);
            }
        }
        return template;
    }
}
