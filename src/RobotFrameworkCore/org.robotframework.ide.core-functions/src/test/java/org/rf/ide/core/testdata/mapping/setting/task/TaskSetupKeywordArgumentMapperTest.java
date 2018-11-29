package org.rf.ide.core.testdata.mapping.setting.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskSetup;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskSetupKeywordArgumentMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskSetupKeywordArgumentMapper mapper = new TaskSetupKeywordArgumentMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void mapperCannotMap_whenParserNotInTaskSetupState() {
        final TaskSetupKeywordArgumentMapper mapper = new TaskSetupKeywordArgumentMapper();

        final RobotFileOutput output = createOutputModel();
        final Stack<ParsingState> states = stack();
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCannotMap_whenParserInTaskSetupStateButLastTaskSetupHasNoKeywordYet() {
        final TaskSetupKeywordArgumentMapper mapper = new TaskSetupKeywordArgumentMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskSetup(output, "kw");
        addTaskSetup(output);
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_SETUP);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isFalse();
    }

    @Test
    public void mapperCanMap_whenParserInTaskSetupStateAndLastTaskSetupHasKeywordAlready() {
        final TaskSetupKeywordArgumentMapper mapper = new TaskSetupKeywordArgumentMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskSetup(output);
        addTaskSetup(output, "kw");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_SETUP);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void mapperCanMap_whenParserInTaskSetupKeywordState() {
        final TaskSetupKeywordArgumentMapper mapper = new TaskSetupKeywordArgumentMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskSetup(output, "kw");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_SETUP_KEYWORD);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void mapperCanMap_whenParserInTaskSetupKeywordArgumentState() {
        final TaskSetupKeywordArgumentMapper mapper = new TaskSetupKeywordArgumentMapper();

        final RobotFileOutput output = createOutputModel();
        addTaskSetup(output, "kw", "arg1");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_SETUP_KEYWORD_ARGUMENT);
        assertThat(mapper.checkIfCanBeMapped(output, null, null, null, states)).isTrue();
    }

    @Test
    public void whenMapped_theSetupHasNewKeywordArgumentAddedAndParsingStateIsUpdated() {
        final TaskSetupKeywordArgumentMapper mapper = new TaskSetupKeywordArgumentMapper();

        final RobotFileOutput output = createOutputModel();
        final TaskSetup setup = addTaskSetup(output, "kw");
        final Stack<ParsingState> states = stack(ParsingState.SETTING_TASK_SETUP);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create(""), null, "arg1");

        assertThat(setup.getArguments()).containsExactly(token);
        assertThat(token.getTypes()).contains(RobotTokenType.SETTING_TASK_SETUP_KEYWORD_ARGUMENT);
        assertThat(token.getText()).isEqualTo("arg1");

        assertThat(states).containsExactly(ParsingState.SETTING_TASK_SETUP,
                ParsingState.SETTING_TASK_SETUP_KEYWORD_ARGUMENT);
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

    private static final TaskSetup addTaskSetup(final RobotFileOutput output, final String... settingCells) {
        final SettingTable table = output.getFileModel().getSettingTable();
        final TaskSetup setup = table.newTaskSetup();
        for (int i = 0; i < settingCells.length; i++) {
            if (i == 0) {
                setup.setKeywordName(settingCells[i]);
            } else {
                setup.addArgument(settingCells[i]);
            }
        }
        return setup;
    }
}
