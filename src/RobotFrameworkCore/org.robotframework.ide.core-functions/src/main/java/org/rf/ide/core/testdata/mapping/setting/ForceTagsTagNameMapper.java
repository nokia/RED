/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ForceTagsTagNameMapper implements IParsingMapper {

    private final ParsingStateHelper utility;

    public ForceTagsTagNameMapper() {
        this.utility = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.getTypes().add(0, RobotTokenType.SETTING_FORCE_TAG);
        rt.setText(text);

        final SettingTable settings = robotFileOutput.getFileModel()
                .getSettingTable();
        final List<ForceTags> suiteForceTags = settings.getForceTags();
        if (!suiteForceTags.isEmpty()) {
            suiteForceTags.get(suiteForceTags.size() - 1).addTag(rt);
        } else {
            // FIXME: some error
        }
        processingState.push(ParsingState.SETTING_FORCE_TAGS_TAG_NAME);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = utility.getCurrentStatus(processingState);
        result = (state == ParsingState.SETTING_FORCE_TAGS || state == ParsingState.SETTING_FORCE_TAGS_TAG_NAME);
        return result;
    }

}
