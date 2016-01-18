/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.hash.comment.tables.setting;

import java.util.List;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SettingSuiteTeardownCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_SUITE_TEARDOWN || state == ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD
                || state == ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT);
    }

    @Override
    public void map(final RobotLine currentLine, final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel) {
        List<SuiteTeardown> suiteTeardowns = fileModel.getSettingTable().getSuiteTeardowns();
        if (!suiteTeardowns.isEmpty()) {
            SuiteTeardown suiteTeardown = suiteTeardowns.get(suiteTeardowns.size() - 1);
            suiteTeardown.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }

}
