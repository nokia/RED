/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.hash.comment.tables.setting;

import java.util.List;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SettingTestTimeoutCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_TEST_TIMEOUT || state == ParsingState.SETTING_TEST_TIMEOUT_VALUE
                || state == ParsingState.SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS);
    }

    @Override
    public void map(final RobotLine currentLine, final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel) {
        List<TestTimeout> testTimeouts = fileModel.getSettingTable().getTestTimeouts();
        if (!testTimeouts.isEmpty()) {
            TestTimeout testTimeout = testTimeouts.get(testTimeouts.size() - 1);
            testTimeout.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }

}
