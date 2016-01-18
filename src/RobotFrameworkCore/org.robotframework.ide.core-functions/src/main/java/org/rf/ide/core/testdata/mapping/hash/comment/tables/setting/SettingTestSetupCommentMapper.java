/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.hash.comment.tables.setting;

import java.util.List;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SettingTestSetupCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_TEST_SETUP || state == ParsingState.SETTING_TEST_SETUP_KEYWORD
                || state == ParsingState.SETTING_TEST_SETUP_KEYWORD_ARGUMENT);
    }

    @Override
    public void map(final RobotLine currentLine, final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel) {
        List<TestSetup> testSetups = fileModel.getSettingTable().getTestSetups();
        if (!testSetups.isEmpty()) {
            TestSetup testSetup = testSetups.get(testSetups.size() - 1);
            testSetup.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }

}
