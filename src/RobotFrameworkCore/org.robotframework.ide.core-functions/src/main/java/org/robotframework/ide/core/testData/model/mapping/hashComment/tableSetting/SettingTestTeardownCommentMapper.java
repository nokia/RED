package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.listener.ITablesGetter;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.TestTeardown;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingTestTeardownCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_TEST_TEARDOWN
                || state == ParsingState.SETTING_TEST_TEARDOWN_KEYWORD || state == ParsingState.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            ITablesGetter fileModel) {
        List<TestTeardown> testTeardowns = fileModel.getSettingTable()
                .getTestTeardowns();
        if (!testTeardowns.isEmpty()) {
            TestTeardown testTeardown = testTeardowns
                    .get(testTeardowns.size() - 1);
            testTeardown.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }

    }

}
