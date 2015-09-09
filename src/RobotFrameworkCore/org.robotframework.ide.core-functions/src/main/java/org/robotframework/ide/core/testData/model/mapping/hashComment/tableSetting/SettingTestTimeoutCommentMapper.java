/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.TestTimeout;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingTestTimeoutCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_TEST_TIMEOUT
                || state == ParsingState.SETTING_TEST_TIMEOUT_VALUE || state == ParsingState.SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<TestTimeout> testTimeouts = fileModel.getSettingTable()
                .getTestTimeouts();
        if (!testTimeouts.isEmpty()) {
            TestTimeout testTimeout = testTimeouts.get(testTimeouts.size() - 1);
            testTimeout.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }

}
