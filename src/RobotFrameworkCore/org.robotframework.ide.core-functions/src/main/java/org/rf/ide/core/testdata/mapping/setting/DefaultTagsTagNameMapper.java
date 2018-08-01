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
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class DefaultTagsTagNameMapper implements IParsingMapper {

    private final ParsingStateHelper utility = new ParsingStateHelper();

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState state = utility.getCurrentStatus(processingState);
        return state == ParsingState.SETTING_DEFAULT_TAGS || state == ParsingState.SETTING_DEFAULT_TAGS_TAG_NAME;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        rt.getTypes().add(0, RobotTokenType.SETTING_DEFAULT_TAG);
        rt.setText(text);

        final SettingTable settings = robotFileOutput.getFileModel().getSettingTable();
        final List<DefaultTags> suiteDefaultTags = settings.getDefaultTags();
        if (!suiteDefaultTags.isEmpty()) {
            suiteDefaultTags.get(suiteDefaultTags.size() - 1).addTag(rt);
        }

        processingState.push(ParsingState.SETTING_DEFAULT_TAGS_TAG_NAME);
        return rt;
    }
}
