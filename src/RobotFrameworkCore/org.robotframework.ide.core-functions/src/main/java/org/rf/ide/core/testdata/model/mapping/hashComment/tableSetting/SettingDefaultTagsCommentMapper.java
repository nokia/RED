/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class SettingDefaultTagsCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_DEFAULT_TAGS || state == ParsingState.SETTING_DEFAULT_TAGS_TAG_NAME);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<DefaultTags> suiteDefaultTags = fileModel.getSettingTable()
                .getDefaultTags();
        if (!suiteDefaultTags.isEmpty()) {
            DefaultTags defaultTags = suiteDefaultTags.get(suiteDefaultTags
                    .size() - 1);
            defaultTags.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }

    }

}
