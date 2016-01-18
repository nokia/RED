/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.hash.comment.tables.setting;

import java.util.List;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SettingTestTeardownCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_TEST_TEARDOWN || state == ParsingState.SETTING_TEST_TEARDOWN_KEYWORD
                || state == ParsingState.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT);
    }

    @Override
    public void map(final RobotLine currentLine, final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel) {
        List<TestTeardown> testTeardowns = fileModel.getSettingTable().getTestTeardowns();
        if (!testTeardowns.isEmpty()) {
            TestTeardown testTeardown = testTeardowns.get(testTeardowns.size() - 1);
            testTeardown.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }

    }

}
