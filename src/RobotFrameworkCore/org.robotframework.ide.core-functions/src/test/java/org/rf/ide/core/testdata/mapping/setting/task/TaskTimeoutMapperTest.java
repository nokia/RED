package org.rf.ide.core.testdata.mapping.setting.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTimeoutMapperTest {

    @Test
    public void theMapperIsOnlyUsedForRobotNewerThan31() {
        final TaskTimeoutMapper mapper = new TaskTimeoutMapper();

        assertThat(mapper.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 0, 1))).isFalse();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(mapper.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void theMapperCorrectlyAddsSetting_ifItDoesNotExist() {
        final TaskTimeoutMapper mapper = new TaskTimeoutMapper();
        
        final SettingTable settingTable = new SettingTable(null);

        assertThat(settingTable.getTaskTimeouts()).isEmpty();
        final boolean isDuplicated = mapper.addSetting(settingTable, RobotToken.create("Task Timeout"));
        assertThat(isDuplicated).isFalse();
        assertThat(settingTable.getTaskTimeouts()).hasSize(1)
                .extracting(TaskTimeout::getDeclaration)
                .flatExtracting(RobotToken::getTypes)
                .contains(RobotTokenType.SETTING_TASK_TIMEOUT_DECLARATION);
    }

    @Test
    public void theMapperCorrectlyAddsSetting_ifThereWasAlreadyOne() {
        final TaskTimeoutMapper mapper = new TaskTimeoutMapper();

        final SettingTable settingTable = new SettingTable(null);
        settingTable.addTaskTimeout(new TaskTimeout(RobotToken.create("task timeout")));

        assertThat(settingTable.getTaskTimeouts()).hasSize(1);
        final boolean isDuplicated = mapper.addSetting(settingTable, RobotToken.create("Task Timeout"));
        assertThat(isDuplicated).isTrue();
        assertThat(settingTable.getTaskTimeouts()).hasSize(2)
                .extracting(TaskTimeout::getDeclaration)
                .flatExtracting(RobotToken::getTypes)
                .contains(RobotTokenType.SETTING_TASK_TIMEOUT_DECLARATION);
    }

}
