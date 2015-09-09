/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.ForceTags;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingForceTagsCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_FORCE_TAGS || state == ParsingState.SETTING_FORCE_TAGS_TAG_NAME);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<ForceTags> suiteForceTags = fileModel.getSettingTable()
                .getForceTags();
        if (!suiteForceTags.isEmpty()) {
            ForceTags forceTags = suiteForceTags.get(suiteForceTags.size() - 1);
            forceTags.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }

}
