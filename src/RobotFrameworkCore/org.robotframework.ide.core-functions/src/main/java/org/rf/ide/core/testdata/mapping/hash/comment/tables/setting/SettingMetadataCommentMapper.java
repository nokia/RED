/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.hash.comment.tables.setting;

import java.util.List;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SettingMetadataCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_METADATA);
    }

    @Override
    public void map(final RobotLine currentLine, final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel) {
        List<Metadata> metadatas = fileModel.getSettingTable().getMetadatas();
        if (!metadatas.isEmpty()) {
            Metadata metadata = metadatas.get(metadatas.size() - 1);
            metadata.addCommentPart(rt);
        } else {
            // FIXME: errors
        }
    }

}
